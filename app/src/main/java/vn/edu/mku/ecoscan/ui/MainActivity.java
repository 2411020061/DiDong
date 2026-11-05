package vn.edu.mku.ecoscan.ui;

import android.os.Bundle;
import android.graphics.Color;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import vn.edu.mku.ecoscan.R;
import vn.edu.mku.ecoscan.ui.scan.ExerciseFragment;
import vn.edu.mku.ecoscan.ui.scan.ExerciseListFragment;
import vn.edu.mku.ecoscan.ui.scan.ExerciseType;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private ExerciseType selectedExercise = ExerciseType.NECK;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            selectedExercise = ExerciseType.fromKey(savedInstanceState.getString("selected_exercise"));
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        findViewById(R.id.main).setBackgroundColor(Color.WHITE);
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setBackgroundColor(Color.WHITE);
        bottomNav.setItemIconTintList(getColorStateList(R.color.bottom_nav_scan_selector));
        bottomNav.setItemTextColor(getColorStateList(R.color.bottom_nav_scan_selector));
        WindowCompat.getInsetsController(getWindow(), findViewById(R.id.main)).setAppearanceLightStatusBars(true);
        WindowCompat.getInsetsController(getWindow(), findViewById(R.id.main)).setAppearanceLightNavigationBars(true);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_exercise) replace(new ExerciseListFragment());
            else if (item.getItemId() == R.id.nav_tracking) replace(new TrackingFragment());
            return true;
        });
        if (savedInstanceState == null) bottomNav.setSelectedItemId(R.id.nav_exercise);
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        outState.putString("selected_exercise", selectedExercise.key);
        super.onSaveInstanceState(outState);
    }

    public void navigateToHome() { navigateToExercise(); }
    public void navigateToExercise() { bottomNav.setSelectedItemId(R.id.nav_exercise); }
    public void navigateToExercise(ExerciseType type) {
        selectedExercise = type;
        replace(ExerciseFragment.forType(type));
    }
    public void navigateToTracking() { bottomNav.setSelectedItemId(R.id.nav_tracking); }
    public void navigateToChat() { replace(new ChatFragment()); }
    public void navigateToScan() { navigateToExercise(); }
    public void navigateToAnalysis() { navigateToTracking(); }
    public void navigateToHistory() { navigateToTracking(); }
    public void navigateToPostureSymmetry() { navigateToTracking(); }

    private void replace(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
    }
}
