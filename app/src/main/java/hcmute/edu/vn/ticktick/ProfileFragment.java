package hcmute.edu.vn.ticktick;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.ticktick.database.DatabaseHelper;
import hcmute.edu.vn.ticktick.models.SubTask;

public class ProfileFragment extends Fragment {

    private static final String PREF_NAME = "UserProfilePrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_JOIN_DATE = "join_date";
    private static final String KEY_RINGTONE_URI = "ringtone_uri";
    private static final String AVATAR_FILE_NAME = "profile_avatar.jpg";

    private ShapeableImageView imgAvatar;
    private TextView tvUsernameDisplay, tvEmailDisplay, tvJoinDate, tvSelectedRingtone;
    private TextView tvTotalTasks, tvCompletedTasks, tvCompletionRate;
    private TextView tvWeekCount, tvMonthCount;
    private ProgressBar progressWeek, progressMonth;
    private EditText edtUsername, edtEmail;
    private Button btnChangeRingtone;

    private SharedPreferences prefs;
    private DatabaseHelper dbHelper;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        dbHelper = new DatabaseHelper(requireContext());

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleSelectedImage);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgAvatar = view.findViewById(R.id.img_avatar);
        tvUsernameDisplay = view.findViewById(R.id.tv_username_display);
        edtUsername = view.findViewById(R.id.edt_username);
        tvEmailDisplay = view.findViewById(R.id.tv_email_display);
        edtEmail = view.findViewById(R.id.edt_email);
        tvJoinDate = view.findViewById(R.id.tv_join_date);
        tvSelectedRingtone = view.findViewById(R.id.tv_selected_ringtone);
        btnChangeRingtone = view.findViewById(R.id.btn_change_ringtone);

        tvTotalTasks = view.findViewById(R.id.tv_total_tasks);
        tvCompletedTasks = view.findViewById(R.id.tv_completed_tasks);
        tvCompletionRate = view.findViewById(R.id.tv_completion_rate);
        tvWeekCount = view.findViewById(R.id.tv_week_count);
        tvMonthCount = view.findViewById(R.id.tv_month_count);
        progressWeek = view.findViewById(R.id.progress_week);
        progressMonth = view.findViewById(R.id.progress_month);

        loadProfileData();
        loadStatistics();

        // Sửa tên
        tvUsernameDisplay.setOnClickListener(v -> enterEditMode(edtUsername, tvUsernameDisplay));
        edtUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) exitEditMode(edtUsername, tvUsernameDisplay, KEY_USERNAME);
        });

        // Sửa email
        tvEmailDisplay.setOnClickListener(v -> enterEditMode(edtEmail, tvEmailDisplay));
        edtEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) exitEditMode(edtEmail, tvEmailDisplay, KEY_EMAIL);
        });

        // Đổi avatar
        imgAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        view.findViewById(R.id.btn_change_avatar).setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Chọn nhạc thông báo
        loadSelectedRingtone();
        btnChangeRingtone.setOnClickListener(v -> pickRingtone());

        // Xuất dữ liệu
        view.findViewById(R.id.tv_export_data).setOnClickListener(v -> exportTasksToCSV());

        // Chia sẻ app
        view.findViewById(R.id.tv_share_app).setOnClickListener(v -> shareApp());

        // Đăng xuất (quay về splash?)
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            // Xoá SharedPreferences (tuỳ ý)
            prefs.edit().clear().apply();
            Intent intent = new Intent(requireContext(), SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadProfileData() {
        String username = prefs.getString(KEY_USERNAME, "Nguyễn Ngọc Thiện");
        String email = prefs.getString(KEY_EMAIL, "example@gmail.com");
        String joinDate = prefs.getString(KEY_JOIN_DATE, null);

        tvUsernameDisplay.setText(username);
        edtUsername.setText(username);
        tvEmailDisplay.setText(email);
        edtEmail.setText(email);

        if (joinDate == null) {
            joinDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            prefs.edit().putString(KEY_JOIN_DATE, joinDate).apply();
        }
        tvJoinDate.setText(joinDate);

        loadAvatarFromFile();
    }

    private void loadStatistics() {
        List<SubTask> allTasks = dbHelper.getAllSubTasksWithCategory();
        List<SubTask> completedTasks = dbHelper.getCompletedSubTasks();

        int total = allTasks.size();
        int completed = completedTasks.size();
        double rate = total == 0 ? 0 : (double) completed / total * 100;

        tvTotalTasks.setText(String.valueOf(total));
        tvCompletedTasks.setText(String.valueOf(completed));
        tvCompletionRate.setText(String.format(Locale.getDefault(), "%.1f%%", rate));

        // Thống kê tuần (từ 0h 7 ngày trước đến 23:59:59 hôm nay)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis(); // không dùng nhưng để tham khảo

        // Tính 7 ngày trước (0h)
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long weekAgoStart = cal.getTimeInMillis();

        // Tính cuối ngày hôm nay (23:59:59)
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long todayEnd = cal.getTimeInMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        int tasksThisWeek = 0;
        int tasksThisMonth = 0;

        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);

        for (SubTask task : allTasks) {
            try {
                if (task.getStartDateTime() != null && !task.getStartDateTime().isEmpty()) {
                    Date startDate = sdf.parse(task.getStartDateTime());
                    if (startDate != null) {
                        long time = startDate.getTime();
                        // Điều kiện: từ 0h 7 ngày trước đến hết ngày hôm nay
                        if (time >= weekAgoStart && time <= todayEnd) {
                            tasksThisWeek++;
                        }
                        Calendar taskCal = Calendar.getInstance();
                        taskCal.setTime(startDate);
                        if (taskCal.get(Calendar.MONTH) == currentMonth && taskCal.get(Calendar.YEAR) == currentYear) {
                            tasksThisMonth++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        tvWeekCount.setText(String.valueOf(tasksThisWeek));
        tvMonthCount.setText(String.valueOf(tasksThisMonth));

        // Tính tỷ lệ phần trăm so với tổng (nếu muốn hiển thị trên progress)
        int max = total == 0 ? 1 : total;
        int weekPercent = (int) ((double) tasksThisWeek / max * 100);
        int monthPercent = (int) ((double) tasksThisMonth / max * 100);
        progressWeek.setProgress(weekPercent);
        progressMonth.setProgress(monthPercent);
    }

    private void exportTasksToCSV() {
        List<SubTask> allTasks = dbHelper.getAllSubTasksWithCategory();
        if (allTasks.isEmpty()) {
            Toast.makeText(getContext(), "Không có dữ liệu để xuất", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Danh mục,Tên công việc,Thời gian bắt đầu,Thời hạn,Đã hoàn thành,Nhắc nhở trước (phút)\n");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        for (SubTask task : allTasks) {
            csv.append(task.getId()).append(",");
            csv.append(escapeCSV(task.getTaskName())).append(",");
            csv.append(escapeCSV(task.getTitle())).append(",");
            csv.append(task.getStartDateTime() != null ? task.getStartDateTime() : "").append(",");
            csv.append(task.getDueDateTime() != null ? task.getDueDateTime() : "").append(",");
            csv.append(task.isDone() ? "Đã xong" : "Chưa xong").append(",");
            csv.append(task.getNotifyBefore()).append("\n");
        }

        try {
            File exportDir = new File(requireContext().getExternalFilesDir(null), "exports");
            if (!exportDir.exists()) exportDir.mkdirs();

            String fileName = "TickTick_Export_" + sdf.format(new Date()).replace("/", "-").replace(" ", "_") + ".csv";
            File file = new File(exportDir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(csv.toString().getBytes());
            fos.close();

            Toast.makeText(getContext(), "Đã xuất ra: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Mở file để xem
            Uri fileUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", file);
            Intent shareIntent = new Intent(Intent.ACTION_VIEW);
            shareIntent.setDataAndType(fileUri, "text/csv");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Xem file CSV"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi xuất file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String escapeCSV(String str) {
        if (str == null) return "";
        if (str.contains(",") || str.contains("\"")) {
            str = str.replace("\"", "\"\"");
            return "\"" + str + "\"";
        }
        return str;
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hãy thử ứng dụng TickTick – quản lý công việc thông minh!\nhttps://play.google.com/store/apps/details?id=" + requireContext().getPackageName());
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ ứng dụng"));
    }

    // ---------- Các hàm xử lý avatar, ringtone, edit giữ nguyên ----------
    private void handleSelectedImage(Uri uri) {
        if (uri == null) return;
        try {
            saveImageToInternalStorage(uri);
            loadAvatarFromFile();
            Toast.makeText(requireContext(), "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không thể lưu ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToInternalStorage(Uri uri) throws Exception {
        InputStream is = requireContext().getContentResolver().openInputStream(uri);
        File file = new File(requireContext().getFilesDir(), AVATAR_FILE_NAME);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Bitmap bmp = BitmapFactory.decodeStream(is);
            if (bmp != null) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            } else {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) fos.write(buffer, 0, len);
            }
        }
    }

    private void loadAvatarFromFile() {
        File file = new File(requireContext().getFilesDir(), AVATAR_FILE_NAME);
        if (file.exists()) {
            imgAvatar.setImageURI(Uri.fromFile(file));
        }
    }

    private void enterEditMode(EditText edt, TextView tv) {
        tv.setVisibility(View.GONE);
        edt.setVisibility(View.VISIBLE);
        edt.requestFocus();
        edt.setSelection(edt.getText().length());
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edt, InputMethodManager.SHOW_IMPLICIT);
    }

    private void exitEditMode(EditText edt, TextView tv, String key) {
        String newValue = edt.getText().toString().trim();
        if (!newValue.isEmpty()) {
            prefs.edit().putString(key, newValue).apply();
            tv.setText(newValue);
        } else {
            String defaultValue = key.equals(KEY_USERNAME) ? "Nguyễn Ngọc Thiện" : "example@gmail.com";
            tv.setText(prefs.getString(key, defaultValue));
        }
        edt.setVisibility(View.GONE);
        tv.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
    }

    private void loadSelectedRingtone() {
        String uriString = prefs.getString(KEY_RINGTONE_URI, null);
        if (uriString == null) {
            tvSelectedRingtone.setText("Mặc định");
            return;
        }
        try {
            Uri uri = Uri.parse(uriString);
            String fileName = getFileNameFromUri(uri);
            tvSelectedRingtone.setText(fileName != null ? fileName : "Tùy chỉnh");
        } catch (Exception e) {
            tvSelectedRingtone.setText("Mặc định");
        }
    }

    private void pickRingtone() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        startActivityForResult(intent, 1001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                prefs.edit().putString(KEY_RINGTONE_URI, uri.toString()).apply();
                loadSelectedRingtone();
                NotificationHelper.updateNotificationChannel(requireContext(), uri);
                Toast.makeText(getContext(), "Đã chọn nhạc thông báo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        try (android.database.Cursor cursor = requireContext().getContentResolver()
                .query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                fileName = cursor.getString(nameIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }
}