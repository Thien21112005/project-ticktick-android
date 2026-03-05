package hcmute.edu.vn.ticktick;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Ẩn thanh tiêu đề (ActionBar) phía trên cùng để màn hình tràn viền đẹp hơn (UX/UI tốt hơn)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 2. Gắn giao diện activity_splash.xml vào màn hình này
        setContentView(R.layout.activity_splash);

        // 3. Dùng Handler để đếm ngược thời gian và chuyển màn hình
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Tạo một Intent (chuyến xe) để chở người dùng từ SplashActivity sang MainActivity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);

                // Bắt đầu khởi hành chuyển sang màn hình chính
                startActivity(intent);

                // Đóng màn hình Splash lại (để khi người dùng ấn nút Back trên điện thoại sẽ không quay lại màn hình chào mừng nữa)
                finish();
            }
        }, 3000); // 3000 milliseconds = 3 giây. Màn hình sẽ hiển thị 3 giây rồi tự chuyển.
    }
}