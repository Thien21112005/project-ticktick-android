package hcmute.edu.vn.ticktick;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import hcmute.edu.vn.ticktick.models.SubTask;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    // 1. Đổi "món hàng" từ Task sang SubTask
    private List<SubTask> subTaskList;

    // Hàm khởi tạo: Nhận mảng dữ liệu SubTask truyền vào
    public TaskAdapter(List<SubTask> subTaskList) {
        this.subTaskList = subTaskList;
    }

    // 2. Tạo ra cái khung (Bơm file item_task.xml mới làm ở Bước 1 vào)
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    // 3. Gắn dữ liệu thật vào cái khung
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        SubTask subTask = subTaskList.get(position); // Lấy công việc ở vị trí hiện tại

        // --- ĐOẠN CODE MỚI: XỬ LÝ ẨN/HIỆN TIÊU ĐỀ DANH MỤC (GOM NHÓM UX/UI) ---
        // Nếu là món hàng đầu tiên (position == 0)
        // HOẶC món hàng này thuộc danh mục khác với món hàng đứng ngay trước nó
        if (position == 0 || subTaskList.get(position - 1).getTaskId() != subTask.getTaskId()) {
            holder.tvCategoryHeader.setVisibility(View.VISIBLE); // Bật dòng chữ đỏ to đùng lên
            holder.tvCategoryHeader.setText(subTask.getTaskName()); // In tên danh mục ra (Ví dụ: "Học tập")
        } else {
            // Nếu nó cùng nhóm với công việc bên trên thì giấu cái tiêu đề đi cho đỡ rối mắt
            holder.tvCategoryHeader.setVisibility(View.GONE);
        }
        // ----------------------------------------------------------------------

        // Đặt chữ cho Tiêu đề và Thời gian
        holder.tvTitle.setText(subTask.getTitle());
        holder.tvTime.setText(subTask.getStartDateTime());

        // LƯU Ý QUAN TRỌNG: Gỡ sự kiện cũ ra trước khi gán trạng thái mới để tránh lỗi hiển thị lộn xộn
        holder.cbDone.setOnCheckedChangeListener(null);

        // Cài đặt nút tick là đã tick hay chưa dựa vào dữ liệu
        holder.cbDone.setChecked(subTask.isDone());

        // Gọi hàm hiệu ứng gạch ngang chữ (UX/UI)
        applyStrikeThrough(holder.tvTitle, subTask.isDone());

        // 4. Lắng nghe hành động: Khi người dùng lấy tay bấm vào nút Checkbox
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Cập nhật lại trạng thái của công việc
            subTask.setDone(isChecked);
            // Kích hoạt ngay hiệu ứng gạch ngang chữ cho đã mắt
            applyStrikeThrough(holder.tvTitle, isChecked);

            // (Tương lai chúng ta sẽ thêm 1 dòng code ở đây để lưu trạng thái này xuống Database)
        });
    }

    @Override
    public int getItemCount() {
        return subTaskList.size();
    }

    // --- HÀM HỖ TRỢ UX/UI: TẠO HIỆU ỨNG GẠCH NGANG CHỮ ---
    private void applyStrikeThrough(TextView tvTitle, boolean isDone) {
        if (isDone) {
            // Nếu đã xong: Thêm đường gạch ngang ở giữa chữ và làm chữ mờ đi thành màu xám
            tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvTitle.setTextColor(0xFF888888); // Mã màu xám
        } else {
            // Nếu chưa xong: Xóa đường gạch ngang, trả chữ về màu đen đậm bình thường
            tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            tvTitle.setTextColor(0xFF333333); // Mã màu xám đen
        }
    }

    // Lớp nội: Tìm các ID trong file item_task.xml
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvCategoryHeader; // <-- KHAI BÁO THÊM tvCategoryHeader Ở ĐÂY
        CheckBox cbDone;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvTime = itemView.findViewById(R.id.tv_item_time);
            cbDone = itemView.findViewById(R.id.cb_task_done);
            tvCategoryHeader = itemView.findViewById(R.id.tv_category_header); // <-- ÁNH XẠ ID Ở ĐÂY
        }
    }
}