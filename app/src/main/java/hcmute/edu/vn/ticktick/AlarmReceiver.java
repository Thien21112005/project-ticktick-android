package hcmute.edu.vn.ticktick;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int subtaskId    = intent.getIntExtra("subtask_id", 0);
        String title     = intent.getStringExtra("subtask_title");
        String dueDatetime = intent.getStringExtra("due_datetime");

        Intent serviceIntent = new Intent(context, NotificationService.class);
        serviceIntent.putExtra("subtask_id", subtaskId);
        serviceIntent.putExtra("subtask_title", title);
        serviceIntent.putExtra("due_datetime", dueDatetime);

        // Phải dùng startForegroundService trên Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}