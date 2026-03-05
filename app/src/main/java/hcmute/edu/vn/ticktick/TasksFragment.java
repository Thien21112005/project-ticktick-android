package hcmute.edu.vn.ticktick;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import hcmute.edu.vn.ticktick.database.DatabaseHelper;
import hcmute.edu.vn.ticktick.models.Task;

public class TasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // 1. Tìm cái băng chuyền trong giao diện
        recyclerView = view.findViewById(R.id.recycler_tasks);

        // 2. Cài đặt băng chuyền chạy theo chiều dọc (từ trên xuống)
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Lấy dữ liệu từ SQLite
        databaseHelper = new DatabaseHelper(getContext());
        List<Task> myTasks = databaseHelper.getAllTasks();

        // 4. Đưa dữ liệu cho "Người công nhân" (Adapter)
        taskAdapter = new TaskAdapter(myTasks);

        // 5. Gắn người công nhân vào băng chuyền
        recyclerView.setAdapter(taskAdapter);

        return view;
    }
}