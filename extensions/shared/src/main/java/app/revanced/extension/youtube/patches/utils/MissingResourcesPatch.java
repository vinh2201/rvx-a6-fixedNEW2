package app.revanced.extension.youtube.patches.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

@SuppressWarnings("unused")
public final class MissingResourcesPatch {
    private static final String TAG = "ReVancedIconLog";
    
    // Lưu vết ID mới từ Server
    private static final ThreadLocal<Integer> lastServerIconType = new ThreadLocal<>();

    public static int getLegacyIconType(int iconType) {
        // BƯỚC 1: Bắt và lưu vết tất cả các ID từ Server
        lastServerIconType.set(iconType);
        
        // CHỈ MAP những thằng có ID cũ thật sự (Bottom Bar)
        switch (iconType) {
            case 1154: return 406; // Home
            case 1157: return 776; // Shorts
            case 1155: return 408; // Sub
            
            // CÒN LẠI: 1160, 1156, 1161, 1239... ĐỂ NGUYÊN KHÔNG MAP!
            default: return iconType;
        }
    }

    // BƯỚC 2: CỨU HỘ Ở CỬA NGÕ CUỐI CÙNG
    private static Drawable rescueMissingIcon(Resources resources, int id, Resources.Theme theme, int density) {
        // NẾU APP ĐÒI VẼ ID = 0 (Do từ điển 17.x không hiểu ID mới)
        if (id == 0) {
            Integer missingType = lastServerIconType.get();
            if (missingType != null) {
                String targetResName = null;

                // GÁN THẲNG ID MỚI -> TÊN FILE ẢNH (BỎ QUA TỪ ĐIỂN CỦA APP)
                if (missingType == 1160) {
                    targetResName = "@drawable/yt_outline_search_black_24";
                } 
                else if (missingType == 1156) {
                    targetResName = "@drawable/yt_outline_bell_black_24";
                } 
                else if (missingType == 1161) {
                    targetResName = "@drawable/yt_outline_add_circle_cairo_black_36"; // Thay bằng tên file icon Create bác tìm được
                }

                // NẾU TÌM THẤY TÊN FILE TƯƠNG ỨNG, ÉP XUẤT ẢNH RA!
                if (targetResName != null) {
                    int realResId = resources.getIdentifier(targetResName, "drawable", "com.google.android.youtube");
                    if (realResId != 0) {
                        Log.d(TAG, "🚀 [CỨU HỘ THÀNH CÔNG] Phục hồi iconType " + missingType + " -> " + targetResName);
                        try {
                            if (theme != null && density > 0) return resources.getDrawableForDensity(realResId, density, theme);
                            if (theme != null) return resources.getDrawable(realResId, theme);
                            if (density > 0) return resources.getDrawableForDensity(realResId, density);
                            return resources.getDrawable(realResId);
                        } catch (Exception ignored) {}
                    }
                }
            }
            return new ColorDrawable(Color.TRANSPARENT);
        }

        // Nếu ID bình thường khác 0, cho qua
        try {
            if (theme != null && density > 0) return resources.getDrawableForDensity(id, density, theme);
            if (theme != null) return resources.getDrawable(id, theme);
            if (density > 0) return resources.getDrawableForDensity(id, density);
            return resources.getDrawable(id);
        } catch (Resources.NotFoundException ex) {
            return new ColorDrawable(Color.TRANSPARENT);
        }
    }

    // =========================================================================
    // HOOK DRAWABLE METHODS CHỈ CẦN GỌI HÀM CỨU HỘ
    // =========================================================================

    public static Drawable getDrawable(Context context, int id) {
        return rescueMissingIcon(context.getResources(), id, null, 0);
    }

    public static Drawable getDrawable(Resources resources, int id) {
        return rescueMissingIcon(resources, id, null, 0);
    }

    public static Drawable getDrawable(Resources resources, int id, Resources.Theme theme) {
        return rescueMissingIcon(resources, id, theme, 0);
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density) {
        return rescueMissingIcon(resources, id, null, density);
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density, Resources.Theme theme) {
        return rescueMissingIcon(resources, id, theme, density);
    }
}