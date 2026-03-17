package hcmute.edu.vn.ticktick;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.ticktick.database.DatabaseHelper;
import hcmute.edu.vn.ticktick.models.SubTask;
import hcmute.edu.vn.ticktick.models.Task;

public class MainActivity extends AppCompatActivity {

    public int currentMenuSelection = 0;

    // Khai báo các biến UI mới để dùng chung trong class
    private DrawerLayout drawerLayout;
    private TextView tvMainTitle;
    private ImageView btnBottomToday, btnBottomCalendar, btnBottomProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationHelper.createNotificationChannel(this);

        // Xin quyền POST_NOTIFICATIONS (bắt buộc Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        DatabaseHelper db = new DatabaseHelper(this);
        SubTask test = new SubTask();
        test.setTaskId(1);
        test.setTitle("Học Android");
        test.setStartDateTime("17/03/2026 09:00");
        test.setDueDateTime("17/03/2026 10:30");
        test.setDone(false);
        test.setNotifyBefore(15);
        db.addSubTask(test);

        setContentView(R.layout.activity_main);

        // 1. ÁNH XẠ CÁC ID THEO GIAO DIỆN MỚI
        drawerLayout = findViewById(R.id.drawer_layout);
        tvMainTitle = findViewById(R.id.tv_main_title);
        ImageView btnOpenMenu = findViewById(R.id.btn_open_menu);

        btnBottomToday = findViewById(R.id.btn_bottom_today);
        btnBottomCalendar = findViewById(R.id.btn_bottom_calendar);
        btnBottomProfile = findViewById(R.id.btn_bottom_profile);
        // Mặc định ban đầu trên thanh nav với nút bằng 0
        updateBottomNavUI(0);

        ImageView btnAddTask = findViewById(R.id.btn_add_task);

