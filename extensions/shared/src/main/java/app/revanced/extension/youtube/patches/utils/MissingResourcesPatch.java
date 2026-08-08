package app.revanced.extension.youtube.patches.utils; // Nhớ đổi lại package name của bạn

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
    
    // Bộ nhớ chống spam log
    private static final Set<Integer> LOGGED_ICON_TYPES = Collections.synchronizedSet(new HashSet<>());
    private static final Set<Integer> LOGGED_DRAWABLE_IDS = Collections.synchronizedSet(new HashSet<>());

    // CHIÊU BÀI TỐI THƯỢNG: Lưu lại số Cairo cuối cùng server vừa gọi
    private static volatile int lastRequestedIconType = -1;

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
    // TRẠM 1: LƯU DẤU VẾT & CHUYỂN ĐỔI SỐ CŨ (NẾU BIẾT)
    // ---------------------------------------------------------
    public static int getLegacyIconType(int iconType) {
        lastRequestedIconType = iconType; // <--- LƯU LẠI VẾT CHÂN CỦA SERVER!

        if (LOGGED_ICON_TYPES.add(iconType)) {
            if (iconType > 1000) Log.w(TAG, "📡 [SERVER GỌI SỐ MỚI] IconType Cairo: " + iconType);
            else Log.d(TAG, "📡 [SERVER GỌI SỐ CŨ] IconType Legacy: " + iconType);
        }

        switch (iconType) {
            case 1154: return 406; // Home
            case 1157: return 776; // Shorts
            case 1155: return 408; // Subs
            case 1156: return 410; // Library
            case 1160: return 181; // Create (+)
            case 1161: return 158; // Bell
            case 1162: return 44;  // Settings
            
            // Cứ kệ 1239 và 1021 rơi vào default, ta sẽ xử chúng nó ở Trạm 3!
            default:
                return iconType;
        }
    }

    // ---------------------------------------------------------
    // TRẠM TỘT ĐỈNH (MỚI): ÉP THẲNG SỐ CAIRO THÀNH TÊN ẢNH
    // ---------------------------------------------------------
    private static Drawable resolveCairoToDrawable(Resources resources, int cairoType) {
        String exactDrawableName = null;

        if (cairoType == 1239) {
            exactDrawableName = "yt_outline_search_black_24"; // Chốt đơn Kính lúp!
        } else if (cairoType == 1021) {
            exactDrawableName = "yt_outline_chromecast_black_24"; // Nghi vấn Cast (bạn check thử xem đúng không nhé)
        }
        // Tương lai lòi ra số lạ nào, bạn cứ vã thêm 'else if' vào đây, KHÔNG TRƯỢT ĐƯỢC!

        if (exactDrawableName != null) {
            int resId = findDrawableId(resources, new String[]{exactDrawableName});
            if (resId != 0) {
                try {
                    Log.i(TAG, "🎯 [HACK THÀNH CÔNG] Đã ép số " + cairoType + " biến thành: " + exactDrawableName);
                    return resources.getDrawable(resId);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    // ---------------------------------------------------------
    // TRẠM 2: VẼ ẢNH & ĐÁNH CHẶN LỖI ID = 0
    // ---------------------------------------------------------
    private static void logAppRequestName(Resources resources, int id) {
        if (id != 0 && LOGGED_DRAWABLE_IDS.add(id)) {
            try {
                String name = resources.getResourceEntryName(id);
                
                // MỞ RỘNG LƯỚI QUÉT: Tóm gọn yt_, abc_, ic_ và cả quantum_
                if (name.startsWith("yt_") || 
                    name.startsWith("abc_") || 
                    name.startsWith("ic_") || 
                    name.startsWith("quantum_")) {
                    
                    Log.i(TAG, "🔎 [APP TÌM TÊN FILE] Đang vẽ icon tên: " + name + " (Mã ID: " + id + ")");
                }
            } catch (Exception ignored) {}
        }
    }

    public static Drawable getTransparentDrawable(Context context) {
        return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
    }

    public static Drawable getDrawable(Context context, int id) {
        logAppRequestName(context.getResources(), id);
        
        // NẾU APP TÌM KHÔNG RA (ID = 0) -> GỌI HÀM ÉP HÌNH CỦA CHÚNG TA RA!
        if (id == 0) {
            Drawable forcedDrawable = resolveCairoToDrawable(context.getResources(), lastRequestedIconType);
            if (forcedDrawable != null) return forcedDrawable;
            return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
        }

        try {
            return context.getDrawable(id);
        } catch (Resources.NotFoundException ex) {
            if (!LOGGED_DRAWABLE_IDS.contains(id)) {
                Log.e(TAG, "💀 [CRASH TRƯỢT] Mất file ID: " + id + " -> Đang nhét icon dự phòng!");
                LOGGED_DRAWABLE_IDS.add(id);
            }
            return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
        }
    }

    public static Drawable getDrawable(Resources resources, int id) {
        logAppRequestName(resources, id);
        if (id == 0) {
            Drawable forcedDrawable = resolveCairoToDrawable(resources, lastRequestedIconType);
            if (forcedDrawable != null) return forcedDrawable;
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }

        try { return resources.getDrawable(id); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawable(resources, isToolbarMenuStack()); }
    }

    public static Drawable getDrawable(Resources resources, int id, Resources.Theme theme) {
        logAppRequestName(resources, id);
        if (id == 0) {
            Drawable forcedDrawable = resolveCairoToDrawable(resources, lastRequestedIconType);
            if (forcedDrawable != null) return forcedDrawable;
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }

        try { return resources.getDrawable(id, theme); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawable(resources, isToolbarMenuStack()); }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density) {
        logAppRequestName(resources, id);
        if (id == 0) {
            Drawable forcedDrawable = resolveCairoToDrawable(resources, lastRequestedIconType);
            if (forcedDrawable != null) return forcedDrawable;
            return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack());
        }

        try { return resources.getDrawableForDensity(id, density); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack()); }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density, Resources.Theme theme) {
        logAppRequestName(resources, id);
        if (id == 0) {
            Drawable forcedDrawable = resolveCairoToDrawable(resources, lastRequestedIconType);
            if (forcedDrawable != null) return forcedDrawable;
            return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());
        }

        try { return resources.getDrawableForDensity(id, density, theme); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack()); }
    }

    private static Drawable getFallbackDrawable(Resources resources, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            int fallbackId = findDrawableId(resources, TOOLBAR_FALLBACK_DRAWABLES);
            if (fallbackId != 0) {
                try { return resources.getDrawable(fallbackId); } catch (Resources.NotFoundException ignored) {}
            }
        }
        return getTransparentDrawable();
    }

    private static Drawable getFallbackDrawableForDensity(Resources resources, int density, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            int fallbackId = findDrawableId(resources, TOOLBAR_FALLBACK_DRAWABLES);
            if (fallbackId != 0) {
                try { return resources.getDrawableForDensity(fallbackId, density); } catch (Resources.NotFoundException ignored) {}
            }
        }
        return getTransparentDrawable();
    }

    private static Drawable getFallbackDrawableForDensity(Resources resources, int density, Resources.Theme theme, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            int fallbackId = findDrawableId(resources, TOOLBAR_FALLBACK_DRAWABLES);
            if (fallbackId != 0) {
                try { return resources.getDrawableForDensity(fallbackId, density, theme); } catch (Resources.NotFoundException ignored) {}
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