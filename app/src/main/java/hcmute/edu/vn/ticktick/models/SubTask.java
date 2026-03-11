package hcmute.edu.vn.ticktick.models;

public class SubTask {
    private int id;
    private int taskId;
    private String title;
    private String startDateTime;
    private String dueDateTime;     // <-- THÊM MỚI
    private boolean isDone;
    private String taskName;
    private int notifyBefore;

    public SubTask() {}

    public void setId(int id) { this.id = id; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
    public void setTitle(String title) { this.title = title; }
    public void setStartDateTime(String startDateTime) { this.startDateTime = startDateTime; }
    public void setDueDateTime(String dueDateTime) { this.dueDateTime = dueDateTime; }  // <-- THÊM MỚI
    public void setDone(boolean done) { isDone = done; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public void setNotifyBefore(int notifyBefore) { this.notifyBefore = notifyBefore; }

    public int getId() { return id; }
    public int getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public String getStartDateTime() { return startDateTime; }
    public String getDueDateTime() { return dueDateTime; }  // <-- THÊM MỚI
    public boolean isDone() { return isDone; }
    public String getTaskName() { return taskName; }
    public int getNotifyBefore() { return notifyBefore; }
}