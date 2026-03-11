package hcmute.edu.vn.ticktick.models;

public class SubTask {
    private int id;                 // ID của subtask
    private int taskId;             // Khóa ngoại kết nối với Task
    private String title;           // Tiêu đề subtask
    private String startDateTime;   // Ngày giờ bắt đầu
    private boolean isDone;         // Đã hoàn thành hay chưa
    private String taskName; // phục vụ cho gom nhóm theo task
    private int notifyBefore;

    public SubTask() {
    }

    // --- CÁC HÀM SETTER (Dùng để gán dữ liệu vào) ---
    public void setId(int id) {
        this.id = id;
    }

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

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    // --- CÁC HÀM GETTER (Dùng để lấy dữ liệu ra) ---
    public int getId() { return id; }
    public int getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public String getStartDateTime() { return startDateTime; }
    public boolean isDone() { return isDone; }
    public String getTaskName() { return taskName; }
    public int getNotifyBefore() {
        return notifyBefore;
    }

    public void setNotifyBefore(int notifyBefore) {
        this.notifyBefore = notifyBefore;
    }
}