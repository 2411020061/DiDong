"""Send a Windows webcam to the Android debug app running in LDPlayer.

Install: py -m pip install opencv-python
Run:     py tools/webcam_bridge.py
"""
import argparse
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import socket
import secrets
from urllib.parse import urlsplit, parse_qs
import sys
import threading

try:
    import cv2
except ImportError:
    sys.exit("Cần OpenCV: py -m pip install opencv-python")


def local_addresses():
    addresses = set()
    for info in socket.getaddrinfo(socket.gethostname(), None, socket.AF_INET):
        address = info[4][0]
        if not address.startswith("127."):
            addresses.add(address)
    return sorted(addresses)


def main():
    parser = argparse.ArgumentParser(description="BodyPoint webcam bridge for LDPlayer")
    parser.add_argument("--camera", default="0", help="Webcam index (default: 0), or a video file for testing")
    parser.add_argument("--port", type=int, default=8765)
    args = parser.parse_args()

    source = int(args.camera) if args.camera.isdigit() else args.camera
    capture = cv2.VideoCapture(source, cv2.CAP_DSHOW if sys.platform == "win32" and isinstance(source, int) else cv2.CAP_ANY)
    if not capture.isOpened() and sys.platform == "win32" and isinstance(source, int):
        capture.release()
        capture = cv2.VideoCapture(source, cv2.CAP_MSMF)
    if not capture.isOpened():
        sys.exit("Không mở được webcam. Kiểm tra quyền camera Windows và thử --camera 1.")
    capture.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
    capture.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
    lock = threading.Lock()
    pairing_code = secrets.token_urlsafe(18)

    class Handler(BaseHTTPRequestHandler):
        def do_GET(self):
            url = urlsplit(self.path)
            supplied = parse_qs(url.query).get("token", [""])[0]
            if not secrets.compare_digest(supplied, pairing_code):
                self.send_error(403, "Pairing code required")
                return
            if url.path == "/health":
                body = b"BodyPoint webcam ready\n"
                self.send_response(200)
                self.send_header("Content-Type", "text/plain; charset=utf-8")
            elif url.path == "/frame":
                with lock:
                    ok, frame = capture.read()
                if not ok:
                    self.send_error(503, "Webcam did not return a frame")
                    return
                ok, encoded = cv2.imencode(".jpg", frame, [cv2.IMWRITE_JPEG_QUALITY, 80])
                if not ok:
                    self.send_error(503, "Could not encode frame")
                    return
                body = encoded.tobytes()
                self.send_response(200)
                self.send_header("Content-Type", "image/jpeg")
            else:
                self.send_error(404)
                return
            self.send_header("Cache-Control", "no-store")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)

        def log_message(self, fmt, *values):
            # The URL carries the pairing code; do not print its query string.
            if urlsplit(self.path).path != "/frame":
                super().log_message("%s %s", self.command, urlsplit(self.path).path)

    server = ThreadingHTTPServer(("0.0.0.0", args.port), Handler)
    print("BodyPoint webcam bridge is running. Keep this window open.")
    print("LDPlayer host alias: 10.0.2.2")
    for address in local_addresses():
        print("If the alias fails, enter this PC address in BodyPoint:", address)
    print("Pairing code (enter in BodyPoint):", pairing_code)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()
        capture.release()


if __name__ == "__main__":
    main()
