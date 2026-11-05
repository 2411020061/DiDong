package vn.edu.mku.ecoscan.ui.scan;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

/** Local Windows webcam bridge used only by debug builds on Android emulators. */
final class WebcamBridge {
    static final String DEFAULT_HOST = "10.0.2.2";
    private static final String PREFS = "bodypoint_webcam";
    private static final String KEY_HOST = "host";
    private static final String KEY_TOKEN = "token";

    private WebcamBridge() { }

    static boolean canUse(Context context) {
        if ((context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) return false;
        if (!token(context).isEmpty()) return true;
        String hardware = Build.HARDWARE.toLowerCase(Locale.US);
        String fingerprint = Build.FINGERPRINT.toLowerCase(Locale.US);
        return hardware.contains("vbox") || hardware.contains("ranchu")
                || hardware.contains("goldfish") || fingerprint.contains("generic")
                || fingerprint.contains("emulator")
                || Build.MODEL.toLowerCase(Locale.US).contains("ldplayer");
    }

    static String host(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_HOST, DEFAULT_HOST);
    }

    static String token(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_TOKEN, "");
    }

    static void setConnection(Context context, String host, String token) {
        if (!validHost(host)) throw new IllegalArgumentException("Địa chỉ IPv4 không hợp lệ");
        if (token.isEmpty()) throw new IllegalArgumentException("Cần mã ghép nối");
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_HOST, host).putString(KEY_TOKEN, token).apply();
    }

    static boolean validHost(String host) {
        if (host == null || !host.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}")) return false;
        for (String part : host.split("\\."))
            if (Integer.parseInt(part) > 255) return false;
        return true;
    }

    static Bitmap frame(String host, String token) throws Exception {
        if (!validHost(host) || token.isEmpty())
            throw new IllegalArgumentException("Cần IP và mã ghép nối webcam");
        HttpURLConnection connection = (HttpURLConnection) new URL(
                "http://" + host + ":8765/frame?token="
                        + URLEncoder.encode(token, "UTF-8")).openConnection();
        connection.setConnectTimeout(800);
        connection.setReadTimeout(1500);
        connection.setUseCaches(false);
        try {
            if (connection.getResponseCode() != 200)
                throw new IllegalStateException("Webcam trả về " + connection.getResponseCode());
            try (InputStream stream = connection.getInputStream()) {
                Bitmap image = BitmapFactory.decodeStream(stream);
                if (image == null) throw new IllegalStateException("Khung hình webcam không hợp lệ");
                return image;
            }
        } finally {
            connection.disconnect();
        }
    }
}
