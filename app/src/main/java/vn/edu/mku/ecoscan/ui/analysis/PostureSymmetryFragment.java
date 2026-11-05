package vn.edu.mku.ecoscan.ui.analysis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.concurrent.Executors;

import vn.edu.mku.ecoscan.R;
import vn.edu.mku.ecoscan.data.AppDatabase;
import vn.edu.mku.ecoscan.data.ScanResult;
import vn.edu.mku.ecoscan.ui.MainActivity;

public class PostureSymmetryFragment extends Fragment {

    private ImageButton btnBack;
    private TextView textPostureSub, textShoulderTilt, textHipTilt, textTrunkLean, textSymmetryVal;
    private TextView textSideElbowLeft, textSideElbowRight, textSideKneeLeft, textSideKneeRight;
    private ProgressBar progressSymmetry;
    private MaterialButton btnViewSavedSessions;
    private View bannerWarning;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_posture_symmetry, container, false);

        findViews(root);
        setupListeners();
        loadData();

        return root;
    }

    private void findViews(View root) {
        btnBack = root.findViewById(R.id.btnBack);
        textPostureSub = root.findViewById(R.id.textPostureSub);
        textShoulderTilt = root.findViewById(R.id.textShoulderTilt);
        textHipTilt = root.findViewById(R.id.textHipTilt);
        textTrunkLean = root.findViewById(R.id.textTrunkLean);
        textSymmetryVal = root.findViewById(R.id.textSymmetryVal);

        progressSymmetry = root.findViewById(R.id.progressSymmetry);
        textSideElbowLeft = root.findViewById(R.id.textSideElbowLeft);
        textSideElbowRight = root.findViewById(R.id.textSideElbowRight);
        textSideKneeLeft = root.findViewById(R.id.textSideKneeLeft);
        textSideKneeRight = root.findViewById(R.id.textSideKneeRight);

        bannerWarning = root.findViewById(R.id.bannerWarning);
        btnViewSavedSessions = root.findViewById(R.id.btnViewSavedSessions);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToAnalysis();
            }
        });

        btnViewSavedSessions.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToHistory();
            }
        });
    }

    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ScanResult result = AppDatabase.getInstance(requireContext()).scanResultDao().getLatest();
            if (result != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    textPostureSub.setText("CAPTURE / " + (result.captureId != null ? result.captureId : "BP-1042"));
                    textShoulderTilt.setText((result.shoulderTilt > 0 ? result.shoulderTilt : 1.8f) + "°");
                    textHipTilt.setText((result.hipTilt > 0 ? result.hipTilt : 1.2f) + "°");
                    textTrunkLean.setText((result.trunkLean > 0 ? result.trunkLean : 2.4f) + "°");

                    int sym = result.bodySymmetry > 0 ? result.bodySymmetry : 94;
                    textSymmetryVal.setText(sym + "%");
                    progressSymmetry.setProgress(sym);

                    textSideElbowLeft.setText((int) (result.elbowLeftAngle > 0 ? result.elbowLeftAngle : 165) + "°");
                    textSideElbowRight.setText((int) (result.elbowRightAngle > 0 ? result.elbowRightAngle : 165) + "°");
                    textSideKneeLeft.setText((int) (result.kneeLeftAngle > 0 ? result.kneeLeftAngle : 176) + "°");
                    textSideKneeRight.setText((int) (result.kneeRightAngle > 0 ? result.kneeRightAngle : 90) + "°");

                    if (bannerWarning != null) {
                        bannerWarning.setVisibility(result.raisedKneeDetected ? View.VISIBLE : View.GONE);
                    }
                });
            }
        });
    }
}