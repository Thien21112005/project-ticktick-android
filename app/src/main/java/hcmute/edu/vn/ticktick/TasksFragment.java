package hcmute.edu.vn.ticktick;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hcmute.edu.vn.ticktick.database.DatabaseHelper;
import hcmute.edu.vn.ticktick.models.SubTask;

public class TasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        recyclerView = view.findViewById(R.id.recycler_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        databaseHelper = new DatabaseHelper(getContext());

        // 1. Lấy TẤT CẢ công việc từ kho
        List<SubTask> allSubTasks = databaseHelper.getAllSubTasksWithCategory();

        // 2. Tạo một danh sách (cái giỏ) trống chỉ để chứa việc của "Hôm nay"
        List<SubTask> todaySubTasks = new ArrayList<>();

        // 3. Lấy ngày hôm nay của điện thoại theo đúng định dạng lúc bạn lưu (d/m/yyyy)
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String todayDateStr = day + "/" + (month + 1) + "/" + year;

        // 4. Lọc: Đi dạo một vòng xem công việc nào bắt đầu bằng ngày hôm nay thì nhặt vào giỏ
        for (SubTask task : allSubTasks) {
            // Kiểm tra xem chuỗi thời gian (ví dụ "8/3/2026 10:05")
            // có bắt đầu bằng chuỗi ngày hôm nay không
            if (task.getStartDateTime() != null && task.getStartDateTime().startsWith(todayDateStr)) {
                todaySubTasks.add(task);
            }
        }

        // 5. Đưa danh sách ĐÃ LỌC cho "Người công nhân" (Adapter)
        taskAdapter = new TaskAdapter(todaySubTasks);
        recyclerView.setAdapter(taskAdapter);

        return view;
    }
}