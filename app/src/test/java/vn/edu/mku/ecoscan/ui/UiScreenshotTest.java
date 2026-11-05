package vn.edu.mku.ecoscan.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import vn.edu.mku.ecoscan.R;
import vn.edu.mku.ecoscan.ui.scan.ExerciseSessionStore;
import vn.edu.mku.ecoscan.ui.scan.ExerciseType;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "w393dp-h852dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class UiScreenshotTest {
    @Test public void renderExerciseAndTracking() throws Exception {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        ExerciseSessionStore.preferences(activity).edit().clear().commit();
        BottomNavigationView nav = activity.findViewById(R.id.bottomNav);
        activity.getSupportFragmentManager().executePendingTransactions();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS);
        nav.setSelectedItemId(R.id.nav_exercise);
        activity.getSupportFragmentManager().executePendingTransactions();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS);
        save(activity, "bai-tap-preview.png", false);
        nav.setSelectedItemId(R.id.nav_tracking);
        activity.getSupportFragmentManager().executePendingTransactions();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS);
        save(activity, "theo-doi-preview.png", false);
        SimpleDateFormat key = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar day = Calendar.getInstance();
        ExerciseSessionStore.record(activity, key.format(day.getTime()), ExerciseType.NECK, 88);
        ExerciseSessionStore.record(activity, key.format(day.getTime()), ExerciseType.SHOULDER, 82);
        day.add(Calendar.DAY_OF_YEAR, -1);
        ExerciseSessionStore.record(activity, key.format(day.getTime()), ExerciseType.BICEPS, 91);
        day.add(Calendar.DAY_OF_YEAR, -2);
        ExerciseSessionStore.record(activity, key.format(day.getTime()), ExerciseType.WRIST_UP, 76);
        nav.setSelectedItemId(R.id.nav_exercise);
        nav.setSelectedItemId(R.id.nav_tracking);
        activity.getSupportFragmentManager().executePendingTransactions();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS);
        save(activity, "theo-doi-co-du-lieu-preview.png", true);
    }

    private void save(FragmentActivity activity, String name, boolean scrollBottom) throws Exception {
        View root = activity.findViewById(R.id.main);
        float density = activity.getResources().getDisplayMetrics().density;
        int width = Math.round(393 * density);
        int height = Math.round(852 * density);
        root.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        root.layout(0, 0, width, height);
        View fragment = ((ViewGroup) activity.findViewById(R.id.fragmentContainer)).getChildAt(0);
        if (fragment instanceof ScrollView) {
            ScrollView scroll = (ScrollView) fragment;
            scroll.scrollTo(0, scrollBottom ? Math.max(0, scroll.getChildAt(0).getHeight() - scroll.getHeight()) : 0);
        }
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(image));
        File directory = new File(findProjectRoot(), "app/build/ui-previews");
        if (!directory.exists() && !directory.mkdirs()) throw new Exception("Cannot create preview folder");
        try (FileOutputStream output = new FileOutputStream(new File(directory, name))) {
            image.compress(Bitmap.CompressFormat.PNG, 100, output);
        }
        image.recycle();
    }

    private File findProjectRoot() throws Exception {
        File location = new File(System.getProperty("user.dir"));
        while (location != null) {
            if (new File(location, "app/src/main").isDirectory()) return location;
            location = location.getParentFile();
        }
        throw new Exception("Cannot find Android project root");
    }
}
