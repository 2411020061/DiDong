package vn.edu.mku.ecoscan.ui.scan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import vn.edu.mku.ecoscan.R;
import vn.edu.mku.ecoscan.ui.MainActivity;

public class ExerciseListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_exercise_list, container, false);
        LinearLayout containerList = root.findViewById(R.id.exerciseListContainer);
        ExerciseType[] types = ExerciseType.activeTypes();
        for (ExerciseType type : types) {
            View card = inflater.inflate(R.layout.item_home_exercise, containerList, false);
            ((ImageView) card.findViewById(R.id.exerciseThumbnail))
                    .setImageResource(type == ExerciseType.NECK
                            ? R.drawable.home_neck_stretch : type.imageRes);
            ((TextView) card.findViewById(R.id.exerciseName)).setText(type.title);
            ((TextView) card.findViewById(R.id.exerciseSummary)).setText(type.instruction);
            card.setContentDescription(type.title + ". " + type.instruction);
            card.setOnClickListener(v -> ((MainActivity) requireActivity()).navigateToExercise(type));
            View camIcon = card.findViewById(R.id.exerciseCameraIcon);
            if (camIcon != null) {
                camIcon.setOnClickListener(v -> ((MainActivity) requireActivity()).navigateToExercise(type));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.bottomMargin = (int) (10 * getResources().getDisplayMetrics().density + 0.5f);
            containerList.addView(card, params);
        }
        return root;
    }
}
