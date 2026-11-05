package vn.edu.mku.ecoscan.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import vn.edu.mku.ecoscan.R;
import vn.edu.mku.ecoscan.ui.scan.ExerciseSessionStore;
import vn.edu.mku.ecoscan.ui.scan.ExerciseType;

public class TrackingFragment extends Fragment {
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tracking, container, false);
        SharedPreferences prefs = ExerciseSessionStore.preferences(requireContext());
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        int[] weekCounts = new int[7];
        String[] weekLabels = new String[7];
        String[] weekExerciseNames = new String[7];

        for (int offset = 6; offset >= 0; offset--) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DAY_OF_YEAR, -offset);
            String dateKey = keyFormat.format(date.getTime());
            int count = prefs.getInt(dateKey, 0);
            weekCounts[6 - offset] = count;
            weekLabels[6 - offset] = dayFormat.format(date.getTime());
            if (count > 0) {
                String[] details = ExerciseSessionStore.getSessionDetails(prefs, dateKey);
                if (details != null) {
                    ExerciseType type = ExerciseType.fromKey(details[0]);
                    weekExerciseNames[6 - offset] = type != null ? type.title : "Giãn cổ";
                } else {
                    weekExerciseNames[6 - offset] = "Giãn cổ";
                }
            }
        }
        ((TrackingChartView) root.findViewById(R.id.trackingChart))
                .setData(weekCounts, weekLabels, weekExerciseNames);

        LinearLayout history = root.findViewById(R.id.trackingHistory);
        boolean any = false;
        for (int offset = 0; offset < 30; offset++) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DAY_OF_YEAR, -offset);
            String dateKey = keyFormat.format(date.getTime());
            for (ExerciseType type : ExerciseType.values()) {
                String typeKey = dateKey + ":" + type.key;
                int count = prefs.getInt(typeKey, type == ExerciseType.NECK
                        ? prefs.getInt(dateKey, 0) : 0);
                if (count == 0) continue;
                any = true;
                View row = inflater.inflate(R.layout.item_tracking_session, history, false);
                ((TextView) row.findViewById(R.id.sessionTitle)).setText(type.title);
                ((TextView) row.findViewById(R.id.sessionDate)).setText(
                        fullDateFormat.format(date.getTime()) + " · " + count + " lượt");
                row.findViewById(R.id.btnSessionChat).setOnClickListener(v -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).navigateToChat();
                    }
                });
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
                params.bottomMargin = dp(9);
                history.addView(row, params);
            }
        }
        if (!any) {
            TextView empty = new TextView(requireContext());
            empty.setText("Chưa có lượt tập nào.");
            empty.setTextColor(0xFF607492);
            empty.setTextSize(15);
            empty.setPadding(dp(16), dp(20), dp(16), dp(20));
            empty.setBackgroundResource(R.drawable.bg_exercise_stat);
            history.addView(empty);
        }
        return root;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
