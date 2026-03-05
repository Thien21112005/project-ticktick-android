package hcmute.edu.vn.ticktick;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class MenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();

        // Tìm 3 nút
        LinearLayout btnToday = view.findViewById(R.id.menu_item_today);
        ImageView iconToday = view.findViewById(R.id.icon_today);
        TextView textToday = view.findViewById(R.id.text_today);

        LinearLayout btnInbox = view.findViewById(R.id.menu_item_inbox);
        ImageView iconInbox = view.findViewById(R.id.icon_inbox);
        TextView textInbox = view.findViewById(R.id.text_inbox);

        LinearLayout btn7Days = view.findViewById(R.id.menu_item_next_7_days);
        ImageView icon7Days = view.findViewById(R.id.icon_7days);
        TextView text7Days = view.findViewById(R.id.text_7days);

        // Chuẩn bị màu và hiệu ứng
        int colorRed = ContextCompat.getColor(requireContext(), R.color.red_primary);
        int colorGray = ContextCompat.getColor(requireContext(), R.color.icon_inactive);
        int colorTextDark = ContextCompat.getColor(requireContext(), R.color.text_dark);

        TypedValue rippleEffect = new TypedValue();
        requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, rippleEffect, true);

        // HÀM PHỤ TRỢ: Giúp code gọn hơn, tái sử dụng để reset màu về xám
        Runnable resetColors = () -> {
            btnToday.setBackgroundResource(rippleEffect.resourceId);
            iconToday.setImageTintList(ColorStateList.valueOf(colorGray));
            textToday.setTextColor(colorTextDark);

            btnInbox.setBackgroundResource(rippleEffect.resourceId);
            iconInbox.setImageTintList(ColorStateList.valueOf(colorGray));
            textInbox.setTextColor(colorTextDark);

            btn7Days.setBackgroundResource(rippleEffect.resourceId);
            icon7Days.setImageTintList(ColorStateList.valueOf(colorGray));
            text7Days.setTextColor(colorTextDark);
        };

        if (mainActivity != null) {
            resetColors.run(); // Đầu tiên cứ reset hết cả 3 về xám trước cho chắc ăn

            if (mainActivity.currentMenuSelection == 0) {
                // Đang chọn Today -> Tô đỏ Today
                btnToday.setBackgroundResource(R.drawable.bg_rounded_red_light);
                iconToday.setImageTintList(ColorStateList.valueOf(colorRed));
                textToday.setTextColor(colorRed);
            } else if (mainActivity.currentMenuSelection == 1) {
                // Đang chọn Inbox -> Tô đỏ Inbox
                btnInbox.setBackgroundResource(R.drawable.bg_rounded_red_light);
                iconInbox.setImageTintList(ColorStateList.valueOf(colorRed));
                textInbox.setTextColor(colorRed);
            } else if (mainActivity.currentMenuSelection == 2) {
                // Đang chọn 7 Days -> Tô đỏ 7 Days
                btn7Days.setBackgroundResource(R.drawable.bg_rounded_red_light);
                icon7Days.setImageTintList(ColorStateList.valueOf(colorRed));
                text7Days.setTextColor(colorRed);
            }
        }

        // Cài đặt sự kiện BẤM cho cả 3 nút
        btnToday.setOnClickListener(v -> {
            if (mainActivity != null) mainActivity.onMenuItemSelected(0);
        });

        btnInbox.setOnClickListener(v -> {
            if (mainActivity != null) mainActivity.onMenuItemSelected(1);
        });

        btn7Days.setOnClickListener(v -> {
            if (mainActivity != null) mainActivity.onMenuItemSelected(2);
        });

        return view;
    }
}