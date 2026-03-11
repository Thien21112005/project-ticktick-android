package hcmute.edu.vn.ticktick;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler; // Kéo thêm thư viện Handler để hẹn giờ
import android.os.Looper;  // Kéo thêm thư viện Looper
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

        // 1. TÌM ID CỦA CÁC NÚT VÀ CÁC THÀNH PHẦN BÊN TRONG (Icon, Text)
        LinearLayout btnToday = view.findViewById(R.id.menu_item_today);
        ImageView iconToday = view.findViewById(R.id.icon_today);
        TextView textToday = view.findViewById(R.id.text_today);

        LinearLayout btnNext7Days = view.findViewById(R.id.menu_item_next_7_days);
        ImageView icon7Days = view.findViewById(R.id.icon_7days);
        TextView text7Days = view.findViewById(R.id.text_7days);

        LinearLayout btnInbox = view.findViewById(R.id.menu_item_inbox);
        ImageView iconInbox = view.findViewById(R.id.icon_inbox);
        TextView textInbox = view.findViewById(R.id.text_inbox);

        ImageView btnAddList = view.findViewById(R.id.btn_add_list);
        layoutDynamicLists = view.findViewById(R.id.layout_dynamic_lists);

        // 2. BẮT SỰ KIỆN KHI BẤM VÀO CÁC MỤC (Gọi hàm hiệu ứng thay vì chuyển ngay lập tức)
        btnToday.setOnClickListener(v -> {
            if (mainActivity != null) animateAndSwitch(btnToday, iconToday, textToday, 0, mainActivity);
        });

        btnInbox.setOnClickListener(v -> {
            if (mainActivity != null) animateAndSwitch(btnInbox, iconInbox, textInbox, 1, mainActivity);
        });

        btnNext7Days.setOnClickListener(v -> {
            if (mainActivity != null) animateAndSwitch(btnNext7Days, icon7Days, text7Days, 2, mainActivity);
        });

        // 3. TẢI CÁC DANH MỤC (Học bài, Thể thao...) LÊN MENU
        loadListsToMenu();

        // 4. SỰ KIỆN KHI BẤM NÚT DẤU CỘNG Ở MỤC LISTS
        btnAddList.setOnClickListener(v -> showAddListDialog());

        return view;
    }

    // --- HÀM TẠO HIỆU ỨNG UX/UI KHI CHẠM NÚT ---
    private void animateAndSwitch(LinearLayout layout, ImageView icon, TextView text, int selectionIndex, MainActivity mainActivity) {
        // Bước 1: Đổi màu nền, màu icon và màu chữ sang tông đỏ ngay khi chạm vào
        layout.setBackgroundColor(Color.parseColor("#FFEBEE"));
        icon.setColorFilter(Color.parseColor("#FF5252"));
        text.setTextColor(Color.parseColor("#FF5252"));

        // Bước 2: Hẹn đồng hồ 200 mili-giây (0.2 giây)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // Bước 3: Đóng Menu và chuyển màn hình
            mainActivity.onMenuItemSelected(selectionIndex);

            // Bước 4: Dọn dẹp chiến trường - Trả lại màu cũ để lần sau mở Menu ra không bị lỗi màu
            TypedValue outValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            layout.setBackgroundResource(outValue.resourceId); // Trả lại hiệu ứng gợn sóng mặc định

            icon.clearColorFilter(); // Bỏ bộ lọc màu đỏ của icon
            text.setTextColor(Color.parseColor("#333333")); // Trả chữ về màu đen xám

        }, 200); // Bạn có thể sửa số 200 này to hơn nếu muốn hiệu ứng hiển thị lâu hơn
    }

    // --- HÀM TẢI DỮ LIỆU TỪ SQLITE LÊN MENU ---
    private void loadListsToMenu() {
        layoutDynamicLists.removeAllViews();
        List<Task> tasks = dbHelper.getAllTasks();

        for (Task task : tasks) {
            TextView tvList = new TextView(requireContext());
            tvList.setText("• " + task.getTitle());
            tvList.setTextSize(15f);
            tvList.setTextColor(Color.parseColor("#444444"));
            tvList.setPadding(0, 16, 0, 16);

            // BẮT SỰ KIỆN CLICK VÀ LÀM HIGHLIGHT
            tvList.setOnClickListener(v -> {
                // UX/UI: Đổi nền sang đỏ nhạt và chữ đỏ ngay lập tức để báo hiệu đã bấm trúng
                tvList.setBackgroundColor(Color.parseColor("#FFEBEE"));
                tvList.setTextColor(Color.parseColor("#FF5252"));

                // Hẹn giờ 0.2s y hệt như các nút Today, Inbox...
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Trả lại màu chữ cũ để lát mở Menu ra nó không bị kẹt màu đỏ mãi
                    tvList.setBackgroundColor(Color.TRANSPARENT);
                    tvList.setTextColor(Color.parseColor("#444444"));

                    // Gọi Tổng đài MainActivity chuyển màn hình
                    MainActivity mainActivity = (MainActivity) getActivity();
                    if (mainActivity != null) {
                        mainActivity.openListFragment(task.getId(), task.getTitle());
                    }
                }, 200);
            });

            // CÀI ĐẶT UX: NHẤN GIỮ LÂU HIỆN MENU SỬA/XÓA
            tvList.setOnLongClickListener(v -> {
                // Tạo 2 lựa chọn cho người dùng
                CharSequence[] options = new CharSequence[]{"Sửa tên", "Xóa danh mục"};

                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Tùy chọn: " + task.getTitle())
                        .setItems(options, (dialog, which) -> {
                            if (which == 0) {
                                // Người dùng bấm mục đầu tiên (Sửa tên) -> Gọi hàm Sửa
                                showEditNameDialog(task);
                            } else if (which == 1) {
                                // Người dùng bấm mục thứ hai (Xóa) -> Gọi hàm Xóa
                                showDeleteConfirmDialog(task);
                            }
                        })
                        .show();
                return true;
            });

            layoutDynamicLists.addView(tvList);
        }
    }

    // --- HÀM HIỂN THỊ HỘP THOẠI ĐỔI TÊN ---
    private void showEditNameDialog(Task task) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Đổi tên danh mục");

        // UX: Dùng code Java tự tạo ra một ô nhập liệu (EditText) và điền sẵn tên cũ vào
        final EditText input = new EditText(requireContext());
        input.setText(task.getTitle());
        input.setPadding(40, 40, 40, 40); // Căn lề cho đẹp
        builder.setView(input);

        // Nút Lưu
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                dbHelper.updateTaskName(task.getId(), newName); // Cập nhật Database
                Toast.makeText(requireContext(), "Đã đổi tên thành công!", Toast.LENGTH_SHORT).show();
                loadListsToMenu(); // Vẽ lại giao diện Menu
            } else {
                Toast.makeText(requireContext(), "Tên không được để trống!", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút Hủy
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // --- HÀM HIỂN THỊ HỘP THOẠI XÁC NHẬN XÓA ---
    private void showDeleteConfirmDialog(Task task) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Cảnh báo")
                .setMessage("Bạn có chắc chắn muốn xóa '" + task.getTitle() + "' không? Toàn bộ công việc bên trong cũng sẽ bị mất!")
                .setPositiveButton("Xóa đỏ", (dialog, which) -> {
                    dbHelper.deleteTask(task.getId());
                    Toast.makeText(requireContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
                    loadListsToMenu();
                })
                .setNegativeButton("Hủy", null)
                .show();
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