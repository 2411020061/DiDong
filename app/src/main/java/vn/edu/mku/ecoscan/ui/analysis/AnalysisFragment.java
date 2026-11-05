package vn.edu.mku.ecoscan.ui.analysis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.concurrent.Executors;

import vn.edu.mku.ecoscan.R;
import vn.edu.mku.ecoscan.data.AppDatabase;
import vn.edu.mku.ecoscan.data.ScanResult;
import vn.edu.mku.ecoscan.ui.MainActivity;

public class AnalysisFragment extends Fragment {

    private ImageButton btnBack;
    private TextView textCaptureSub, textModeBadge, textConfidenceVal, textLandmarksVal, textDateTimestamp;
    private TextView textShoulderLeft, textShoulderRight, textElbowLeft, textElbowRight, textHipLeft, textHipRight, textKneeLeft, textKneeRight;
    private MaterialButton btnSaveSession, btnReviewPosture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_capture_analysis, container, false);

        findViews(root);
        setupListeners();
        loadLatestResult();

        return root;
    }

    private void findViews(View root) {
        btnBack = root.findViewById(R.id.btnBack);
        textCaptureSub = root.findViewById(R.id.textCaptureSub);
        textModeBadge = root.findViewById(R.id.textModeBadge);
        textConfidenceVal = root.findViewById(R.id.textConfidenceVal);
        textLandmarksVal = root.findViewById(R.id.textLandmarksVal);
        textDateTimestamp = root.findViewById(R.id.textDateTimestamp);

        textShoulderLeft = root.findViewById(R.id.textShoulderLeft);
        textShoulderRight = root.findViewById(R.id.textShoulderRight);
        textElbowLeft = root.findViewById(R.id.textElbowLeft);
        textElbowRight = root.findViewById(R.id.textElbowRight);
        textHipLeft = root.findViewById(R.id.textHipLeft);
        textHipRight = root.findViewById(R.id.textHipRight);
        textKneeLeft = root.findViewById(R.id.textKneeLeft);
        textKneeRight = root.findViewById(R.id.textKneeRight);

        btnSaveSession = root.findViewById(R.id.btnSaveSession);
        btnReviewPosture = root.findViewById(R.id.btnReviewPosture);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToScan();
            }
        });

        btnSaveSession.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Session saved to history!", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToHistory();
            }
        });

        btnReviewPosture.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToPostureSymmetry();
            }
        });
    }

    private void loadLatestResult() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ScanResult result = AppDatabase.getInstance(requireContext()).scanResultDao().getLatest();
            if (result != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    textCaptureSub.setText("CAPTURE / " + (result.captureId != null ? result.captureId : "BP-1042"));
                    textModeBadge.setText((result.mode != null ? result.mode.toUpperCase() : "FULL BODY") + " • STILL");
                    textConfidenceVal.setText((int) (result.confidence * 100) + "%");
                    textLandmarksVal.setText((result.landmarkCount > 0 ? result.landmarkCount : 33) + " / 33");

                    textShoulderLeft.setText((int) (result.shoulderLeftAngle > 0 ? result.shoulderLeftAngle : 17) + "°");
                    textShoulderRight.setText((int) (result.shoulderRightAngle > 0 ? result.shoulderRightAngle : 17) + "°");
                    textElbowLeft.setText((int) (result.elbowLeftAngle > 0 ? result.elbowLeftAngle : 165) + "°");
                    textElbowRight.setText((int) (result.elbowRightAngle > 0 ? result.elbowRightAngle : 165) + "°");
                    textHipLeft.setText((int) (result.hipLeftAngle > 0 ? result.hipLeftAngle : 176) + "°");
                    textHipRight.setText((int) (result.hipRightAngle > 0 ? result.hipRightAngle : 90) + "°");
                    textKneeLeft.setText((int) (result.kneeLeftAngle > 0 ? result.kneeLeftAngle : 176) + "°");
                    textKneeRight.setText((int) (result.kneeRightAngle > 0 ? result.kneeRightAngle : 90) + "°");
                });
            }
        });
    }
}