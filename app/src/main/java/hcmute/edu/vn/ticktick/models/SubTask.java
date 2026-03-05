package hcmute.edu.vn.ticktick.models;

public class SubTask {
    private int id;                 // ID của subtask
    private int taskId;             // Khóa ngoại kết nối với Task
    private String title;           // Tiêu đề subtask
    private String startDateTime;   // Ngày giờ bắt đầu
    private boolean isDone;         // Đã hoàn thành hay chưa

    public SubTask() {
    }

    // --- CÁC HÀM SETTER (Dùng để gán dữ liệu - MainActivity đang cần cái này) ---
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    // --- CÁC HÀM GETTER (Dùng để lấy dữ liệu ra) ---
    public int getId() { return id; }
    public int getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public String getStartDateTime() { return startDateTime; }
    public boolean isDone() { return isDone; }
}