package hcmute.edu.vn.ticktick;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

    private static String getChannelId(Uri soundUri) {
        if (soundUri == null) return "ticktick_reminder_default";
        return "ticktick_reminder_" + soundUri.toString().hashCode();
    }

    public static void createNotificationChannel(Context context) {
        updateNotificationChannel(context, null);
    }

    public static void updateNotificationChannel(Context context, Uri soundUri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        String channelId = getChannelId(soundUri);

        if (manager.getNotificationChannel(channelId) != null) {
            manager.deleteNotificationChannel(channelId);
        }

        NotificationChannel channel = new NotificationChannel(
                channelId,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setDescription(CHANNEL_DESC);
        channel.enableVibration(true);
        channel.setShowBadge(true);

        if (soundUri != null) {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)        // Quan trọng: dùng ALARM để khó tắt nhạc
                    .build();
            channel.setSound(soundUri, attr);
        } else {
            channel.setSound(null, null);
        }

        manager.createNotificationChannel(channel);
    }

    public static void sendNotification(Context context, int notificationId, String title, String message) {
        SharedPreferences prefs = context.getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
        String uriString = prefs.getString("ringtone_uri", null);
        Uri ringtoneUri = uriString != null ? Uri.parse(uriString) : null;

        String channelId = getChannelId(ringtoneUri);
        updateNotificationChannel(context, ringtoneUri);

        // Intent mở app khi nhấn vào thân notification
        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPendingIntent = PendingIntent.getActivity(
                context, notificationId, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent tắt thông báo (khi nhấn nút "Tắt")
        Intent dismissIntent = new Intent(context, DismissNotificationReceiver.class);
        dismissIntent.putExtra("notification_id", notificationId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context, notificationId, dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("⏰ " + title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)                    // Không cho vuốt tắt dễ dàng
                .setAutoCancel(false)
                .setOnlyAlertOnce(false)             // Cho phép nhắc lại
                .setVibrate(new long[]{0, 500, 300, 500, 300, 500, 300, 500}) // rung lặp nhiều lần
                .setContentIntent(openPendingIntent)
                .addAction(R.drawable.ic_notification, "Tắt nhắc nhở", dismissPendingIntent); // Nút tắt

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
            Log.d(TAG, "Đã gửi thông báo ongoing + nút tắt");
        }
    }
}