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
        int subtaskId = getInputData().getInt(KEY_SUBTASK_ID, 0);
        String subtaskTitle = getInputData().getString(KEY_SUBTASK_TITLE);
        String dueDateTime = getInputData().getString(KEY_DUE_DATETIME);

        String message = (dueDateTime != null && !dueDateTime.isEmpty())
                ? "Công việc đến hạn lúc: " + dueDateTime
                : "Đã đến giờ nhắc nhở công việc của bạn!";

        NotificationHelper.sendNotification(
                getApplicationContext(),
                subtaskId,
                subtaskTitle != null ? subtaskTitle : "Nhắc nhở",
                message
        );

        return Result.success();
    }
}