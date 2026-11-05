# Dùng webcam máy tính với BodyPoint trong LDPlayer

1. Cài Python 3 (có lệnh `py`) trên Windows nếu chưa có. Đóng ứng dụng khác đang giữ webcam. Bật quyền camera cho ứng dụng desktop trong Windows Settings.
2. Trong checkout BodyPoint, chạy `tools\start_webcam_windows.bat`. Giữ cửa sổ đó mở. Lần đầu script sẽ cài `opencv-python` qua pip.
3. Mở bản **debug** của BodyPoint trong LDPlayer. Chạm thông báo trạng thái camera trong app rồi nhập IP `10.0.2.2` và mã ghép nối do script in ra. Nếu địa chỉ đó không kết nối, nhập một địa chỉ IPv4 của máy tính do script in ra. Windows Firewall có thể yêu cầu cho phép Python trên mạng riêng.
4. Thử `http://<IP-máy-tính>:8765/health?token=<mã-ghép-nối>` trong trình duyệt của LDPlayer nếu vẫn không kết nối. Endpoint này phải trả `BodyPoint webcam ready`.

Luồng này chỉ chạy khi script trên máy tính đang mở. Trên điện thoại, ứng dụng tiếp tục dùng camera trước của máy. Bản release không bật kết nối HTTP nội bộ.
