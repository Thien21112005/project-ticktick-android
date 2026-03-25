package hcmute.edu.vn.ticktick;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.ticktick.models.SubTask;

public class AlarmScheduler {

    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public static void scheduleNotification(Context context, SubTask subTask) {
        if (subTask.getNotifyBefore() <= 0) return;
        if (subTask.getStartDateTime() == null || subTask.getStartDateTime().trim().isEmpty()) return;

        try {
            Date startDate = SDF.parse(subTask.getStartDateTime());
            if (startDate == null) return;

            long notifyTimeMillis = startDate.getTime()
                    - TimeUnit.MINUTES.toMillis(subTask.getNotifyBefore());

            if (notifyTimeMillis <= System.currentTimeMillis()) return;

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("subtask_id", subTask.getId());
            intent.putExtra("subtask_title", subTask.getTitle());
            intent.putExtra("due_datetime", subTask.getDueDateTime());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    subTask.getId(), // requestCode dùng làm ID duy nhất
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // setExactAndAllowWhileIdle đảm bảo bắn đúng giờ kể cả khi máy Doze
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
    }
}