        // 2. NHÚNG MENU VÀO NGĂN KÉO
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.menu_drawer_container, new MenuFragment())
                .commit();

        // 3. Mặc định mở Today
        loadFragment(new TasksFragment());
        tvMainTitle.setText("Hôm nay");

        // 4. XỬ LÝ NÚT MỞ MENU (Icon 3 gạch)
        btnOpenMenu.setOnClickListener(v -> {
            // Refresh MenuFragment trước khi mở drawer
            refreshMenuFragment();
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // 5. XỬ LÝ THANH ĐIỀU HƯỚNG BOTTOM NAV
        btnBottomToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuItemSelected(0);
                updateBottomNavUI(0); // Hiện viên thuốc ở Today
            }
        });

        btnBottomCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new CalendarFragment());
                tvMainTitle.setText("Lịch");
                currentMenuSelection = -1;
                updateBottomNavUI(1); // Hiện viên thuốc ở Lịch
            }
        });

        btnBottomProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Giả sử Fragment Profile/Inbox của bạn là index 1
                onMenuItemSelected(1);
                updateBottomNavUI(2); // Hiện viên thuốc ở Profile
            }
        });

        // 6. XỬ LÝ NÚT THÊM CÔNG VIỆC (+) ĐỎ
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });
    }

    public void onMenuItemSelected(int selection) {
        currentMenuSelection = selection;

        // Đóng menu ngăn kéo lại nếu nó đang mở
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        if (currentMenuSelection == 0) {
            loadFragment(new TasksFragment());
            tvMainTitle.setText("Hôm nay");
        }
        else if (currentMenuSelection == 1) {
            loadFragment(new ProfileFragment());
            tvMainTitle.setText("Profile");
        }
        else if (currentMenuSelection == 2) {
            loadFragment(new Next7DaysFragment());
            tvMainTitle.setText("7 Ngày Tới");
        }
        else if (currentMenuSelection == 3) {
            loadFragment(new HistoryFragment());
            tvMainTitle.setText("Lịch sử");
        }
    }

    private void openSelectedContent() {
        if (currentMenuSelection == 0) {
            loadFragment(new TasksFragment());
        }
        else if (currentMenuSelection == 1) {
            loadFragment(new ProfileFragment());
        }
        else if (currentMenuSelection == 2) {
            loadFragment(new Next7DaysFragment());
        }
        else if (currentMenuSelection == 3) {
            loadFragment(new HistoryFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    // --- HÀM HIỂN THỊ HỘP THOẠI THÊM CÔNG VIỆC (SUBTASK) ---
    private void showAddTaskDialog() {
        DatabaseHelper db = new DatabaseHelper(this);

        // Bước 1: Lấy toàn bộ danh sách "Thư mục" (Task) hiện có
        List<Task> allTasks = db.getAllTasks();

        // Nếu chưa có thư mục nào (như "Học bài"), nhắc người dùng tạo trước
        if (allTasks.isEmpty()) {
            Toast.makeText(this, "Vui lòng tạo Danh mục (Lists) ở Menu trước!", Toast.LENGTH_LONG).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_task);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Ánh xạ các thành phần giao diện
        Spinner spinnerCategory = dialog.findViewById(R.id.spinner_task_category);
        EditText edtTitle = dialog.findViewById(R.id.edt_task_title);
        TextView tvStartDate = dialog.findViewById(R.id.tv_start_date);
        TextView tvStartTime = dialog.findViewById(R.id.tv_start_time);
        Button btnSave = dialog.findViewById(R.id.btn_save_task);
        TextView tvDueDate = dialog.findViewById(R.id.tv_due_date);
        TextView tvDueTime = dialog.findViewById(R.id.tv_due_time);

        // Ánh xạ nút X
        ImageView btnClose = dialog.findViewById(R.id.btn_close_dialog);
        // Bắt sự kiện nút X để thoát
        btnClose.setOnClickListener(v -> {
            dialog.dismiss(); // Lệnh tắt hộp thoại ngay lập tức
        });

        // Ánh xạ Menu xổ xuống của Nhắc nhở
        android.widget.AutoCompleteTextView autoCompleteNotify = dialog.findViewById(R.id.autoComplete_notify_before);

        // Bước 2: Đổ tên các Danh mục vào Spinner (Menu thả xuống)
        List<String> taskNames = new ArrayList<>();
        for (Task t : allTasks) {
            taskNames.add(t.getTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, taskNames);
        spinnerCategory.setAdapter(adapter);

        // --- ĐOẠN CODE MỚI THÊM 2: Cài đặt dữ liệu và sự kiện cho Menu Nhắc nhở ---
        String[] notifyOptions = {"Không nhắc", "Trước 1 phút", "Trước 30 phút", "Trước 1 giờ", "Trước 1 ngày"};
        int[] notifyValues = {0, 1, 30, 60, 1440};

        ArrayAdapter<String> notifyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notifyOptions);
        autoCompleteNotify.setAdapter(notifyAdapter);
        autoCompleteNotify.setText(notifyOptions[0], false); // Mặc định hiển thị "Không nhắc"

        // Biến lưu trữ số phút nhắc nhở (dùng mảng 1 phần tử để truyền được vào bên trong sự kiện click)
        final int[] selectedNotifyValue = {0};

        autoCompleteNotify.setOnItemClickListener((parent, view, position, id) -> {
            // Lấy số phút tương ứng (ví dụ: chọn "Trước 10 phút" -> lấy số 10)
            selectedNotifyValue[0] = notifyValues[position];
        });
        // -------------------------------------------------------------------------

        // Xử lý Lịch và Đồng hồ
        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvDueDate.setOnClickListener(v -> showDatePicker(tvDueDate));
        tvDueTime.setOnClickListener(v -> showTimePicker(tvDueTime));

        // Bước 3: Khi bấm nút LƯU
        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String sDate = tvStartDate.getText().toString();
            String sTime = tvStartTime.getText().toString();

            if (title.isEmpty()) {
                Toast.makeText(this, "Hãy nhập tên công việc!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy ID của danh mục đã chọn từ Spinner
            int selectedPos = spinnerCategory.getSelectedItemPosition();
            int selectedTaskId = allTasks.get(selectedPos).getId();

            // Bước 4: Tạo đối tượng SubTask (Công việc chi tiết)
            SubTask newSubTask = new SubTask();
            newSubTask.setTaskId(selectedTaskId); // Khóa ngoại kết nối với Task
            newSubTask.setTitle(title);
            newSubTask.setStartDateTime(sDate + " " + sTime);
            newSubTask.setDone(false); // Mặc định là chưa xong

            // --- ĐOẠN CODE MỚI THÊM 3: Gắn số phút nhắc nhở vào đối tượng SubTask trước khi lưu ---
            newSubTask.setNotifyBefore(selectedNotifyValue[0]);
            // --------------------------------------------------------------------------------------
            String dDate = tvDueDate.getText().toString();
            String dTime = tvDueTime.getText().toString();
            // Chỉ lưu nếu người dùng đã chọn (không phải placeholder)
            if (!dDate.contains("Chọn") && !dTime.contains("Chọn")) {
                newSubTask.setDueDateTime(dDate + " " + dTime);
            }
            // Bước 5: Lưu vào bảng SubTask trong SQLite
            long newId = db.addSubTask(newSubTask);
            if (newId != -1) {
                newSubTask.setId((int) newId); // Gán ID vừa được sinh ra
                WorkManagerScheduler.scheduleNotification(MainActivity.this, newSubTask); // <-- LÊN LỊCH NHẮC
                Toast.makeText(this, "Đã thêm công việc!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            } else {
                Toast.makeText(this, "Lỗi khi lưu vào Database!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showDatePicker(TextView textView) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        int month = calendar.get(java.util.Calendar.MONTH);
        int year = calendar.get(java.util.Calendar.YEAR);

        android.app.DatePickerDialog datePicker = new android.app.DatePickerDialog(this, (view, y, m, d) -> {
            textView.setText(d + "/" + (m + 1) + "/" + y);
        }, year, month, day);
        datePicker.show();
    }

    private void showTimePicker(TextView textView) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = calendar.get(java.util.Calendar.MINUTE);

        android.app.TimePickerDialog timePicker = new android.app.TimePickerDialog(this, (view, h, m) -> {
            textView.setText(String.format("%02d:%02d", h, m));
        }, hour, minute, true);
        timePicker.show();
    }

    // --- HÀM MỞ MÀN HÌNH DANH SÁCH CÔNG VIỆC CỦA TỪNG DANH MỤC ---
    public void openListFragment(int taskId, String listName) {
        // Đặt một số bất kỳ (ví dụ số 3) để đánh dấu là không phải Today(0), Inbox(1) hay 7Days(2)
        currentMenuSelection = 3;

        tvMainTitle.setText(listName); // Đổi tiêu đề trên cùng thành tên danh mục

        // Đóng menu ngăn kéo lại
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        // Tạo một cái thùng (Bundle) để nhét ID và Tên vào
        Bundle bundle = new Bundle();
        bundle.putInt("TASK_ID", taskId);
        bundle.putString("TASK_NAME", listName);

        // Tạo Fragment mới và gắn cái thùng vào lưng nó
        ListTasksFragment fragment = new ListTasksFragment();
        fragment.setArguments(bundle);

        // Ra lệnh chuyển màn hình
        loadFragment(fragment);
    }

    // --- HÀM HIỂN THỊ HỘP THOẠI SỬA CÔNG VIỆC ---
    public void showEditSubTaskDialog(SubTask subTask) {
        DatabaseHelper db = new DatabaseHelper(this);
        List<Task> allTasks = db.getAllTasks();

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_task); // Dùng layout mới tạo ở Bước 2
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // 1. Ánh xạ các thành phần
        Spinner spinnerCategory = dialog.findViewById(R.id.spinner_task_category);
        EditText edtTitle = dialog.findViewById(R.id.edt_task_title);
        TextView tvStartDate = dialog.findViewById(R.id.tv_start_date);
        TextView tvStartTime = dialog.findViewById(R.id.tv_start_time);
        TextView tvDueDate = dialog.findViewById(R.id.tv_due_date);
        TextView tvDueTime = dialog.findViewById(R.id.tv_due_time);
        android.widget.AutoCompleteTextView autoCompleteNotify = dialog.findViewById(R.id.autoComplete_notify_before);
        Button btnUpdate = dialog.findViewById(R.id.btn_update_task); // Nút cập nhật
        Button btnDelete = dialog.findViewById(R.id.btn_delete_task); // Nút xóa

        // 2. Đổ dữ liệu các Danh mục vào Spinner và chọn đúng danh mục cũ
        List<String> taskNames = new ArrayList<>();
        int selectedIndex = 0;
        for (int i = 0; i < allTasks.size(); i++) {
            taskNames.add(allTasks.get(i).getTitle());
            // Nếu ID danh mục trùng với ID mà công việc này đang lưu thì ghi nhớ vị trí
            if (allTasks.get(i).getId() == subTask.getTaskId()) {
                selectedIndex = i;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, taskNames);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setSelection(selectedIndex); // Hiển thị đúng danh mục cũ

        // 3. Điền sẵn các thông tin cũ của công việc vào form
        edtTitle.setText(subTask.getTitle());

        // Tách ngày và giờ từ chuỗi startDateTime (Ví dụ: "8/3/2026 10:05" -> Tách thành "8/3/2026" và "10:05")
        if (subTask.getStartDateTime() != null && subTask.getStartDateTime().contains(" ")) {
            String[] parts = subTask.getStartDateTime().split(" ");
            tvStartDate.setText(parts[0]);
            tvStartTime.setText(parts[1]);
        }

        if (subTask.getDueDateTime() != null && subTask.getDueDateTime().contains(" ")) {
            String[] parts = subTask.getDueDateTime().split(" ");
            tvDueDate.setText(parts[0]);
            tvDueTime.setText(parts[1]);
        }

        // Cài đặt Nhắc nhở cũ
        String[] notifyOptions = {"Không nhắc", "Trước 1 phút", "Trước 30 phút", "Trước 1 giờ", "Trước 1 ngày"};
        int[] notifyValues = {0, 1, 30, 60, 1440};
        ArrayAdapter<String> notifyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notifyOptions);
        autoCompleteNotify.setAdapter(notifyAdapter);

        final int[] selectedNotifyValue = {subTask.getNotifyBefore()};
        for (int i = 0; i < notifyValues.length; i++) {
            if (notifyValues[i] == subTask.getNotifyBefore()) {
                autoCompleteNotify.setText(notifyOptions[i], false); // Điền thông số cũ
                break;
            }
        }

        autoCompleteNotify.setOnItemClickListener((parent, view, position, id) -> {
            selectedNotifyValue[0] = notifyValues[position];
        });

        // Xử lý Lịch và Đồng hồ (Giữ nguyên như cũ)
        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvDueDate.setOnClickListener(v -> showDatePicker(tvDueDate));
        tvDueTime.setOnClickListener(v -> showTimePicker(tvDueTime));

        // 4. BẮT SỰ KIỆN NÚT "CẬP NHẬT"
        btnUpdate.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Hãy nhập tên công việc!", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedPos = spinnerCategory.getSelectedItemPosition();
            int selectedTaskId = allTasks.get(selectedPos).getId();

            // Cập nhật lại đối tượng subTask
            subTask.setTaskId(selectedTaskId);
            subTask.setTitle(title);
            subTask.setStartDateTime(tvStartDate.getText().toString() + " " + tvStartTime.getText().toString());
            subTask.setNotifyBefore(selectedNotifyValue[0]);

            String dDate = tvDueDate.getText().toString();
            String dTime = tvDueTime.getText().toString();
            if (!dDate.contains("Chọn") && !dTime.contains("Chọn")) {
                subTask.setDueDateTime(dDate + " " + dTime);
            }

            // Lưu xuống SQLite
            db.updateSubTask(subTask);

            // Xóa lịch cũ và đặt lại lịch nhắc nhở mới
            WorkManagerScheduler.cancelNotification(this, subTask.getId());
            WorkManagerScheduler.scheduleNotification(this, subTask);

            Toast.makeText(this, "Đã cập nhật công việc!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            // Refresh lại màn hình hiện tại để thấy thay đổi (bằng hàm bạn đã viết)
            openSelectedContent();
        });

        // 5. BẮT SỰ KIỆN NÚT "XÓA BỎ"
        btnDelete.setOnClickListener(v -> {
            // Hiển thị một hộp thoại xác nhận nhỏ cho chắc chắn
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa công việc này?")
                    .setPositiveButton("Xóa", (d, which) -> {
                        db.deleteSubTask(subTask.getId()); // Xóa trong Database
                        WorkManagerScheduler.cancelNotification(this, subTask.getId()); // Hủy nhắc nhở
                        Toast.makeText(this, "Đã xóa công việc!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        openSelectedContent(); // Refresh lại danh sách
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        dialog.show();
    }

    // Hiệu ứng viên thuốc khi chọn btn trên thanh nav
    private void updateBottomNavUI(int position) {
        // Xóa toàn bộ nền cũ của 3 nút
        btnBottomToday.setBackgroundResource(0);
        btnBottomCalendar.setBackgroundResource(0);
        btnBottomProfile.setBackgroundResource(0);

        // Dựa vào vị trí đang chọn (0: Today, 1: Calendar, 2: Profile) để set nền mới
        if (position == 0) {
            btnBottomToday.setBackgroundResource(R.drawable.bg_nav_selected);
        } else if (position == 1) {
            btnBottomCalendar.setBackgroundResource(R.drawable.bg_nav_selected);
        } else if (position == 2) {
            btnBottomProfile.setBackgroundResource(R.drawable.bg_nav_selected);
        }
    }
    private void refreshMenuFragment() {
        MenuFragment menuFragment = (MenuFragment) getSupportFragmentManager()
                .findFragmentById(R.id.menu_drawer_container);

        if (menuFragment != null) {
            menuFragment.refreshProfileData();
        }
    }
}