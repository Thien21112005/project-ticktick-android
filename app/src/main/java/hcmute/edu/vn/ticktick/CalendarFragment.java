package hcmute.edu.vn.ticktick;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.ticktick.database.DatabaseHelper;
import hcmute.edu.vn.ticktick.models.SubTask;

public class CalendarFragment extends Fragment {

    private static final String TAG = "CalendarFragment";
    private static final int HOUR_HEIGHT_DP = 80;

    private Button btnToday, btnPrevDay, btnNextDay;
    private TextView tvSelectedDate;
    private LinearLayout timelineLayout;
    private DatabaseHelper dbHelper;
    private Calendar currentCalendar;

    private final int[] TASK_COLORS = {
            0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
            0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
            0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFFFC107,
            0xFFFF9800, 0xFFFF5722, 0xFF795548, 0xFF9E9E9E,
            0xFF607D8B
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        btnToday = view.findViewById(R.id.btnToday);
        btnPrevDay = view.findViewById(R.id.btnPrevDay);
        btnNextDay = view.findViewById(R.id.btnNextDay);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        timelineLayout = view.findViewById(R.id.timelineLayout);

        dbHelper = new DatabaseHelper(requireContext());
        currentCalendar = Calendar.getInstance();

        btnToday.setOnClickListener(v -> goToToday());
        btnPrevDay.setOnClickListener(v -> changeDay(-1));
        btnNextDay.setOnClickListener(v -> changeDay(1));
        tvSelectedDate.setOnClickListener(v -> showMaterialDatePicker());

        updateDateDisplay();
        loadDayView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDayView();
    }

    private void goToToday() {
        currentCalendar = Calendar.getInstance();
        updateDateDisplay();
        loadDayView();
    }

