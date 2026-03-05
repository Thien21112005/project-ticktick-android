package hcmute.edu.vn.ticktick.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
}