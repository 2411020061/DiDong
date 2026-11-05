package vn.edu.mku.ecoscan.ui.scan;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Keeps daily exercise counts and the measured posture accuracy for each exercise. */
public final class ExerciseSessionStore {
    private ExerciseSessionStore() { }

    public static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences("exercise_sessions", Context.MODE_PRIVATE);
    }

    public static void record(Context context, String date, ExerciseType type, int accuracy) {
        SharedPreferences prefs = preferences(context);
        int total = prefs.getInt(date, 0);
        String neckKey = date + ":" + ExerciseType.NECK.key;
        String typeKey = date + ":" + type.key;
        String timeKey = date + ":time:" + total;
        String typeIndexKey = date + ":typeindex:" + total;
        String timeStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        SharedPreferences.Editor update = prefs.edit();
        // Old versions stored only the total, which consisted entirely of neck sessions.
        if (!prefs.contains(neckKey)) update.putInt(neckKey, total);
        update.putInt(typeKey, (type == ExerciseType.NECK && !prefs.contains(neckKey)
                ? total : prefs.getInt(typeKey, 0)) + 1);
        update.putInt(date, total + 1);
        update.putString(timeKey, timeStr);
        update.putString(typeIndexKey, type.key);
        update.putInt("accuracy_sum:" + typeKey,
                prefs.getInt("accuracy_sum:" + typeKey, 0) + accuracy);
        update.putInt("accuracy_count:" + typeKey,
                prefs.getInt("accuracy_count:" + typeKey, 0) + 1);
        update.apply();
    }

    public static String[] getSessionDetails(SharedPreferences prefs, String date) {
        int total = prefs.getInt(date, 0);
        if (total <= 0) return null;
        String typeKey = prefs.getString(date + ":typeindex:" + (total - 1), ExerciseType.NECK.key);
        String timeStr = prefs.getString(date + ":time:" + (total - 1), "08:00");
        return new String[]{typeKey, timeStr};
    }

    /** Returns null for historical sessions recorded before accuracy was measured. */
    public static Integer accuracy(SharedPreferences prefs, String date, ExerciseType type) {
        String typeKey = date + ":" + type.key;
        int count = prefs.getInt("accuracy_count:" + typeKey, 0);
        return count == 0 ? null : Math.round(
                (float) prefs.getInt("accuracy_sum:" + typeKey, 0) / count);
    }
}
