package hcmute.edu.vn.ticktick;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import hcmute.edu.vn.ticktick.database.DatabaseHelper;
import hcmute.edu.vn.ticktick.models.SubTask;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    // 1. Đổi "món hàng" từ Task sang SubTask
    private List<SubTask> subTaskList;

    // Hàm khởi tạo: Nhận mảng dữ liệu SubTask truyền vào
    public TaskAdapter(List<SubTask> subTaskList, OnTaskEditListener editListener) {
        this.subTaskList = subTaskList;
        this.editListener = editListener; // Cất bộ đàm vào túi để lát dùng
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

        // Nếu là món hàng đầu tiên (position == 0)
        // HOẶC món hàng này thuộc danh mục khác với món hàng đứng ngay trước nó
        if (position == 0 || subTaskList.get(position - 1).getTaskId() != subTask.getTaskId()) {

            // Nếu tên danh mục bị rỗng (ví dụ khi đang xem bên trong 1 danh mục cụ thể),
            // thì ta cũng giấu luôn cái hộp đi cho đẹp, không để lại đường gạch ngang vô duyên.
            if (subTask.getTaskName() == null || subTask.getTaskName().isEmpty()) {
                holder.layoutCategoryHeader.setVisibility(View.GONE);
            } else {
                // Nếu có tên danh mục đàng hoàng (như ở tab Hôm Nay), thì hiện cả hộp (chữ + đường gạch) lên
                holder.layoutCategoryHeader.setVisibility(View.VISIBLE);
                holder.tvCategoryHeader.setText(subTask.getTaskName());
            }

        } else {
            // Nếu cùng danh mục với công việc bên trên -> Giấu nguyên cả cái hộp đi
            holder.layoutCategoryHeader.setVisibility(View.GONE);
        }

        // Đặt chữ cho Tiêu đề và Thời gian
        holder.tvTitle.setText(subTask.getTitle());
        holder.tvTime.setText(subTask.getStartDateTime());
        String due = subTask.getDueDateTime();

        // Gỡ sự kiện cũ ra trước khi gán trạng thái mới để tránh lỗi hiển thị lộn xộn
        holder.cbDone.setOnCheckedChangeListener(null);

        // Cài đặt nút tick là đã tick hay chưa dựa vào dữ liệu
        holder.cbDone.setChecked(subTask.isDone());

        // Gọi hàm hiệu ứng gạch ngang chữ (UX/UI)
        //applyStrikeThrough(holder.tvTitle, subTask.isDone());

        // 4. Lắng nghe hành động: Khi người dùng lấy tay bấm vào nút Checkbox
        holder.cbDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Cập nhật lại trạng thái của công việc
            subTask.setDone(isChecked);
            // Kích hoạt ngay hiệu ứng gạch ngang chữ cho đã mắt
            //applyStrikeThrough(holder.tvTitle, isChecked);

            DatabaseHelper db = new DatabaseHelper(buttonView.getContext());
            db.updateSubTask(subTask);
        });

        // 5. Lắng nghe hành động bấm nút cây bút
        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                // Gọi điện báo tin về cho Activity/Fragment kèm theo thông tin của SubTask này
                editListener.onEditClick(subTask);
            }
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
        TextView tvTitle, tvTime, tvCategoryHeader; //
        CheckBox cbDone;
        TextView tvDue;
        ImageView btnEdit;
        android.widget.LinearLayout layoutCategoryHeader;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvTime = itemView.findViewById(R.id.tv_item_time);
            cbDone = itemView.findViewById(R.id.cb_task_done);
            tvCategoryHeader = itemView.findViewById(R.id.tv_category_header);
            btnEdit = itemView.findViewById(R.id.btn_edit_task);
            layoutCategoryHeader = itemView.findViewById(R.id.layout_category_header);
        }
    }

    // BỘ ĐÀM BÁO TIN SỰ KIỆN CẬP NHẬT HOẶC XOÁ SUBTASK
    public interface OnTaskEditListener {
        void onEditClick(SubTask subTask);
    }

    private OnTaskEditListener editListener;
}