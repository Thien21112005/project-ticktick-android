package hcmute.edu.vn.ticktick;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {

    // Key để truyền dữ liệu vào Worker
    public static final String KEY_SUBTASK_ID    = "subtask_id";
    public static final String KEY_SUBTASK_TITLE = "subtask_title";
    public static final String KEY_DUE_DATETIME  = "due_datetime";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Lấy dữ liệu được truyền vào
        int    subtaskId    = getInputData().getInt(KEY_SUBTASK_ID, 0);
        String subtaskTitle = getInputData().getString(KEY_SUBTASK_TITLE);
        String dueDateTime  = getInputData().getString(KEY_DUE_DATETIME);

        // Xây dựng nội dung thông báo
        String message = (dueDateTime != null && !dueDateTime.isEmpty())
                ? "Công việc đến hạn lúc: " + dueDateTime
                : "Đã đến giờ nhắc nhở công việc của bạn!";

        // Bắn thông báo lên system tray
        NotificationHelper.sendNotification(
                getApplicationContext(),
                subtaskId,          // dùng subtaskId làm notificationId để mỗi task có 1 noti riêng
                subtaskTitle != null ? subtaskTitle : "Nhắc nhở công việc",
                message
        );

        return Result.success();
    }
}