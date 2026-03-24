package hcmute.edu.vn.ticktick;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    public static final String CHANNEL_NAME = "TickTick Nhắc nhở";
    private static final String CHANNEL_DESC = "Thông báo nhắc nhở công việc từ TickTick";

    // Tạo channel ID động dựa trên uri nhạc (nếu không có nhạc thì dùng default)
    private static String getChannelId(Uri soundUri) {
        if (soundUri == null) {
            return "ticktick_reminder_default";
        }
        // HashCode của URI để tạo ID unique
        return "ticktick_reminder_" + soundUri.toString().hashCode();
    }

    /**
     * Gọi một lần khi app khởi động (MainActivity.onCreate)
     */
    public static void createNotificationChannel(Context context) {
        // Ban đầu dùng channel mặc định (không âm thanh tùy chỉnh)
        updateNotificationChannel(context, null);
    }

    /**
     * Gọi mỗi khi người dùng chọn nhạc mới trong Profile
     */
    public static void updateNotificationChannel(Context context, Uri soundUri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        String channelId = getChannelId(soundUri);

        // XÓA CHANNEL CŨ nếu tồn tại (rất quan trọng!)
        NotificationChannel oldChannel = manager.getNotificationChannel(channelId);
        if (oldChannel != null) {
            manager.deleteNotificationChannel(channelId);
            Log.d(TAG, "Đã xóa channel cũ: " + channelId);
        }

        // Tạo channel mới
        NotificationChannel channel = new NotificationChannel(
                channelId,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setDescription(CHANNEL_DESC);
        channel.enableVibration(true);
        channel.setShowBadge(true);

        if (soundUri != null) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            channel.setSound(soundUri, audioAttributes);
            Log.d(TAG, "Đã tạo channel với âm thanh tùy chỉnh: " + soundUri);
        } else {
            channel.setSound(null, null);   // Không âm thanh (hoặc để hệ thống mặc định)
            Log.d(TAG, "Đã tạo channel mặc định (không âm thanh tùy chỉnh)");
        }

        manager.createNotificationChannel(channel);
        Log.d(TAG, "Channel đã được tạo thành công: " + channelId);
    }

    /**
     * Gửi thông báo
     */
    public static void sendNotification(Context context, int notificationId, String title, String message) {
        SharedPreferences prefs = context.getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
        String ringtoneUriString = prefs.getString("ringtone_uri", null);
        Uri ringtoneUri = null;

        if (ringtoneUriString != null) {
            try {
                ringtoneUri = Uri.parse(ringtoneUriString);
                // Kiểm tra quyền đọc file
                context.getContentResolver().openInputStream(ringtoneUri).close();
                Log.d(TAG, "Sử dụng âm thanh tùy chỉnh: " + ringtoneUri);
            } catch (Exception e) {
                Log.e(TAG, "Không thể đọc file âm thanh tùy chỉnh → fallback về mặc định", e);
                ringtoneUri = null;
            }
        }

        // Lấy channelId tương ứng với âm thanh hiện tại
        String channelId = getChannelId(ringtoneUri);

        // Cập nhật (tạo mới) channel trước khi gửi thông báo
        updateNotificationChannel(context, ringtoneUri);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("⏰ " + title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_LIGHTS);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
            Log.d(TAG, "Đã gửi thông báo với channel: " + channelId);
        }
    }
}