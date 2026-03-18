package hcmute.edu.vn.ticktick;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.ticktick.database.DatabaseHelper;
import hcmute.edu.vn.ticktick.models.SubTask;

public class ListTasksFragment extends Fragment {

    private int taskId;
    private String taskName;

    // Khai báo các biến UI và Database ở cấp class để dùng lại được nhiều lần
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private DatabaseHelper databaseHelper;
    private EditText edtQuickTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // 1. Nhận dữ liệu ID danh mục từ MainActivity
        if (getArguments() != null) {
            taskId = getArguments().getInt("TASK_ID");
            taskName = getArguments().getString("TASK_NAME");
        }

        // 2. Ánh xạ các UI
        recyclerView = view.findViewById(R.id.recycler_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        databaseHelper = new DatabaseHelper(getContext());

        edtQuickTask = view.findViewById(R.id.edt_quick_task_title);
        ImageView btnQuickAdd = view.findViewById(R.id.btn_quick_add);

        // 3. Tải danh sách công việc lần đầu tiên lên màn hình
        loadTasksToScreen();

        // 4. BẮT SỰ KIỆN KHI BẤM NÚT DẤU CỘNG
        btnQuickAdd.setOnClickListener(v -> {
            // Lấy chữ người dùng vừa gõ và xóa khoảng trắng dư thừa
            String title = edtQuickTask.getText().toString().trim();

            // Nếu chưa gõ gì mà bấm thì nhắc nhở
            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập công việc!", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- LẤY NGÀY GIỜ HIỆN TẠI THEO CHUẨN CỦA BẠN ---
            // Định dạng này giống hệt thuật toán lọc bạn viết ở Next7DaysFragment
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String currentDateTime = sdf.format(new Date());

            // --- TẠO CÔNG VIỆC MỚI ---
            SubTask newSubTask = new SubTask();
            newSubTask.setTaskId(taskId); // Bắt buộc phải có để biết nó thuộc danh mục nào
            newSubTask.setTitle(title);
            newSubTask.setStartDateTime(currentDateTime); // Gán ngày giờ vừa lấy
            newSubTask.setDone(false); // Mặc định là chưa làm xong
            newSubTask.setNotifyBefore(0); // 0 = Không nhắc nhở

            // --- LƯU VÀO DATABASE VÀ CẬP NHẬT GIAO DIỆN ---
            long result = databaseHelper.addSubTask(newSubTask);

            if (result != -1) {
                // Xóa chữ trong ô nhập đi để người dùng gõ việc tiếp theo
                edtQuickTask.setText("");
                // Tải lại danh sách để thấy ngay công việc vừa thêm
                loadTasksToScreen();
            } else {
                Toast.makeText(getContext(), "Lỗi khi lưu!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // Viết một hàm riêng để Tải dữ liệu, giúp code gọn và dễ tái sử dụng
    private void loadTasksToScreen() {
        // Lấy danh sách từ kho dựa theo ID
        List<SubTask> subTasks = databaseHelper.getSubTasksByTaskId(taskId);
        // Gắn vào Adapter
        TaskAdapter taskAdapter = new TaskAdapter(subTasks, false, new TaskAdapter.OnTaskEditListener() {
            @Override
            public void onEditClick(SubTask subTask) {
                // Kiểm tra xem Tổng đài có đang hoạt động không, nếu có thì gọi hàm showEditSubTaskDialog
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showEditSubTaskDialog(subTask);
                }
            }
        });
        recyclerView.setAdapter(taskAdapter);
    }
}