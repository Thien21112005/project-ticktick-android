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
    private static final int DATABASE_VERSION = 4; // <-- ĐỔI LÊN 4

    public static final String TABLE_TASK = "Task";
    public static final String COLUMN_TASK_ID = "id";
    public static final String COLUMN_TASK_TITLE = "title";

    public static final String TABLE_SUBTASK = "SubTask";
    public static final String COLUMN_SUBTASK_ID = "id";
    public static final String COLUMN_SUBTASK_TASK_ID = "taskId";
    public static final String COLUMN_SUBTASK_TITLE = "title";
    public static final String COLUMN_SUBTASK_TIME = "startDateTime";
    public static final String COLUMN_SUBTASK_DUE = "dueDateTime";       // <-- THÊM MỚI
    public static final String COLUMN_SUBTASK_IS_DONE = "isDone";
    public static final String COLUMN_SUBTASK_NOTIFY = "notifyBefore";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableTask = "CREATE TABLE " + TABLE_TASK + " ("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TASK_TITLE + " TEXT)";

        String createTableSubTask = "CREATE TABLE " + TABLE_SUBTASK + " ("
                + COLUMN_SUBTASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SUBTASK_TASK_ID + " INTEGER, "
                + COLUMN_SUBTASK_TITLE + " TEXT, "
                + COLUMN_SUBTASK_TIME + " TEXT, "
                + COLUMN_SUBTASK_DUE + " TEXT, "                          // <-- THÊM MỚI
                + COLUMN_SUBTASK_IS_DONE + " INTEGER DEFAULT 0, "
                + COLUMN_SUBTASK_NOTIFY + " INTEGER DEFAULT 0, "
                + "FOREIGN KEY(" + COLUMN_SUBTASK_TASK_ID + ") REFERENCES "
                + TABLE_TASK + "(" + COLUMN_TASK_ID + ") ON DELETE CASCADE)";

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
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_SUBTASK +
                    " ADD COLUMN " + COLUMN_SUBTASK_NOTIFY + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 4) {                                             // <-- THÊM MỚI
            db.execSQL("ALTER TABLE " + TABLE_SUBTASK +
                    " ADD COLUMN " + COLUMN_SUBTASK_DUE + " TEXT");
        }
    }

    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        long id = db.insert(TABLE_TASK, null, values);
        db.close();
        return id;
    }

    public List<Task> getAllTasks() {
        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASK, null);
        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(0));
                task.setTitle(cursor.getString(1));
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
        values.put(COLUMN_SUBTASK_TASK_ID, subTask.getTaskId());
        values.put(COLUMN_SUBTASK_TITLE, subTask.getTitle());
        values.put(COLUMN_SUBTASK_TIME, subTask.getStartDateTime());
        values.put(COLUMN_SUBTASK_DUE, subTask.getDueDateTime());         // <-- THÊM MỚI
        values.put(COLUMN_SUBTASK_IS_DONE, subTask.isDone() ? 1 : 0);
        values.put(COLUMN_SUBTASK_NOTIFY, subTask.getNotifyBefore());
        long id = db.insert(TABLE_SUBTASK, null, values);
        db.close();
        return id;
    }

    // --- Helper đọc SubTask từ Cursor (tránh lặp code) ---
    // Thứ tự cột: 0=id, 1=taskId, 2=title, 3=startDateTime, 4=dueDateTime, 5=isDone, 6=notifyBefore
    private SubTask cursorToSubTask(Cursor cursor) {
        SubTask subTask = new SubTask();
        subTask.setId(cursor.getInt(0));
        subTask.setTaskId(cursor.getInt(1));
        subTask.setTitle(cursor.getString(2));
        subTask.setStartDateTime(cursor.getString(3));
        subTask.setDueDateTime(cursor.getString(4));                      // <-- THÊM MỚI
        subTask.setDone(cursor.getInt(5) == 1);
        subTask.setNotifyBefore(cursor.getInt(6));
        return subTask;
    }

    public List<SubTask> getAllSubTasks() {
        List<SubTask> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SUBTASK, null);
        if (cursor.moveToFirst()) {
            do { list.add(cursorToSubTask(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public void deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASK, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    public List<SubTask> getAllSubTasksWithCategory() {
        List<SubTask> list = new ArrayList<>();
        // Thứ tự cột JOIN: 0=id,1=taskId,2=title,3=startDateTime,4=dueDateTime,5=isDone,6=notifyBefore,7=taskName
        String query = "SELECT SubTask.*, Task.title FROM " + TABLE_SUBTASK
                + " INNER JOIN " + TABLE_TASK
                + " ON SubTask." + COLUMN_SUBTASK_TASK_ID + " = Task." + COLUMN_TASK_ID
                + " ORDER BY SubTask." + COLUMN_SUBTASK_TASK_ID;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                SubTask subTask = cursorToSubTask(cursor);
                subTask.setTaskName(cursor.getString(7));                 // cột 7 = Task.title
                list.add(subTask);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public int updateTaskName(int taskId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, newName);
        int result = db.update(TABLE_TASK, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return result;
    }

    public List<SubTask> getSubTasksByTaskId(int taskId) {
        List<SubTask> list = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_SUBTASK
                + " WHERE " + COLUMN_SUBTASK_TASK_ID + " = " + taskId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do { list.add(cursorToSubTask(cursor)); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}