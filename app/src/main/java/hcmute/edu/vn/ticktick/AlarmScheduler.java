package hcmute.edu.vn.ticktick;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.ticktick.models.SubTask;

public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";

    // Không dùng static final SDF nữa để tránh lỗi locale
    private static SimpleDateFormat getDateFormatter() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    /**
     * Lên lịch thông báo nhắc nhở trước giờ bắt đầu của SubTask
     */
    public static void scheduleNotification(Context context, SubTask subTask) {
        if (subTask == null) return;
        if (subTask.getNotifyBefore() <= 0) return;
        if (subTask.getStartDateTime() == null || subTask.getStartDateTime().trim().isEmpty()) return;

        try {
            SimpleDateFormat sdf = getDateFormatter();
            Date startDate = sdf.parse(subTask.getStartDateTime());
            if (startDate == null) return;

            long notifyTimeMillis = startDate.getTime()
                    - TimeUnit.MINUTES.toMillis(subTask.getNotifyBefore());

            // Nếu thời gian nhắc đã qua thì không đặt
            if (notifyTimeMillis <= System.currentTimeMillis()) return;

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("subtask_id", subTask.getId());
            intent.putExtra("subtask_title", subTask.getTitle());
            intent.putExtra("due_datetime", subTask.getDueDateTime());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    subTask.getId(),                    // requestCode làm ID duy nhất
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Kiểm tra quyền exact alarm (bắt buộc từ Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Exact alarm permission is not granted for this app. " +
                            "Please ask user to grant 'Alarms & reminders' in Settings.");
                    // Tùy chọn: tự động mở trang xin quyền
                    requestExactAlarmPermission(context);
                    return; // hoặc bạn có thể vẫn đặt inexact alarm nếu muốn
                }
            }

            // Đặt exact alarm (hỗ trợ Doze mode)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notifyTimeMillis,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        notifyTimeMillis,
                        pendingIntent
                );
            }

            Log.d(TAG, "Scheduled notification for subtask " + subTask.getId() +
                    " at " + sdf.format(new Date(notifyTimeMillis)));

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + subTask.getStartDateTime(), e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error when scheduling alarm", e);
        }
    }

    /**
     * Hủy thông báo đã đặt
     */
    public static void cancelNotification(Context context, int subtaskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                subtaskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Cancelled notification for subtask " + subtaskId);
    }

    /**
     * Mở trang Settings để người dùng cấp quyền "Alarms & reminders"
     */
    private static void requestExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Cannot open exact alarm permission screen", e);
            }
        }
    }
}