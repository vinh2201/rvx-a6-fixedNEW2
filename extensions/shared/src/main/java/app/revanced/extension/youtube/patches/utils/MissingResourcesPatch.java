package app.revanced.extension.youtube.patches.utils; // TODO: Đổi package name

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.lang.reflect.Method;

@SuppressWarnings("unused")
public final class MissingResourcesPatch {
    private static final String TAG = "ReVancedIconLog";
    
    private static final String[] TOOLBAR_FALLBACK_DRAWABLES = {
            "quantum_ic_more_vert_black_24",
            "ic_more_vert_black_24",
            "quantum_ic_more_vert_white_24"
    };
    
    private static final String[] RESOURCE_PACKAGES = {
            "com.google.android.youtube",
            null
    };

    /**
     * MÁY QUÉT SIÊU ÂM: Truy xuất phả hệ gọi hàm sâu 25 tầng
     * Sẽ lật tẩy chính xác class nào của YouTube gọi ra cái Drawable này.
     */
    private static String getDeepStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder();
        sb.append("\n⬇️ ====== DEEP STACK TRACE (QUÉT TẦNG SÂU) ====== ⬇️\n");
        int count = 0;
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            
            // Lọc bớt các class hệ thống vô nghĩa để log sạch sẽ hơn
            if (className.startsWith("dalvik.system") || 
                className.startsWith("java.lang.Thread") ||
                className.startsWith("app.revanced")) {
                continue;
            }
            
            sb.append("\t🔍 ").append(className)
              .append(".").append(element.getMethodName())
              .append(" (Dòng: ").append(element.getLineNumber()).append(")\n");
            
            count++;
            if (count > 25) break; // Đào sâu 25 tầng là lòi ra hết
        }
        sb.append("⬆️ ================================================ ⬆️");
        return sb.toString();
    }

    private static String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 3; i < stackTrace.length; i++) {
            String className = stackTrace[i].getClassName();
            if (!className.startsWith("app.revanced") 
                    && !className.startsWith("java.lang") 
                    && !className.startsWith("android.content.res")) {
                String simpleName = className.substring(className.lastIndexOf('.') + 1);
                return simpleName + "." + stackTrace[i].getMethodName();
            }
        }
        return "UnknownCaller";
    }

    /**
     * RADAR KÍCH HOẠT MÁY QUÉT SÂU KHI GẶP ICON TÌM KIẾM/THÔNG BÁO
     */
    private static void logAndInterceptDrawable(Resources resources, int id, String contextName) {
        boolean isToolbar = isToolbarMenuStack();
        String tagPrefix = isToolbar ? "🧲 [TOOLBAR]" : "📦 [DRAWABLE]";
        String caller = getCallerInfo();
        
        if (id == 0) return;

        String hexId = String.format("0x%08X", id);
        try {
            String resName = resources.getResourceEntryName(id);
            
            // 🚨 BẮT ĐÚNG MẤY THẰNG TOP BAR ĐỂ ĐÀO SÂU
            if (resName.contains("search") || resName.contains("bell") || resName.contains("notif")) {
                Log.e(TAG, "🚨 [TRÚNG MỤC TIÊU] Phát hiện đang load: '" + resName + "' | Hex ID: " + hexId);
                // Phóng luồng sóng âm quét sâu xuống các class YouTube
                Log.e(TAG, getDeepStackTrace());
            } else {
                // Các icon bình thường thì chỉ log nhẹ
                Log.d(TAG, tagPrefix + " ID: " + id + " (" + hexId + ") -> Found: " + resName);
            }
            
        } catch (Resources.NotFoundException e) {
            // Im lặng bỏ qua lỗi NotFound ở log này cho đỡ rác
        }
    }

    public static int getLegacyIconType(int iconType) {
        int mappedType;
        boolean isMapped = true;

        switch (iconType) {
            case 1154: mappedType = 406; break; 
            case 1157: mappedType = 776; break; 
            case 1155: mappedType = 408; break; 
            case 1156: mappedType = 410; break; 
            case 1160: mappedType = 181; break; 
            case 1161: mappedType = 158; break; 
            case 1162: mappedType = 44;  break; 
            default:
                mappedType = iconType;
                isMapped = false;
                break;
        }

        String caller = getCallerInfo();
        if (isMapped) {
            Log.d(TAG, "✅ [MAPPED_ID] Server ID: " + iconType + " -> Legacy: " + mappedType + " | Từ: " + caller);
        } else {
            Log.w(TAG, "⚠️ [UNMAPPED_ID] Server rớt ID lạ: " + iconType + " | Từ: " + caller);
        }

        return mappedType;
    }

    // =========================================================================
    // HOOK DRAWABLE METHODS
    // =========================================================================

    public static Drawable getTransparentDrawable(Context context) {
        return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
    }

    public static Drawable getDrawable(Context context, int id) {
        logAndInterceptDrawable(context.getResources(), id, "Context");
        if (id == 0) return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
        try { return context.getDrawable(id); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawable(context.getResources(), isToolbarMenuStack()); }
    }

    public static Drawable getDrawable(Resources resources, int id) {
        logAndInterceptDrawable(resources, id, "Resources");
        if (id == 0) return getFallbackDrawable(resources, isToolbarMenuStack());
        try { return resources.getDrawable(id); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawable(resources, isToolbarMenuStack()); }
    }

    public static Drawable getDrawable(Resources resources, int id, Resources.Theme theme) {
        logAndInterceptDrawable(resources, id, "Resources_Theme");
        if (id == 0) return getFallbackDrawable(resources, isToolbarMenuStack());
        try { return resources.getDrawable(id, theme); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawable(resources, isToolbarMenuStack()); }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density) {
        logAndInterceptDrawable(resources, id, "Density");
        if (id == 0) return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack());
        try { return resources.getDrawableForDensity(id, density); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack()); }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density, Resources.Theme theme) {
        logAndInterceptDrawable(resources, id, "Density_Theme");
        if (id == 0) return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());
        try { return resources.getDrawableForDensity(id, density, theme); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack()); }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

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