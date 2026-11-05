package vn.edu.mku.ecoscan.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.mku.ecoscan.R;
import vn.edu.mku.ecoscan.data.ScanResult;

public class HistoryRvAdapter extends RecyclerView.Adapter<HistoryRvAdapter.HistoryViewHolder> {

    private final List<ScanResult> results;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ScanResult item);
    }

    public HistoryRvAdapter(List<ScanResult> results) {
        this.results = results;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_history_rv, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ScanResult item = results.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    public ScanResult getItemAt(int position) {
        return results.get(position);
    }

    public void removeAt(int position) {
        results.remove(position);
        notifyItemRemoved(position);
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDetails, textBadge, textSymmetry;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textHistoryTitle);
            textDetails = itemView.findViewById(R.id.textHistoryDetails);
            textBadge = itemView.findViewById(R.id.textHistoryBadge);
            textSymmetry = itemView.findViewById(R.id.textHistorySymmetry);
        }

        public void bind(ScanResult item, OnItemClickListener listener) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
            String dateStr = item.timestamp > 0 ? sdf.format(new Date(item.timestamp)) : "Today, 05 Oct";
            textTitle.setText(dateStr);

            SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String timeStr = item.timestamp > 0 ? timeSdf.format(new Date(item.timestamp)) : "09:41 AM";
            textDetails.setText(timeStr + " · " + (int) (item.confidence > 0 ? item.confidence * 100 : 99) + "% confidence");

            textBadge.setText((item.captureId != null ? item.captureId : "BP-1042") + " · Saved");
            textSymmetry.setText((item.bodySymmetry > 0 ? item.bodySymmetry : 94) + "%");

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }
}