package hcmute.edu.vn.ticktick;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class InboxFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Tái sử dụng giao diện fragment_tasks.xml
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // Đổi chữ tiêu đề thành "Inbox"
        TextView tvTitle = view.findViewById(R.id.tv_title);
        if(tvTitle != null) {
            tvTitle.setText("Inbox");
        }
        return view;
    }
}