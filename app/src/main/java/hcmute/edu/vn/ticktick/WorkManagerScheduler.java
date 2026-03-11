package hcmute.edu.vn.ticktick;

import android.content.Context;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.ticktick.models.SubTask;

public class WorkManagerScheduler {

    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());

    public static void scheduleNotification(Context context, SubTask subTask) {
        // Không nhắc nếu người dùng chọn "Không nhắc"
        if (subTask.getNotifyBefore() <= 0) return;

        // Không nhắc nếu không có startDateTime
        if (subTask.getStartDateTime() == null || subTask.getStartDateTime().trim().isEmpty()) return;

        try {
            // Parse ngày giờ bắt đầu công việc
            Date startDate = SDF.parse(subTask.getStartDateTime());
            if (startDate == null) return;

            // Tính thời điểm cần bắn thông báo = startDateTime - notifyBefore phút
            long notifyTimeMillis = startDate.getTime()
                    - TimeUnit.MINUTES.toMillis(subTask.getNotifyBefore());

            // Tính delay từ bây giờ đến lúc cần bắn
            long delayMillis = notifyTimeMillis - System.currentTimeMillis();

            // Nếu thời gian đã qua thì thôi, không lên lịch nữa
            if (delayMillis <= 0) return;

            // Đóng gói dữ liệu truyền vào Worker
            Data inputData = new Data.Builder()
                    .putInt(ReminderWorker.KEY_SUBTASK_ID,       subTask.getId())
                    .putString(ReminderWorker.KEY_SUBTASK_TITLE, subTask.getTitle())
                    .putString(ReminderWorker.KEY_DUE_DATETIME,  subTask.getDueDateTime())
                    .build();

            // Tạo OneTimeWorkRequest với delay đã tính
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag("reminder_" + subTask.getId()) // Tag để cancel sau này nếu cần
                    .build();

            // Enqueue vào WorkManager (tự động chạy đúng giờ kể cả khi app bị tắt)
            WorkManager.getInstance(context).enqueue(workRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm hủy nhắc nhở nếu người dùng xóa task
    public static void cancelNotification(Context context, int subtaskId) {
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_" + subtaskId);
    }
}