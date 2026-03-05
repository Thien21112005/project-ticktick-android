package hcmute.edu.vn.ticktick;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class Next7DaysFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Vẫn dùng giao diện cũ cho nhanh
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // Nhưng ta sẽ tìm cái TextView tiêu đề để đổi chữ thành "7 Ngày Tới"
        // (Lưu ý: Bạn cần mở file fragment_tasks.xml và thêm android:id="@+id/tv_title" cho thẻ TextView tiêu đề nhé)
        TextView tvTitle = view.findViewById(R.id.tv_title);
        if(tvTitle != null) {
            tvTitle.setText("7 Ngày Tới");
        }
        return view;
    }
}