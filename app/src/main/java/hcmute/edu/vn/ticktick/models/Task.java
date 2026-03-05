package hcmute.edu.vn.ticktick.models;

public class Task {
    // Khai báo các thuộc tính dựa trên hình 1 của bạn
    private int id;                 // ID tự tăng của công việc
    private String title;           // Tiêu đề công việc
    private String description;     // Mô tả chi tiết
    private String startDate;       // Ngày bắt đầu (Lưu dạng chuỗi String cho dễ xử lý ban đầu)
    private String endDate;         // Ngày kết thúc
    private boolean isCompleted;    // Đã hoàn thành hay chưa (true/false)

    // Constructor rỗng (Bắt buộc phải có để Firebase hoặc SQLite đôi khi cần dùng tới)
    public Task() {
    }

    // Constructor có đầy đủ tham số (Dùng để tạo nhanh một đối tượng Task)
    public Task(int id, String title, String description, String startDate, String endDate, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCompleted = isCompleted;
    }

    // --- CÁC HÀM GETTER VÀ SETTER ---
    // (Bạn có thể click chuột phải -> Generate -> Getter and Setter để Android Studio tự tạo)

    // Hàm lấy ID
    public int getId() { return id; }
    // Hàm gán ID
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}