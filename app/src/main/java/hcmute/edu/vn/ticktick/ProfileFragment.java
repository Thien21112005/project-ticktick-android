package hcmute.edu.vn.ticktick;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final String PREF_NAME = "UserProfilePrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_JOIN_DATE = "join_date";
    private static final String AVATAR_FILE_NAME = "profile_avatar.jpg";

    private ShapeableImageView imgAvatar;

    private TextView tvUsernameDisplay, tvEmailDisplay, tvJoinDate, tvCompletedTasks;
    private EditText edtUsername, edtEmail;

    private SharedPreferences prefs;
    private ActivityResultLauncher<String> pickImageLauncher;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

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
        tvCompletedTasks = view.findViewById(R.id.tv_completed_tasks);

        loadProfileData();

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
    }

    private void handleSelectedImage(Uri uri) {
        if (uri == null) return;
        try {
            saveImageToInternalStorage(uri);
            loadAvatarFromFile();
            Toast.makeText(requireContext(), "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không thể lưu ảnh", Toast.LENGTH_SHORT).show();
        }
        if (getActivity() instanceof MainActivity) {
            MainActivity main = (MainActivity) getActivity();
            // Gọi refresh menu nếu cần (sẽ làm ở bước sau)
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

        InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
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

        InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
        if (getActivity() instanceof MainActivity) {
            MainActivity main = (MainActivity) getActivity();
            // Gọi refresh menu nếu cần (sẽ làm ở bước sau)
        }
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

        // 1. Gọi DatabaseHelper
        hcmute.edu.vn.ticktick.database.DatabaseHelper dbHelper =
                new hcmute.edu.vn.ticktick.database.DatabaseHelper(requireContext());

        java.util.List<hcmute.edu.vn.ticktick.models.SubTask> completedTasks = dbHelper.getCompletedSubTasks();

        // 3. Đếm số lượng task đã hoàn thành
        int totalCompleted = completedTasks.size();

        // 4. In con số vừa đếm được lên màn hình
        tvCompletedTasks.setText(String.valueOf(totalCompleted));
    }
}