package hcmute.edu.vn.ticktick;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.util.List;

import hcmute.edu.vn.ticktick.database.DatabaseHelper;
import hcmute.edu.vn.ticktick.models.Task;

public class MenuFragment extends Fragment {

    private LinearLayout layoutDynamicLists; // Khai báo "giá sách"
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();

        dbHelper = new DatabaseHelper(requireContext());

        // ... (Giữ nguyên các đoạn tìm ID btnToday, btnInbox... và cấu hình màu sắc của bạn) ...

        // 1. TÌM NÚT DẤU CỘNG VÀ CÁI GIÁ SÁCH
        ImageView btnAddList = view.findViewById(R.id.btn_add_list);
        layoutDynamicLists = view.findViewById(R.id.layout_dynamic_lists);

        // 2. TẢI CÁC DANH MỤC LÊN MENU KHI VỪA MỞ APP
        loadListsToMenu();

        // 3. SỰ KIỆN KHI BẤM NÚT DẤU CỘNG Ở MỤC LISTS
        btnAddList.setOnClickListener(v -> showAddListDialog());

        return view;
    }

    // --- HÀM TẢI DỮ LIỆU TỪ SQLITE LÊN MENU ---
    private void loadListsToMenu() {
        // Xóa sạch các mục cũ đi trước khi xếp lại để không bị trùng lặp
        layoutDynamicLists.removeAllViews();

        // Lấy danh sách từ Database
        List<Task> tasks = dbHelper.getAllTasks();

        // Duyệt qua từng danh mục (Học bài, Thể thao...)
        for (Task task : tasks) {
            // Dùng code Java để "đúc" ra một dòng TextView mới
            TextView tvList = new TextView(requireContext());
            tvList.setText("• " + task.getTitle()); // Thêm dấu chấm cho đẹp
            tvList.setTextSize(15f);
            tvList.setTextColor(Color.parseColor("#444444"));
            tvList.setPadding(0, 16, 0, 16); // Tạo khoảng cách trên dưới cho dễ bấm

            // Xếp nó lên "giá sách"
            layoutDynamicLists.addView(tvList);

            // Nếu bạn muốn bấm vào "Học bài" để mở ra danh sách việc học,
            // có thể thiết lập sự kiện onClick tại đây (chúng ta sẽ làm sau)
            // tvList.setOnClickListener(v -> { ... });
        }
    }

    // --- HÀM MỞ HỘP THOẠI THÊM DANH MỤC ---
    private void showAddListDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_list);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText edtName = dialog.findViewById(R.id.edt_list_name);
        Button btnSave = dialog.findViewById(R.id.btn_save_list);

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập tên danh mục!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu vào bảng Task
            Task newTask = new Task();
            newTask.setTitle(name);
            long result = dbHelper.addTask(newTask);

            if (result != -1) {
                Toast.makeText(requireContext(), "Đã thêm danh mục!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                // LƯU THÀNH CÔNG THÌ GỌI LẠI HÀM NÀY ĐỂ VẼ LẠI GIAO DIỆN
                loadListsToMenu();
            }
        });

        dialog.show();
    }
}