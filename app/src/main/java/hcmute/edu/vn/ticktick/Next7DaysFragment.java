package hcmute.edu.vn.ticktick;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.ticktick.database.DatabaseHelper;
import hcmute.edu.vn.ticktick.models.SubTask;

public class Next7DaysFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Tái sử dụng giao diện fragment_tasks.xml
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // 1. Đổi tiêu đề thành "7 Ngày Tới"
        TextView tvTitle = view.findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText("7 Ngày Tới");
        }

        // 2. Tìm cái "băng chuyền" (RecyclerView) và cài đặt chiều dọc
        recyclerView = view.findViewById(R.id.recycler_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseHelper = new DatabaseHelper(getContext());

        // 3. Lấy TẤT CẢ công việc từ kho
        List<SubTask> allSubTasks = databaseHelper.getAllSubTasksWithCategory();

        // Tạo một cái "giỏ" trống để đựng công việc 7 ngày tới
        List<SubTask> next7DaysTasks = new ArrayList<>();

        // --- THUẬT TOÁN LỌC TÌM CÔNG VIỆC TRONG 7 NGÀY ---

        // Công cụ dịch chữ thành Ngày Giờ (Đúng với chuẩn ngày/tháng/năm giờ:phút của bạn)
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());

        // Lấy Mốc A: Hôm nay (Bắt đầu từ 0h sáng)
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);

        // Lấy Mốc B: 7 Ngày sau (Đến 23h59 đêm)
        Calendar next7Days = Calendar.getInstance();
        next7Days.add(Calendar.DAY_OF_YEAR, 7); // Cộng thêm 7 ngày
        next7Days.set(Calendar.HOUR_OF_DAY, 23);
        next7Days.set(Calendar.MINUTE, 59);

        // Đi kiểm tra từng công việc
        for (SubTask task : allSubTasks) {
            try {
                // Nếu công việc có ngày giờ đàng hoàng
                if (task.getStartDateTime() != null && !task.getStartDateTime().isEmpty()) {
                    // Dịch chuỗi text (VD: "8/3/2026 10:05") thành đối tượng Date
                    Date taskDate = sdf.parse(task.getStartDateTime());

                    // Nếu thời gian công việc: Lớn hơn hoặc bằng Mốc A VÀ Nhỏ hơn hoặc bằng Mốc B
                    if (taskDate != null && !taskDate.before(today.getTime()) && !taskDate.after(next7Days.getTime())) {
                        next7DaysTasks.add(task); // Đủ điều kiện -> Nhặt vào giỏ!
                    }
                }
            } catch (Exception e) {
                // Nếu người dùng nhập sai định dạng ngày giờ (hoặc để trống), nó sẽ bỏ qua không bị sập App
                e.printStackTrace();
            }
        }

        // 4. Giao cái giỏ đã lọc xong cho Adapter hiển thị lên màn hình
        taskAdapter = new TaskAdapter(next7DaysTasks);
        recyclerView.setAdapter(taskAdapter);

        return view;
    }
}