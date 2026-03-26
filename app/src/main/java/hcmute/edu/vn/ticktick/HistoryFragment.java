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

        // Ẩn thanh nhập tên công việc
        View inputLayout = view.findViewById(R.id.layout_quick_add);
        if (inputLayout != null) {
            inputLayout.setVisibility(View.GONE); // Ẩn thanh nhập đi
        }

        // Ẩn thanh gạt xem công việc đã hoàn thành
        View layoutFilter = view.findViewById(R.id.layout_filter_switch);
        if (layoutFilter != null) {
            layoutFilter.setVisibility(View.GONE); // Ẩn thanh gạt đi
        }

        RecyclerView recyclerView = view.findViewById(R.id.recycler_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Gọi kho dữ liệu lấy các công việc ĐÃ XONG
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        List<SubTask> completedTasks = databaseHelper.getCompletedSubTasks();

        // CHÚ Ý Ở ĐÂY: Truyền thêm chữ "true" vào giữa để báo hiệu đây là màn hình Lịch sử
        TaskAdapter taskAdapter = new TaskAdapter(completedTasks, true, new TaskAdapter.OnTaskEditListener() {
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