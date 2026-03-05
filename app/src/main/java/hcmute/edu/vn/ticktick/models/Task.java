package hcmute.edu.vn.ticktick.models;

public class Task {
    private int id;
    private String title; // Tên danh mục (VD: "Học bài", "Việc nhà")
    // Mình có thể bỏ các trường startDate, endDate đi cho nhẹ,
    // vì cái thư mục thì không cần ngày giờ.

    public Task() {}

    public Task(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}