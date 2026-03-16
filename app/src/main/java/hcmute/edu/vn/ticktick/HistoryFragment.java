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
import hcmute.edu.vn.ticktick.models.SubTask;

public class HistoryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Tái sử dụng giao diện danh sách có sẵn
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Gọi kho dữ liệu lấy các công việc ĐÃ XONG
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        List<SubTask> completedTasks = databaseHelper.getCompletedSubTasks();

        // Giao cho anh công nhân Adapter vẽ lên màn hình, kèm theo cái bộ đàm sửa công việc
        TaskAdapter taskAdapter = new TaskAdapter(completedTasks, new TaskAdapter.OnTaskEditListener() {
            @Override
            public void onEditClick(SubTask subTask) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showEditSubTaskDialog(subTask);
                }
            }
        });

        recyclerView.setAdapter(taskAdapter);
        return view;
    }
}