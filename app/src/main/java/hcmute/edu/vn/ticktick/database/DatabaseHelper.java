package hcmute.edu.vn.ticktick.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.ticktick.models.SubTask;
import hcmute.edu.vn.ticktick.models.Task;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TickTickApp.db";
    // 💡 LƯU Ý QUAN TRỌNG: Đổi DATABASE_VERSION từ 1 lên 2 để máy biết là mình vừa cấu trúc lại Database và nó sẽ tạo lại bảng mới.
    private static final int DATABASE_VERSION = 2;

    // --- BẢNG 1: TASK (ĐÓNG VAI TRÒ LÀ DANH MỤC / LISTS) ---
    public static final String TABLE_TASK = "Task";
    public static final String COLUMN_TASK_ID = "id";
    public static final String COLUMN_TASK_TITLE = "title";

    // --- BẢNG 2: SUBTASK (ĐÓNG VAI TRÒ LÀ CÔNG VIỆC CỤ THỂ) ---
    public static final String TABLE_SUBTASK = "SubTask";
    public static final String COLUMN_SUBTASK_ID = "id";
    public static final String COLUMN_SUBTASK_TASK_ID = "taskId";
    public static final String COLUMN_SUBTASK_TITLE = "title";
    public static final String COLUMN_SUBTASK_TIME = "startDateTime";
    public static final String COLUMN_SUBTASK_IS_DONE = "isDone";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Lệnh tạo bảng Task (Chỉ cần ID và Tên)
        String createTableTask = "CREATE TABLE " + TABLE_TASK + " ("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TASK_TITLE + " TEXT)";

        // Lệnh tạo bảng SubTask (Có chứa ngày giờ và khóa ngoại nối với bảng Task)
        String createTableSubTask = "CREATE TABLE " + TABLE_SUBTASK + " ("
                + COLUMN_SUBTASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SUBTASK_TASK_ID + " INTEGER, "
                + COLUMN_SUBTASK_TITLE + " TEXT, "
                + COLUMN_SUBTASK_TIME + " TEXT, "
                + COLUMN_SUBTASK_IS_DONE + " INTEGER DEFAULT 0, "
                + "FOREIGN KEY(" + COLUMN_SUBTASK_TASK_ID + ") REFERENCES " + TABLE_TASK + "(" + COLUMN_TASK_ID + ") ON DELETE CASCADE)";

        db.execSQL(createTableTask);
        db.execSQL(createTableSubTask);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nếu version tăng (từ 1 lên 2), xóa bảng cũ đi và chạy lại hàm onCreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBTASK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
        onCreate(db);
    }

    // Hàm thêm Danh Mục (Task)
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        long id = db.insert(TABLE_TASK, null, values);
        db.close();
        return id;
    }
    // Hàm lấy danh sách các Thư Mục (Task)
    public List<Task> getAllTasks() {
        List<Task> list = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_TASK;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));      // Cột 0 là ID
                task.setTitle(cursor.getString(1)); // Cột 1 là Tiêu đề
                list.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
    public long addSubTask(SubTask subTask) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Đưa dữ liệu vào các cột tương ứng trong bảng SubTask
        values.put(COLUMN_SUBTASK_TASK_ID, subTask.getTaskId());
        values.put(COLUMN_SUBTASK_TITLE, subTask.getTitle());
        values.put(COLUMN_SUBTASK_TIME, subTask.getStartDateTime());
        values.put(COLUMN_SUBTASK_IS_DONE, subTask.isDone() ? 1 : 0);

        long id = db.insert(TABLE_SUBTASK, null, values);
        db.close();
        return id; // Trả về ID của dòng vừa thêm
    }
}