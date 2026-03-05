package hcmute.edu.vn.ticktick;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import hcmute.edu.vn.ticktick.database.DatabaseHelper;
import hcmute.edu.vn.ticktick.models.Task;
public class MainActivity extends AppCompatActivity {

    public int currentMenuSelection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Mặc định mở Today
        loadFragment(new TasksFragment());

        // Kéo 2 nút ra từ giao diện (Chỉ khai báo ĐÚNG 1 LẦN ở đây)
        ImageView btnTask = findViewById(R.id.btn_nav_task);
        ImageView btnCalendar = findViewById(R.id.btn_nav_calendar);

        // --- 1. XỬ LÝ KHI BẤM NÚT TASK ---
        btnTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Đổi màu: Task -> Đỏ, Lịch -> Xám
                btnTask.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.red_primary));
                btnCalendar.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.icon_inactive));

                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                if (currentFragment instanceof CalendarFragment) {
                    openSelectedContent();
                } else if (currentFragment instanceof MenuFragment) {
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
                // Đổi màu: Lịch -> Đỏ, Task -> Xám
                btnCalendar.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.red_primary));
                btnTask.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.icon_inactive));

                // Mở trực tiếp trang Lịch
                loadFragment(new CalendarFragment());
            }
        });
        // 1. Tìm nút dấu +
        ImageView btnAddTask = findViewById(R.id.btn_add_task);

        // 2. Gắn sự kiện khi bấm nút +
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog(); // Gọi hàm mở hộp thoại
            }
        });
    }

    // Các hàm phụ trợ chuyển đổi màn hình
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
    // --- HÀM HIỂN THỊ HỘP THOẠI THÊM LỊCH ---
    private void showAddTaskDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_task);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Ánh xạ (Tìm các thành phần trong XML)
        EditText edtTitle = dialog.findViewById(R.id.edt_task_title);
        EditText edtDesc = dialog.findViewById(R.id.edt_task_desc);
        TextView tvStartDate = dialog.findViewById(R.id.tv_start_date);
        TextView tvStartTime = dialog.findViewById(R.id.tv_start_time);
        TextView tvEndDate = dialog.findViewById(R.id.tv_end_date);
        TextView tvEndTime = dialog.findViewById(R.id.tv_end_time);
        Button btnSave = dialog.findViewById(R.id.btn_save_task);

        // --- XỬ LÝ CHỌN NGÀY ---
        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvEndDate.setOnClickListener(v -> showDatePicker(tvEndDate));

        // --- XỬ LÝ CHỌN GIỜ ---
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));

        btnSave.setOnClickListener(v -> {
            // Lấy dữ liệu từ các ô nhập
            String title = edtTitle.getText().toString().trim();
            String desc = edtDesc.getText().toString().trim();
            String sDate = tvStartDate.getText().toString();
            String sTime = tvStartTime.getText().toString();
            String eDate = tvEndDate.getText().toString();
            String eTime = tvEndTime.getText().toString();

            if (title.isEmpty()) {
                Toast.makeText(this, "Hãy nhập tên công việc!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo đối tượng Task mới để lưu
            Task newTask = new Task();
            newTask.setTitle(title);
            newTask.setDescription(desc);
            newTask.setStartDate(sDate + " " + sTime); // Kết hợp ngày và giờ
            newTask.setEndDate(eDate + " " + eTime);

            // Lưu vào SQLite
            DatabaseHelper db = new DatabaseHelper(this);
            if (db.addTask(newTask) != -1) {
                Toast.makeText(this, "Thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    // --- HÀM HIỆN TỜ LỊCH (DatePicker) ---
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

    // --- HÀM HIỆN ĐỒNG HỒ (TimePicker) ---
    private void showTimePicker(TextView textView) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = calendar.get(java.util.Calendar.MINUTE);

        android.app.TimePickerDialog timePicker = new android.app.TimePickerDialog(this, (view, h, m) -> {
            textView.setText(String.format("%02d:%02d", h, m));
        }, hour, minute, true); // true là định dạng 24h
        timePicker.show();
    }
}