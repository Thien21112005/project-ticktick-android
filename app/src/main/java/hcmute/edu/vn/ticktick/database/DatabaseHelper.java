package hcmute.edu.vn.ticktick.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import hcmute.edu.vn.ticktick.models.Task;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
public class DatabaseHelper extends SQLiteOpenHelper {

    // Tên cơ sở dữ liệu và phiên bản
    private static final String DATABASE_NAME = "TickTickApp.db";
    private static final int DATABASE_VERSION = 1;

    // --- ĐỊNH NGHĨA BẢNG TASK ---
    public static final String TABLE_TASK = "Task";
    public static final String COLUMN_TASK_ID = "id";
    public static final String COLUMN_TASK_TITLE = "title";
    public static final String COLUMN_TASK_DESC = "description";
    public static final String COLUMN_TASK_START = "startDate";
    public static final String COLUMN_TASK_END = "endDate";
    public static final String COLUMN_TASK_IS_COMPLETED = "isCompleted"; // Lưu 0 hoặc 1

    // --- ĐỊNH NGHĨA BẢNG SUBTASK ---
    public static final String TABLE_SUBTASK = "SubTask";
    public static final String COLUMN_SUBTASK_ID = "id";
    public static final String COLUMN_SUBTASK_TASK_ID = "taskId"; // Khóa ngoại
    public static final String COLUMN_SUBTASK_TITLE = "title";
    public static final String COLUMN_SUBTASK_START_TIME = "startDateTime";
    public static final String COLUMN_SUBTASK_END_TIME = "endDateTime";
    public static final String COLUMN_SUBTASK_IS_DONE = "isDone"; // Lưu 0 hoặc 1

    // Hàm khởi tạo
    public DatabaseHelper(Context context) {
        // Gọi hàm của lớp cha để tạo database
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Hàm này chạy đầu tiên khi database được tạo
    // Nơi đây dùng để viết các câu lệnh SQL tạo bảng
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Câu lệnh SQL tạo bảng Task
        String createTableTask = "CREATE TABLE " + TABLE_TASK + " ("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " // ID tự tăng
                + COLUMN_TASK_TITLE + " TEXT, " // Kiểu chữ
                + COLUMN_TASK_DESC + " TEXT, "
                + COLUMN_TASK_START + " TEXT, "
                + COLUMN_TASK_END + " TEXT, "
                + COLUMN_TASK_IS_COMPLETED + " INTEGER DEFAULT 0)"; // Mặc định là 0 (chưa xong)

        // Câu lệnh SQL tạo bảng SubTask (Có khóa ngoại)
        String createTableSubTask = "CREATE TABLE " + TABLE_SUBTASK + " ("
                + COLUMN_SUBTASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SUBTASK_TASK_ID + " INTEGER, " // Chứa ID của bảng Task
                + COLUMN_SUBTASK_TITLE + " TEXT, "
                + COLUMN_SUBTASK_START_TIME + " TEXT, "
                + COLUMN_SUBTASK_END_TIME + " TEXT, "
                + COLUMN_SUBTASK_IS_DONE + " INTEGER DEFAULT 0, "
                // Khai báo foreign key liên kết với bảng Task
                + "FOREIGN KEY(" + COLUMN_SUBTASK_TASK_ID + ") REFERENCES " + TABLE_TASK + "(" + COLUMN_TASK_ID + ") ON DELETE CASCADE)";

        // Thực thi các câu lệnh SQL
        db.execSQL(createTableTask);
        db.execSQL(createTableSubTask);
    }

    // Bật tính năng khóa ngoại (Foreign Key) trong SQLite (Mặc định nó bị tắt)
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Hàm này chạy khi bạn tăng DATABASE_VERSION lên (VD: từ 1 lên 2 khi cập nhật app)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu tồn tại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBTASK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
        // Tạo lại bảng mới
        onCreate(db);
    }
    // Hàm thêm một Task mới vào Database
    public long addTask(Task task) {
        // 1. Mở kết nối với Database ở chế độ Ghi (Writable) để có thể thêm dữ liệu
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. Tạo một đối tượng ContentValues.
        // Nó giống như một cái giỏ hàng, bạn bỏ từng món (cột, giá trị) vào giỏ.
        ContentValues values = new ContentValues();

        // 3. Bỏ dữ liệu vào giỏ. Cú pháp: put("Tên_Cột", Giá_trị)
        values.put(COLUMN_TASK_TITLE, task.getTitle()); // Thêm tiêu đề
        values.put(COLUMN_TASK_DESC, task.getDescription()); // Thêm mô tả
        // (Tạm thời chúng ta thêm Tiêu đề và Mô tả trước cho dễ hiểu, ngày tháng có thể update sau)

        // 4. Thực hiện lệnh insert (chèn) cái "giỏ hàng" đó vào bảng TABLE_TASK.
        // Hàm insert sẽ trả về ID của dòng mới được thêm, hoặc -1 nếu bị lỗi.
        long id = db.insert(TABLE_TASK, null, values);

        // 5. Đóng kết nối Database lại để giải phóng bộ nhớ (Rất quan trọng)
        db.close();

        // 6. Trả về ID để biết là thêm thành công hay thất bại
        return id;
    }
    // Hàm lấy toàn bộ danh sách công việc
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>(); // Tạo một mảng rỗng để chứa dữ liệu

        // Câu lệnh SQL: Lấy tất cả (*) từ bảng Task
        String selectQuery = "SELECT * FROM " + TABLE_TASK;

        // Mở database ở chế độ Đọc (Readable)
        SQLiteDatabase db = this.getReadableDatabase();

        // Cursor giống như một mũi tên trỏ vào từng dòng kết quả trong bảng Excel
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Nếu mũi tên trỏ được vào dòng đầu tiên (nghĩa là có dữ liệu)
        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0)); // Cột 0 là ID
                task.setTitle(cursor.getString(1)); // Cột 1 là Tiêu đề
                task.setDescription(cursor.getString(2)); // Cột 2 là Mô tả
                task.setStartDate(cursor.getString(3)); // Cột 3 là Ngày bắt đầu
                task.setEndDate(cursor.getString(4)); // Cột 4 là Ngày kết thúc
                // Cột 5 là isCompleted (Lưu 0 hoặc 1), nếu == 1 thì là true (đã xong)
                task.setCompleted(cursor.getInt(5) == 1);

                taskList.add(task); // Nhét công việc vừa lấy được vào mảng
            } while (cursor.moveToNext()); // Tiếp tục nhảy xuống dòng tiếp theo
        }

        cursor.close(); // Dùng xong mũi tên thì đóng lại cho đỡ tốn RAM
        db.close();     // Đóng database
        return taskList; // Trả về mảng chứa toàn bộ công việc
    }
}