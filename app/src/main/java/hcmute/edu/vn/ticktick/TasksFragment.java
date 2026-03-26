package hcmute.edu.vn.ticktick;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.ticktick.database.DatabaseHelper;
import hcmute.edu.vn.ticktick.models.SubTask;

public class TasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private DatabaseHelper databaseHelper;

    // Khai báo một biến để giữ cái "giỏ gốc" (Toàn bộ việc hôm nay)
    private List<SubTask> originalTodayTasks = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        android.widget.LinearLayout layoutQuickAdd = view.findViewById(R.id.layout_quick_add);
        if (layoutQuickAdd != null) {
            layoutQuickAdd.setVisibility(View.GONE); // GONE nghĩa là biến mất hoàn toàn
        }

        // Ẩn thanh gạt xem công việc
        View layoutFilter = view.findViewById(R.id.layout_filter_switch);
        if (layoutFilter != null) {
            layoutFilter.setVisibility(View.GONE); // Ẩn thanh gạt đi luôn
        }

        recyclerView = view.findViewById(R.id.recycler_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        databaseHelper = new DatabaseHelper(getContext());

        // Ánh xạ cái nút gạt từ giao diện
        SwitchCompat switchFilter = view.findViewById(R.id.switch_filter_done);

        // 1. Lấy TẤT CẢ công việc từ kho
        List<SubTask> allSubTasks = databaseHelper.getAllSubTasksWithCategory();

        // 2. Tạo một danh sách (cái giỏ) trống chỉ để chứa việc của "Hôm nay"
        List<SubTask> todaySubTasks = new ArrayList<>();

        // 3. Lấy ngày hôm nay của điện thoại theo đúng định dạng lúc bạn lưu (d/m/yyyy)
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String todayDateStr = sdf.format(calendar.getTime());

        // 4. Lọc: Đi dạo một vòng xem công việc nào bắt đầu bằng ngày hôm nay thì nhặt vào giỏ
        for (SubTask task : allSubTasks) {
            // Kiểm tra xem chuỗi thời gian (ví dụ "8/3/2026 10:05")
            // có bắt đầu bằng chuỗi ngày hôm nay không
            if (task.getStartDateTime() != null && task.getStartDateTime().startsWith(todayDateStr)) {
                todaySubTasks.add(task);
            }
        }

        // 5. Đưa danh sách ĐÃ LỌC cho "Người công nhân" (Adapter)
        taskAdapter = new TaskAdapter(todaySubTasks, false, new TaskAdapter.OnTaskEditListener() {
            @Override
            public void onEditClick(SubTask subTask) {
                // Ép kiểu Activity hiện tại về MainActivity và gọi hàm hiển thị hộp thoại
                ((MainActivity) getActivity()).showEditSubTaskDialog(subTask);
            }
        });
        recyclerView.setAdapter(taskAdapter);

        // Lắng nghe sự kiện khi người dùng búng tay gạt nút
        switchFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Tạo một cái giỏ tạm để chứa kết quả lọc
            List<SubTask> filteredTasks = new ArrayList<>();

            if (isChecked) {
                // NẾU BẬT NÚT (true): Chỉ lấy những việc đã đánh dấu tick
                for (SubTask task : originalTodayTasks) {
                    if (task.isDone()) { // Hàm isDone() kiểm tra xem đã xong chưa [cite: 59]
                        filteredTasks.add(task); // Xong rồi thì nhặt vào giỏ tạm
                    }
                }
            } else {
                // NẾU TẮT NÚT (false): Lấy lại toàn bộ từ giỏ gốc
                filteredTasks.addAll(originalTodayTasks);
            }

            // Đưa giỏ tạm cho Adapter và bảo nó vẽ lại màn hình (gọi hàm ở Bước 2)
            taskAdapter.updateList(filteredTasks);
        });

        return view;
    }
}