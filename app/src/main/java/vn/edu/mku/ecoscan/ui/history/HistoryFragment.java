package vn.edu.mku.ecoscan.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import vn.edu.mku.ecoscan.R;
import vn.edu.mku.ecoscan.data.AppDatabase;
import vn.edu.mku.ecoscan.data.ScanResult;
import vn.edu.mku.ecoscan.ui.MainActivity;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerHistory;
    private HistoryRvAdapter adapter;
    private SymmetryGraphView symmetryGraphView;
    private TextView textSessionsCount;
    private MaterialButton btnNewBodyScan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        findViews(root);
        setupListeners();
        loadHistory();

        return root;
    }

    private void findViews(View root) {
        recyclerHistory = root.findViewById(R.id.recyclerHistory);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(requireContext()));

        symmetryGraphView = root.findViewById(R.id.symmetryGraphView);
        textSessionsCount = root.findViewById(R.id.textSessionsCount);
        btnNewBodyScan = root.findViewById(R.id.btnNewBodyScan);
    }

    private void setupListeners() {
        btnNewBodyScan.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToScan();
            }
        });
    }

    private void loadHistory() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ScanResult> dbResults = AppDatabase.getInstance(requireContext())
                    .scanResultDao().getAll();

            if (dbResults == null || dbResults.isEmpty()) {
                dbResults = createSampleData();
            }

            List<ScanResult> finalResults = dbResults;
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    adapter = new HistoryRvAdapter(finalResults);
                    adapter.setOnItemClickListener(item -> {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).navigateToAnalysis();
                        }
                    });
                    recyclerHistory.setAdapter(adapter);

                    textSessionsCount.setText(finalResults.size() + " sessions recorded");

                    if (symmetryGraphView != null) {
                        symmetryGraphView.setData(new float[]{88f, 91f, 89f, 92f, 91f, 93f, 94f}, "28 SEP", "05 OCT");
                    }
                });
            }
        });
    }

    private List<ScanResult> createSampleData() {
        List<ScanResult> list = new ArrayList<>();

        ScanResult r1 = new ScanResult();
        r1.captureId = "BP-1042";
        r1.timestamp = System.currentTimeMillis();
        r1.confidence = 0.99f;
        r1.bodySymmetry = 94;
        list.add(r1);

        ScanResult r2 = new ScanResult();
        r2.captureId = "BP-1041";
        r2.timestamp = System.currentTimeMillis() - 86400000L;
        r2.confidence = 0.98f;
        r2.bodySymmetry = 93;
        list.add(r2);

        ScanResult r3 = new ScanResult();
        r3.captureId = "BP-1040";
        r3.timestamp = System.currentTimeMillis() - (86400000L * 2);
        r3.confidence = 0.99f;
        r3.bodySymmetry = 91;
        list.add(r3);

        return list;
    }
}