package vn.edu.mku.ecoscan.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import vn.edu.mku.ecoscan.R;

public class ChatFragment extends Fragment {
    private LinearLayout container;
    private ScrollView scrollView;
    private EditText input;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup containerView, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, containerView, false);
        container = root.findViewById(R.id.chatMessagesContainer);
        scrollView = root.findViewById(R.id.chatScroll);
        input = root.findViewById(R.id.chatInput);

        root.findViewById(R.id.chatBack).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTracking();
            }
        });

        root.findViewById(R.id.chatSend).setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                addUserMessage(text);
                input.setText("");
                replyAi(text);
            }
        });

        root.findViewById(R.id.suggestionFeel).setOnClickListener(v -> {
            String q = "Cảm nhận cơ thể ra sao sau khi tập?";
            addUserMessage(q);
            replyAi(q);
        });

        root.findViewById(R.id.suggestionTechnique).setOnClickListener(v -> {
            String q = "Cách giãn cơ đúng chuẩn là gì?";
            addUserMessage(q);
            replyAi(q);
        });

        root.findViewById(R.id.suggestionPrecautions).setOnClickListener(v -> {
            String q = "Những điều không nên làm khi tập là gì?";
            addUserMessage(q);
            replyAi(q);
        });

        return root;
    }

    private void addUserMessage(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextColor(0xFF0F172A);
        tv.setTextSize(14);
        tv.setBackgroundResource(R.drawable.bg_card_white);
        tv.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.gravity = Gravity.END;
        params.bottomMargin = dp(8);
        params.leftMargin = dp(48);
        container.addView(tv, params);
        scrollToBottom();
    }

    private void addAiMessage(String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextColor(0xFF08A364);
        tv.setTextSize(14);
        tv.setBackgroundResource(R.drawable.bg_banner_mint);
        tv.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.gravity = Gravity.START;
        params.bottomMargin = dp(8);
        params.rightMargin = dp(48);
        container.addView(tv, params);
        scrollToBottom();
    }

    private void replyAi(String query) {
        String lower = query.toLowerCase();
        String reply;
        if (lower.contains("cảm nhận") || lower.contains("cơ thể")) {
            reply = "🤖 Trợ lý AI:\n\nSau khi tập, cơ thể ba mẹ sẽ cảm thấy nhẹ nhàng, các cơ được kéo giãn thoải mái. Cảm giác mỏi cơ nhẹ là bình thường, giúp tuần hoàn máu tốt hơn!";
        } else if (lower.contains("đúng") || lower.contains("chuẩn") || lower.contains("cách giãn")) {
            reply = "🤖 Trợ lý AI:\n\n1. Thực hiện từ từ, không vội vàng.\n2. Giữ thẳng lưng và cổ.\n3. Hít thở sâu và đều đặn.\n4. Giữ mỗi động tác khoảng 30 giây.";
        } else if (lower.contains("không nên") || lower.contains("tránh") || lower.contains("làm")) {
            reply = "🤖 Trợ lý AI:\n\n1. Không nín thở khi kéo giãn.\n2. Không giật cục hay uốn quá tầm.\n3. Không cố tập khi đang đau cấp tính hoặc quá mệt.";
        } else {
            reply = "🤖 Trợ lý AI:\n\nCảm ơn ba mẹ đã hỏi! Hãy duy trì tập luyện mỗi ngày để cơ thể luôn khỏe mạnh, linh hoạt và sống vui sống khỏe nhé!";
        }
        container.postDelayed(() -> addAiMessage(reply), 400);
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
