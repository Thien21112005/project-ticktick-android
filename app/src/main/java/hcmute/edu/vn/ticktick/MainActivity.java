package hcmute.edu.vn.ticktick;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

        setContentView(R.layout.activity_main);

        // Mặc định mở Today
        loadFragment(new TasksFragment());

        ImageView btnTask = findViewById(R.id.btn_nav_task);
        ImageView btnCalendar = findViewById(R.id.btn_nav_calendar);

        // --- 1. XỬ LÝ KHI BẤM NÚT TASK ---
        btnTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTask.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.red_primary));
                btnCalendar.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.icon_inactive));

                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                if (currentFragment instanceof CalendarFragment || currentFragment instanceof MenuFragment) {
                    openSelectedContent();
                } else {
                    loadFragment(new MenuFragment());
                }
            }
        });

        // --- 2. XỬ LÝ KHI BẤM NÚT LỊCH ---
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCalendar.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.red_primary));
                btnTask.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.icon_inactive));
                loadFragment(new CalendarFragment());
            }
        });

        // 3. XỬ LÝ NÚT THÊM CÔNG VIỆC (+) ĐỎ
        ImageView btnAddTask = findViewById(R.id.btn_add_task);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });
    }

    public void onMenuItemSelected(int selection) {
        currentMenuSelection = selection;
        openSelectedContent();
    }

    private void openSelectedContent() {
        if (currentMenuSelection == 0) {
            loadFragment(new TasksFragment());
        } else if (currentMenuSelection == 1) {
            loadFragment(new InboxFragment());
        } else if (currentMenuSelection == 2) {
            loadFragment(new Next7DaysFragment());
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

        // --- ĐOẠN CODE MỚI THÊM 1: Ánh xạ Menu xổ xuống của Nhắc nhở ---
        android.widget.AutoCompleteTextView autoCompleteNotify = dialog.findViewById(R.id.autoComplete_notify_before);
        // --------------------------------------------------------------

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
}