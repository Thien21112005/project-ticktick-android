package hcmute.edu.vn.ticktick;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlarmManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import java.util.Locale;

import hcmute.edu.vn.ticktick.database.DatabaseHelper;
import hcmute.edu.vn.ticktick.models.SubTask;
import hcmute.edu.vn.ticktick.models.Task;

public class MainActivity extends AppCompatActivity {

    public int currentMenuSelection = 0;

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

        // Xin quyền đặt alarm chính xác (bắt buộc Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        tvMainTitle = findViewById(R.id.tv_main_title);
        ImageView btnOpenMenu = findViewById(R.id.btn_open_menu);

        btnBottomToday = findViewById(R.id.btn_bottom_today);
        btnBottomCalendar = findViewById(R.id.btn_bottom_calendar);
        btnBottomProfile = findViewById(R.id.btn_bottom_profile);
        updateBottomNavUI(0);

        ImageView btnAddTask = findViewById(R.id.btn_add_task);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.menu_drawer_container, new MenuFragment())
                .commit();

        loadFragment(new TasksFragment());
        tvMainTitle.setText("Hôm nay");

        btnOpenMenu.setOnClickListener(v -> {
            refreshMenuFragment();
            drawerLayout.openDrawer(GravityCompat.START);
        });

        btnBottomToday.setOnClickListener(v -> {
            onMenuItemSelected(0);
            updateBottomNavUI(0);
        });

        btnBottomCalendar.setOnClickListener(v -> {
            loadFragment(new CalendarFragment());
            tvMainTitle.setText("Lịch");
            currentMenuSelection = -1;
            updateBottomNavUI(1);
        });

        btnBottomProfile.setOnClickListener(v -> {
            onMenuItemSelected(1);
            updateBottomNavUI(2);
        });

        btnAddTask.setOnClickListener(v -> showAddTaskDialog());
    }

    public void onMenuItemSelected(int selection) {
        currentMenuSelection = selection;

        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        if (currentMenuSelection == 0) {
            loadFragment(new TasksFragment());
            tvMainTitle.setText("Hôm nay");
        } else if (currentMenuSelection == 1) {
            loadFragment(new ProfileFragment());
            tvMainTitle.setText("Profile");
        } else if (currentMenuSelection == 2) {
            loadFragment(new Next7DaysFragment());
            tvMainTitle.setText("7 Ngày Tới");
        } else if (currentMenuSelection == 3) {
            loadFragment(new HistoryFragment());
            tvMainTitle.setText("Lịch sử");
        }
    }

    private void openSelectedContent() {
        if (currentMenuSelection == 0) {
            loadFragment(new TasksFragment());
        } else if (currentMenuSelection == 1) {
            loadFragment(new ProfileFragment());
        } else if (currentMenuSelection == 2) {
            loadFragment(new Next7DaysFragment());
        } else if (currentMenuSelection == 3) {
            loadFragment(new HistoryFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void showAddTaskDialog() {
        DatabaseHelper db = new DatabaseHelper(this);
        List<Task> allTasks = db.getAllTasks();

        if (allTasks.isEmpty()) {
            Toast.makeText(this, "Vui lòng tạo Danh mục (Lists) ở Menu trước!", Toast.LENGTH_LONG).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_task);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Spinner spinnerCategory = dialog.findViewById(R.id.spinner_task_category);
        EditText edtTitle = dialog.findViewById(R.id.edt_task_title);
        TextView tvStartDate = dialog.findViewById(R.id.tv_start_date);
        TextView tvStartTime = dialog.findViewById(R.id.tv_start_time);
        TextView tvDueDate = dialog.findViewById(R.id.tv_due_date);
        TextView tvDueTime = dialog.findViewById(R.id.tv_due_time);
        Button btnSave = dialog.findViewById(R.id.btn_save_task);
        ImageView btnClose = dialog.findViewById(R.id.btn_close_dialog);
        android.widget.AutoCompleteTextView autoCompleteNotify = dialog.findViewById(R.id.autoComplete_notify_before);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        List<String> taskNames = new ArrayList<>();
        for (Task t : allTasks) taskNames.add(t.getTitle());
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, taskNames));

        String[] notifyOptions = {"Không nhắc", "Trước 1 phút", "Trước 30 phút", "Trước 1 giờ", "Trước 1 ngày"};
        int[] notifyValues = {0, 1, 30, 60, 1440};
        autoCompleteNotify.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notifyOptions));
        autoCompleteNotify.setText(notifyOptions[0], false);

        final int[] selectedNotifyValue = {0};
        autoCompleteNotify.setOnItemClickListener((parent, view, position, id) ->
                selectedNotifyValue[0] = notifyValues[position]);

        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvDueDate.setOnClickListener(v -> showDatePicker(tvDueDate));
        tvDueTime.setOnClickListener(v -> showTimePicker(tvDueTime));

        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Hãy nhập tên công việc!", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedTaskId = allTasks.get(spinnerCategory.getSelectedItemPosition()).getId();

            SubTask newSubTask = new SubTask();
            newSubTask.setTaskId(selectedTaskId);
            newSubTask.setTitle(title);
            newSubTask.setStartDateTime(tvStartDate.getText().toString() + " " + tvStartTime.getText().toString());
            newSubTask.setDone(false);
            newSubTask.setNotifyBefore(selectedNotifyValue[0]);

            String dDate = tvDueDate.getText().toString();
            String dTime = tvDueTime.getText().toString();
            if (!dDate.contains("Chọn") && !dTime.contains("Chọn")) {
                newSubTask.setDueDateTime(dDate + " " + dTime);
            }

            long newId = db.addSubTask(newSubTask);
            if (newId != -1) {
                newSubTask.setId((int) newId);
                AlarmScheduler.scheduleNotification(MainActivity.this, newSubTask);
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
        android.app.DatePickerDialog datePicker = new android.app.DatePickerDialog(this, (view, y, m, d) -> {
            textView.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y));
        },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePicker.show();
    }

    private void showTimePicker(TextView textView) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        new android.app.TimePickerDialog(this, (view, h, m) ->
                textView.setText(String.format("%02d:%02d", h, m)),
                calendar.get(java.util.Calendar.HOUR_OF_DAY),
                calendar.get(java.util.Calendar.MINUTE),
                true).show();
    }

    public void openListFragment(int taskId, String listName) {
        currentMenuSelection = 3;
        tvMainTitle.setText(listName);

        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("TASK_ID", taskId);
        bundle.putString("TASK_NAME", listName);

        ListTasksFragment fragment = new ListTasksFragment();
        fragment.setArguments(bundle);
        loadFragment(fragment);
    }

    public void showEditSubTaskDialog(SubTask subTask) {
        DatabaseHelper db = new DatabaseHelper(this);
        List<Task> allTasks = db.getAllTasks();

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_task);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Spinner spinnerCategory = dialog.findViewById(R.id.spinner_task_category);
        EditText edtTitle = dialog.findViewById(R.id.edt_task_title);
        TextView tvStartDate = dialog.findViewById(R.id.tv_start_date);
        TextView tvStartTime = dialog.findViewById(R.id.tv_start_time);
        TextView tvDueDate = dialog.findViewById(R.id.tv_due_date);
        TextView tvDueTime = dialog.findViewById(R.id.tv_due_time);
        android.widget.AutoCompleteTextView autoCompleteNotify = dialog.findViewById(R.id.autoComplete_notify_before);
        Button btnUpdate = dialog.findViewById(R.id.btn_update_task);
        Button btnDelete = dialog.findViewById(R.id.btn_delete_task);

        List<String> taskNames = new ArrayList<>();
        int selectedIndex = 0;
        for (int i = 0; i < allTasks.size(); i++) {
            taskNames.add(allTasks.get(i).getTitle());
            if (allTasks.get(i).getId() == subTask.getTaskId()) selectedIndex = i;
        }
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, taskNames));
        spinnerCategory.setSelection(selectedIndex);

        edtTitle.setText(subTask.getTitle());

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

        String[] notifyOptions = {"Không nhắc", "Trước 1 phút", "Trước 30 phút", "Trước 1 giờ", "Trước 1 ngày"};
        int[] notifyValues = {0, 1, 30, 60, 1440};
        autoCompleteNotify.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notifyOptions));

        final int[] selectedNotifyValue = {subTask.getNotifyBefore()};
        for (int i = 0; i < notifyValues.length; i++) {
            if (notifyValues[i] == subTask.getNotifyBefore()) {
                autoCompleteNotify.setText(notifyOptions[i], false);
                break;
            }
        }
        autoCompleteNotify.setOnItemClickListener((parent, view, position, id) ->
                selectedNotifyValue[0] = notifyValues[position]);

        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvDueDate.setOnClickListener(v -> showDatePicker(tvDueDate));
        tvDueTime.setOnClickListener(v -> showTimePicker(tvDueTime));

        btnUpdate.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Hãy nhập tên công việc!", Toast.LENGTH_SHORT).show();
                return;
            }

            subTask.setTaskId(allTasks.get(spinnerCategory.getSelectedItemPosition()).getId());
            subTask.setTitle(title);
            subTask.setStartDateTime(tvStartDate.getText().toString() + " " + tvStartTime.getText().toString());
            subTask.setNotifyBefore(selectedNotifyValue[0]);

            String dDate = tvDueDate.getText().toString();
            String dTime = tvDueTime.getText().toString();
            if (!dDate.contains("Chọn") && !dTime.contains("Chọn")) {
                subTask.setDueDateTime(dDate + " " + dTime);
            }

            db.updateSubTask(subTask);

            AlarmScheduler.cancelNotification(this, subTask.getId());
            AlarmScheduler.scheduleNotification(this, subTask);

            Toast.makeText(this, "Đã cập nhật công việc!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            openSelectedContent();
        });

        btnDelete.setOnClickListener(v ->
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa công việc này?")
                        .setPositiveButton("Xóa", (d, which) -> {
                            db.deleteSubTask(subTask.getId());
                            AlarmScheduler.cancelNotification(this, subTask.getId());
                            Toast.makeText(this, "Đã xóa công việc!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            openSelectedContent();
                        })
                        .setNegativeButton("Hủy", null)
                        .show());

        dialog.show();
    }

    private void updateBottomNavUI(int position) {
        btnBottomToday.setBackgroundResource(0);
        btnBottomCalendar.setBackgroundResource(0);
        btnBottomProfile.setBackgroundResource(0);

        if (position == 0) btnBottomToday.setBackgroundResource(R.drawable.bg_nav_selected);
        else if (position == 1) btnBottomCalendar.setBackgroundResource(R.drawable.bg_nav_selected);
        else if (position == 2) btnBottomProfile.setBackgroundResource(R.drawable.bg_nav_selected);
    }

    private void refreshMenuFragment() {
        MenuFragment menuFragment = (MenuFragment) getSupportFragmentManager()
                .findFragmentById(R.id.menu_drawer_container);
        if (menuFragment != null) menuFragment.refreshProfileData();
    }
}