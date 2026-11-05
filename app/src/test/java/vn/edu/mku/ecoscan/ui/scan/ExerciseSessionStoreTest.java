package vn.edu.mku.ecoscan.ui.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import android.content.Context;
import android.content.SharedPreferences;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ExerciseSessionStoreTest {
    @Test public void recordsAccuracyByExerciseAndMigratesOldNeckTotals() {
        Context context = RuntimeEnvironment.getApplication();
        SharedPreferences prefs = ExerciseSessionStore.preferences(context);
        prefs.edit().clear().putInt("2026-10-25", 2).commit();

        ExerciseSessionStore.record(context, "2026-10-25", ExerciseType.SHOULDER, 80);
        ExerciseSessionStore.record(context, "2026-10-25", ExerciseType.SHOULDER, 90);

        assertEquals(4, prefs.getInt("2026-10-25", 0));
        assertEquals(2, prefs.getInt("2026-10-25:neck", 0));
        assertEquals(2, prefs.getInt("2026-10-25:shoulder", 0));
        assertEquals(Integer.valueOf(85), ExerciseSessionStore.accuracy(
                prefs, "2026-10-25", ExerciseType.SHOULDER));
        assertNull(ExerciseSessionStore.accuracy(
                prefs, "2026-10-25", ExerciseType.NECK));
    }
}
