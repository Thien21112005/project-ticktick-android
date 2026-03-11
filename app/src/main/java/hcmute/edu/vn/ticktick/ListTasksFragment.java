package hcmute.edu.vn.ticktick;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import hcmute.edu.vn.ticktick.database.DatabaseHelper;
import hcmute.edu.vn.ticktick.models.SubTask;

public class ListTasksFragment extends Fragment {

    private int taskId;
    private String taskName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Tái sử dụng lại layout cũ cho tiết kiệm thời gian
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // 1. Nhận gói hàng (Bundle) chứa ID và Tên danh mục từ MainActivity gửi qua
        if (getArguments() != null) {
            taskId = getArguments().getInt("TASK_ID");
            taskName = getArguments().getString("TASK_NAME");
        }

        // 2. Đổi tên Tiêu đề màn hình thành Tên của danh mục (VD: "Học bài")
        TextView tvTitle = view.findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText(taskName);
        }

        // 3. Cài đặt RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 4. Lấy dữ liệu từ Database theo ID và đưa lên màn hình
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        List<SubTask> subTasks = databaseHelper.getSubTasksByTaskId(taskId); // Gọi hàm vừa tạo ở Bước 1

        TaskAdapter taskAdapter = new TaskAdapter(subTasks);
        recyclerView.setAdapter(taskAdapter);

        return view;
    }
}