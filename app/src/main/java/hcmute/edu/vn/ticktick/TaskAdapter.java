package hcmute.edu.vn.ticktick;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import hcmute.edu.vn.ticktick.models.Task;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;

    // Hàm khởi tạo: Nhận mảng dữ liệu truyền vào
    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    // 1. Tạo ra cái khung (Bơm file item_task.xml vào)
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    // 2. Gắn dữ liệu thật vào cái khung (Lấy title, time nhét vào TextView)
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position); // Lấy công việc ở vị trí hiện tại
        holder.tvTitle.setText(task.getTitle()); // Đặt chữ cho Tiêu đề
        holder.tvTime.setText(task.getStartDate() + " - " + task.getEndDate()); // Đặt chữ cho Thời gian
    }

    // 3. Báo cho băng chuyền biết có tổng cộng bao nhiêu món hàng
    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Lớp nội (Inner class) để tìm các ID trong file item_task.xml
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvTime = itemView.findViewById(R.id.tv_item_time);
        }
    }
}