package hcmute.edu.vn.ticktick;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

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
}