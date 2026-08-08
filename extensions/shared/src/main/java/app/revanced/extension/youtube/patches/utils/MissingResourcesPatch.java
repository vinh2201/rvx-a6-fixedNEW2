package app.revanced.extension.youtube.patches.utils; // Nhớ đổi lại package name cho đúng project của bạn

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public final class MissingResourcesPatch {
    private static final String TAG = "YTIconDebug";
    
    // Bộ nhớ tạm để CHỐNG SPAM LOG (mỗi số/tên chỉ in ra màn hình đúng 1 lần)
    private static final Set<Integer> LOGGED_ICON_TYPES = Collections.synchronizedSet(new HashSet<>());
    private static final Set<Integer> LOGGED_DRAWABLE_IDS = Collections.synchronizedSet(new HashSet<>());

    private static final String[] TOOLBAR_FALLBACK_DRAWABLES = {
            "quantum_ic_more_vert_black_24",
            "ic_more_vert_black_24",
            "quantum_ic_more_vert_white_24",
            "yt_outline_search_black_24"
    };
    
    private static final String[] RESOURCE_PACKAGES = {
            "com.google.android.youtube",
            null
    };

    // ---------------------------------------------------------
    // TRẠM 1: QUÉT SỐ TỪ SERVER (ICON TYPE)
    // ---------------------------------------------------------
    public static int getLegacyIconType(int iconType) {
        if (LOGGED_ICON_TYPES.add(iconType)) {
            if (iconType > 1000) {
                Log.w(TAG, "📡 [SERVER GỌI SỐ MỚI] IconType Cairo: " + iconType);
            } else {
                Log.d(TAG, "📡 [SERVER GỌI SỐ CŨ] IconType Legacy: " + iconType);
            }
        }

        switch (iconType) {
            case 1154: return 406; // Trang chủ (Home)
            case 1157: return 776; // Shorts
            case 1155: return 408; // Kênh đăng ký (Subscriptions)
            case 1156: return 410; // Thư viện / Bạn (Library / You)
            case 1160: return 181; // Nút Tạo (+)
            case 1161: return 158; // Thông báo (Notifications)
            case 1162: return 44;  // Cài đặt (Settings)
            
            // Nếu bạn tìm ra cặp số mới, nhét nó vào đây!
            
            default:
                if (iconType > 1000 && LOGGED_ICON_TYPES.contains(iconType)) {
                    // Log cảnh báo để biết số này chưa được map
                    Log.e(TAG, "⚠️ [CẢNH BÁO] Số " + iconType + " chưa có map! Có thể gây lỗi hiển thị.");
                }
                return iconType;
        }
    }

    // ---------------------------------------------------------
    // TRẠM 2: QUÉT TÊN TỪ APP (RESOURCE NAME)
    // ---------------------------------------------------------
    private static void logAppRequestName(Resources resources, int id) {
        if (id != 0 && LOGGED_DRAWABLE_IDS.add(id)) {
            try {
                String name = resources.getResourceEntryName(id);
                Log.i(TAG, "🔎 [APP TÌM TÊN FILE] Đang vẽ icon tên: " + name + " (Mã ID: " + id + ")");
            } catch (Exception e) {
                Log.e(TAG, "❌ [APP TÌM TÊN FILE] Không tìm thấy tên cho ID: " + id);
            }
        }
    }

    public static Drawable getTransparentDrawable(Context context) {
        return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
    }

    public static Drawable getDrawable(Context context, int id) {
        logAppRequestName(context.getResources(), id);
        if (id == 0) return getFallbackDrawable(context.getResources(), isToolbarMenuStack());

        try {
            return context.getDrawable(id);
        } catch (Resources.NotFoundException ex) {
            Log.e(TAG, "💀 [CRASH TRƯỢT] Mất file ID: " + id + " -> Đang nhét icon dự phòng vào!");
            return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
        }
    }

    public static Drawable getDrawable(Resources resources, int id) {
        logAppRequestName(resources, id);
        if (id == 0) return getFallbackDrawable(resources, isToolbarMenuStack());

        try {
            return resources.getDrawable(id);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }
    }

    public static Drawable getDrawable(Resources resources, int id, Resources.Theme theme) {
        logAppRequestName(resources, id);
        if (id == 0) return getFallbackDrawable(resources, isToolbarMenuStack());

        try {
            return resources.getDrawable(id, theme);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density) {
        logAppRequestName(resources, id);
        if (id == 0) return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack());

        try {
            return resources.getDrawableForDensity(id, density);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack());
        }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density, Resources.Theme theme) {
        logAppRequestName(resources, id);
        if (id == 0) return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());

        try {
            return resources.getDrawableForDensity(id, density, theme);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());
        }
    }

    private static Drawable getFallbackDrawable(Resources resources, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            int fallbackId = findDrawableId(resources, TOOLBAR_FALLBACK_DRAWABLES);
            if (fallbackId != 0) {
                try {
                    return resources.getDrawable(fallbackId);
                } catch (Resources.NotFoundException ignored) {}
            }
        }
        return getTransparentDrawable();
    }

    private static Drawable getFallbackDrawableForDensity(Resources resources, int density, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            int fallbackId = findDrawableId(resources, TOOLBAR_FALLBACK_DRAWABLES);
            if (fallbackId != 0) {
                try {
                    return resources.getDrawableForDensity(fallbackId, density);
                } catch (Resources.NotFoundException ignored) {}
            }
        }
        return getTransparentDrawable();
    }

    private static Drawable getFallbackDrawableForDensity(Resources resources, int density, Resources.Theme theme, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            int fallbackId = findDrawableId(resources, TOOLBAR_FALLBACK_DRAWABLES);
            if (fallbackId != 0) {
                try {
                    return resources.getDrawableForDensity(fallbackId, density, theme);
                } catch (Resources.NotFoundException ignored) {}
            }
        }
        return getTransparentDrawable();
    }

    private static int findDrawableId(Resources resources, String[] names) {
        for (String name : names) {
            for (String resourcePackage : RESOURCE_PACKAGES) {
                int id = resources.getIdentifier(name, "drawable", resourcePackage);
                if (id != 0) return id;
            }
        }
        return 0;
    }

    private static boolean isToolbarMenuStack() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String methodName = element.getMethodName();
            if ("onCreateOptionsMenu".equals(methodName) || "onCreatePanelMenu".equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    private static Drawable getTransparentDrawable() {
        return new ColorDrawable(Color.TRANSPARENT);
    }
}