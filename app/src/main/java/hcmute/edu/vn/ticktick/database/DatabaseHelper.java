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
        // Lệnh tạo bảng Task
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
    // --- HÀM LẤY DANH SÁCH TẤT CẢ CÔNG VIỆC (SUBTASK) ---
    public List<SubTask> getAllSubTasks() {
        List<SubTask> list = new ArrayList<>();
        // Câu lệnh SQL yêu cầu lấy tất cả dữ liệu từ bảng SubTask
        String query = "SELECT * FROM " + TABLE_SUBTASK;

        SQLiteDatabase db = this.getReadableDatabase(); // Mở kho database để đọc
        Cursor cursor = db.rawQuery(query, null); // Con trỏ (Cursor) duyệt qua từng dòng dữ liệu

        if (cursor.moveToFirst()) {
            do {
                SubTask subTask = new SubTask();
                subTask.setId(cursor.getInt(0));              // Cột 0: ID của SubTask
                subTask.setTaskId(cursor.getInt(1));          // Cột 1: ID của Danh mục (Task)
                subTask.setTitle(cursor.getString(2));        // Cột 2: Tiêu đề công việc
                subTask.setStartDateTime(cursor.getString(3));// Cột 3: Ngày giờ
                subTask.setDone(cursor.getInt(4) == 1);       // Cột 4: Đã xong chưa (1 là true, 0 là false)

                list.add(subTask); // Thêm công việc vừa nhặt được vào danh sách
            } while (cursor.moveToNext()); // Tiếp tục chuyển sang dòng tiếp theo
        }
        cursor.close();
        db.close(); // Đóng kho lại
        return list; // Trả về danh sách công việc
    }

    // --- HÀM XÓA DANH MỤC (TASK) ---
    public void deleteTask(int taskId) {
        // 1. Mở cửa kho dữ liệu với quyền ghi (để có thể xóa)
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. Ra lệnh xóa:
        // - Xóa ở bảng nào? -> Bảng TABLE_TASK
        // - Xóa dòng nào? -> Dòng có cột ID khớp với tham số taskId truyền vào
        db.delete(TABLE_TASK, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});

        // 3. Xóa xong thì đóng cửa kho lại cho an toàn
        db.close();
    }
    // --- HÀM MỚI: LẤY CÔNG VIỆC + TÊN DANH MỤC (VÀ SẮP XẾP GOM NHÓM) ---
    public List<SubTask> getAllSubTasksWithCategory() {
        List<SubTask> list = new ArrayList<>();
        // Lệnh JOIN: Nối 2 bảng và Sắp xếp (ORDER BY) theo ID danh mục để chúng gom lại 1 chỗ
        String query = "SELECT SubTask.*, Task.title FROM " + TABLE_SUBTASK +
                " INNER JOIN " + TABLE_TASK +
                " ON SubTask." + COLUMN_SUBTASK_TASK_ID + " = Task." + COLUMN_TASK_ID +
                " ORDER BY SubTask." + COLUMN_SUBTASK_TASK_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                SubTask subTask = new SubTask();
                subTask.setId(cursor.getInt(0));
                subTask.setTaskId(cursor.getInt(1));
                subTask.setTitle(cursor.getString(2));
                subTask.setStartDateTime(cursor.getString(3));
                subTask.setDone(cursor.getInt(4) == 1);

                // Lấy thêm Tên Danh mục ở cột số 5 (do ta gọi Task.title ở câu lệnh SQL trên)
                subTask.setTaskName(cursor.getString(5));

                list.add(subTask);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    // --- HÀM ĐỔI TÊN DANH MỤC (TASK) ---
    public int updateTaskName(int taskId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase(); // Mở kho với quyền ghi

        android.content.ContentValues values = new android.content.ContentValues();
        values.put(COLUMN_TASK_TITLE, newName); // Đưa tên mới vào gói hàng

        // Ra lệnh cập nhật: Tìm dòng có ID tương ứng và thay bằng tên mới
        int result = db.update(TABLE_TASK, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return result; // Trả về số lượng dòng đã sửa thành công
    }
    // --- HÀM LẤY DANH SÁCH CÔNG VIỆC CỦA MỘT DANH MỤC CỤ THỂ ---
    public List<SubTask> getSubTasksByTaskId(int taskId) {
        List<SubTask> list = new ArrayList<>();
        // Câu lệnh SQL: Lấy tất cả từ bảng SubTask NƠI MÀ cột taskId bằng với id truyền vào
        String query = "SELECT * FROM " + TABLE_SUBTASK + " WHERE " + COLUMN_SUBTASK_TASK_ID + " = " + taskId;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                SubTask subTask = new SubTask();
                subTask.setId(cursor.getInt(0));
                subTask.setTaskId(cursor.getInt(1));
                subTask.setTitle(cursor.getString(2));
                subTask.setStartDateTime(cursor.getString(3));
                subTask.setDone(cursor.getInt(4) == 1);
                list.add(subTask);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}