package hcmute.edu.vn.ticktick;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class NotificationService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int subtaskId      = intent.getIntExtra("subtask_id", 0);
            String title       = intent.getStringExtra("subtask_title");
            String dueDatetime = intent.getStringExtra("due_datetime");

            String message = (dueDatetime != null && !dueDatetime.isEmpty())
                    ? "Công việc đến hạn lúc: " + dueDatetime
                    : "Đã đến giờ nhắc nhở công việc của bạn!";

            // Phải gọi startForeground ngay lập tức trên Android 8+
            // Dùng luôn notification của task làm foreground notification
            android.app.Notification notification = buildForegroundNotification(title, message);
            startForeground(subtaskId != 0 ? subtaskId : 9999, notification);

            // Gửi thông báo ongoing (với nút Tắt) như cũ
            NotificationHelper.sendNotification(this, subtaskId, title, message);
        }

        // Tự chết sau khi làm xong, không restart
        stopSelf();
        return START_NOT_STICKY;
    }

    private android.app.Notification buildForegroundNotification(String title, String message) {
        // Notification tối giản chỉ để thỏa mãn yêu cầu foreground của Android
        return new androidx.core.app.NotificationCompat.Builder(this, NotificationHelper.CHANNEL_NAME)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title != null ? title : "Nhắc nhở")
                .setContentText(message)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}