    private void changeDay(int amount) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, amount);
        updateDateDisplay();
        loadDayView();
    }

    private void updateDateDisplay() {
        String[] daysOfWeek = {"Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy"};
        String[] months = {"Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
                "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"};

        String dayName = daysOfWeek[currentCalendar.get(Calendar.DAY_OF_WEEK) - 1];
        String monthName = months[currentCalendar.get(Calendar.MONTH)];

        tvSelectedDate.setText(dayName + ", " +
                currentCalendar.get(Calendar.DAY_OF_MONTH) + " " +
                monthName + " " + currentCalendar.get(Calendar.YEAR));
    }

    private void showMaterialDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày")
                .setSelection(currentCalendar.getTimeInMillis())
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar selected = Calendar.getInstance();
            selected.setTimeInMillis(selection);
            currentCalendar.set(selected.get(Calendar.YEAR), selected.get(Calendar.MONTH), selected.get(Calendar.DAY_OF_MONTH));
            updateDateDisplay();
            loadDayView();
        });
        picker.show(getParentFragmentManager(), "date_picker");
    }

    private void loadDayView() {
        timelineLayout.removeAllViews();

        int hourHeightPx = (int) (HOUR_HEIGHT_DP * getResources().getDisplayMetrics().density);
        int totalHeight = 24 * hourHeightPx;

        LinearLayout timeColumn = new LinearLayout(getContext());
        timeColumn.setOrientation(LinearLayout.VERTICAL);
        timeColumn.setLayoutParams(new LinearLayout.LayoutParams(140, totalHeight));

        FrameLayout eventArea = new FrameLayout(getContext());
        eventArea.setLayoutParams(new LinearLayout.LayoutParams(0, totalHeight, 1f));

        View divider = new View(getContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(2, totalHeight));
        divider.setBackgroundColor(0xFFCCCCCC);

        timelineLayout.addView(timeColumn);
        timelineLayout.addView(divider);
        timelineLayout.addView(eventArea);

        for (int h = 0; h < 24; h++) {
            TextView hourTv = new TextView(getContext());
            hourTv.setText(String.format(Locale.getDefault(), "%02d:00", h));
            hourTv.setTextSize(14);
            hourTv.setTextColor(0xFF666666);
            hourTv.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            hourTv.setPadding(0, 0, 16, 0);
            hourTv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, hourHeightPx));
            timeColumn.addView(hourTv);

            View line = new View(getContext());
            line.setBackgroundColor(0x1A000000);
            FrameLayout.LayoutParams lineParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, 1);
            lineParams.topMargin = h * hourHeightPx;
            line.setLayoutParams(lineParams);
            eventArea.addView(line);
        }

        // Lấy ngày dạng dd/MM/yyyy
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = sdf.format(currentCalendar.getTime());

        List<SubTask> subtasks = dbHelper.getSubTasksForDayCovering(dateStr);
        Log.d(TAG, "Found " + subtasks.size() + " subtasks covering " + dateStr);

        if (subtasks.isEmpty()) {
            TextView emptyTv = new TextView(getContext());
            emptyTv.setText("Không có công việc nào trong ngày này");
            emptyTv.setTextSize(16);
            emptyTv.setTextColor(0xFF888888);
            emptyTv.setGravity(Gravity.CENTER);
            FrameLayout.LayoutParams emptyParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            emptyParams.topMargin = (int) (hourHeightPx * 8);
            emptyTv.setLayoutParams(emptyParams);
            eventArea.addView(emptyTv);
            return;
        }

        long startOfDay = getStartOfDayMillis(currentCalendar);
        long endOfDay = getEndOfDayMillis(currentCalendar);
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (SubTask sub : subtasks) {
            try {
                Date startDate = dateTimeFormat.parse(sub.getStartDateTime());
                if (startDate == null) continue;

                Date dueDate = sub.getDueDateTime() != null
                        ? dateTimeFormat.parse(sub.getDueDateTime())
                        : null;

                long startMillis = startDate.getTime();
                long dueMillis;
                String endTimeStr;

                if (dueDate != null) {
                    dueMillis = dueDate.getTime();
                    endTimeStr = sub.getDueDateTime().split(" ")[1];
                } else {
                    dueMillis = startMillis + 30 * 60 * 1000L; // 30 phút cho task point
                    endTimeStr = sub.getStartDateTime().split(" ")[1];
                }

                long blockStart = Math.max(startMillis, startOfDay);
                long blockEnd = Math.min(dueMillis, endOfDay);

                if (blockEnd <= blockStart) continue;

                float startHour = (blockStart - startOfDay) / (1000f * 60 * 60);
                float duration = (blockEnd - blockStart) / (1000f * 60 * 60);

                String startTimeStr = sub.getStartDateTime().split(" ")[1];

                View eventView = createEventBlock(sub, startHour, duration, hourHeightPx, startTimeStr, endTimeStr);
                eventArea.addView(eventView);

            } catch (ParseException e) {
                Log.e(TAG, "Parse error sub " + sub.getId(), e);
            }
        }
    }

    private long getStartOfDayMillis(Calendar calendar) {
        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getEndOfDayMillis(Calendar calendar) {
        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    private View createEventBlock(SubTask sub, float startHour, float duration, int hourHeightPx, String startTime, String endTime) {
        CardView card = new CardView(getContext());
        card.setRadius(8f);
        card.setCardElevation(4f);
        card.setMaxCardElevation(6f);
        card.setPreventCornerOverlap(false);
        card.setUseCompatPadding(true);

        int backgroundColor = getTaskColor(sub.getTaskId());
        card.setCardBackgroundColor(backgroundColor);
        int textColor = isColorDark(backgroundColor) ? Color.WHITE : Color.BLACK;

        LinearLayout content = new LinearLayout(getContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(16, 12, 16, 12);

        TextView title = new TextView(getContext());
        title.setText(sub.getTitle());
        title.setTextColor(textColor);
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        content.addView(title);

        TextView timeRange = new TextView(getContext());
        timeRange.setText(startTime + " – " + endTime);
        timeRange.setTextColor(textColor);
        timeRange.setTextSize(13);
        timeRange.setAlpha(0.9f);
        content.addView(timeRange);

        TextView category = new TextView(getContext());
        category.setText("• " + sub.getTaskName());
        category.setTextColor(textColor);
        category.setTextSize(13);
        category.setAlpha(0.9f);
        content.addView(category);

        card.addView(content);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                (int) (duration * hourHeightPx));
        params.topMargin = (int) (startHour * hourHeightPx);
        params.leftMargin = 16;
        params.rightMargin = 16;
        card.setLayoutParams(params);

        return card;
    }

    private int getTaskColor(int taskId) {
        if (taskId <= 0) return 0xFF4285F4;
        int index = taskId % TASK_COLORS.length;
        return TASK_COLORS[index];
    }

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }
}