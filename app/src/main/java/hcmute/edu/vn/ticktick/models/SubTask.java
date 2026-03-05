package hcmute.edu.vn.ticktick.models;

public class SubTask {
    private int id;                 // ID của subtask
    private int taskId;             // Khóa ngoại (Nối với id của bảng Task)
    private String title;           // Tiêu đề subtask
    private String startDateTime;   // Ngày giờ bắt đầu
    private String endDateTime;     // Ngày giờ kết thúc
    private boolean isDone;         // Đã hoàn thành hay chưa

    public SubTask() {
    }

    public SubTask(int id, int taskId, String title, String startDateTime, String endDateTime, boolean isDone) {
        this.id = id;
        this.taskId = taskId;
        this.title = title;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.isDone = isDone;
    }

    // --- GETTER VÀ SETTER ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStartDateTime() { return startDateTime; }
    public void setStartDateTime(String startDateTime) { this.startDateTime = startDateTime; }

    public String getEndDateTime() { return endDateTime; }
    public void setEndDateTime(String endDateTime) { this.endDateTime = endDateTime; }

    public boolean isDone() { return isDone; }
    public void setDone(boolean done) { isDone = done; }
}