package hcmute.edu.vn.ticktick;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

public class TasksFragment extends Fragment {

    // Hàm này được gọi khi Fragment bắt đầu vẽ giao diện
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Nạp file giao diện fragment_tasks.xml vào Java và hiển thị lên
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }
}