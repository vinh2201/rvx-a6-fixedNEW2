package app.revanced.extension.youtube.patches.utils;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public final class MissingResourcesPatch {
    private static final String TAG = "ReVancedIconLog";
    
    // VŨ KHÍ BÍ MẬT: MÓC NỐI ICON_TYPE VÀ DRAWABLE
    private static final ThreadLocal<Integer> lastIconTypeTracker = new ThreadLocal<>();

    private static final String[] TOOLBAR_FALLBACK_DRAWABLES = {
            "quantum_ic_more_vert_black_24",
            "ic_more_vert_black_24",
            "quantum_ic_more_vert_white_24"
    };
    
    private static final String[] RESOURCE_PACKAGES = {
            "com.google.android.youtube",
            null
    };

    // =========================================================================
    // VŨ KHÍ 1: TRUY VẾT SÂU HÀNG CHỜ
    // =========================================================================
    
    private static String getDeepCallerInfo() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StringBuilder trace = new StringBuilder();
        int count = 0;
        
        for (int i = 3; i < stack.length; i++) {
            String className = stack[i].getClassName();
            if (!className.startsWith("app.revanced") 
                    && !className.startsWith("java.lang") 
                    && !className.startsWith("android.content.res")
                    && !className.startsWith("dalvik.system")) {
                
                String simpleName = className.substring(className.lastIndexOf('.') + 1);
                trace.append(simpleName).append(".").append(stack[i].getMethodName());
                
                count++;
                if (count >= 3) break;
                trace.append(" <- ");
            }
        }
        return trace.length() > 0 ? trace.toString() : "Unknown";
    }

    // =========================================================================
    // VŨ KHÍ 2: GHI NHẬN ICON_TYPE TỪ SERVER & LOGIC DRAWABLE MÓC NỐI
    // =========================================================================

    public static int getLegacyIconType(int iconType) {
        // LƯU LẠI ICON TYPE NÀY VÀO LUỒNG HIỆN TẠI ĐỂ LÁT NỮA MÓC NỐI
        lastIconTypeTracker.set(iconType);

        int mappedType;
        boolean isMapped = true;

        switch (iconType) {
            case 1154: mappedType = 406; break; // Home
            case 1157: mappedType = 776; break; // Shorts
            case 1155: mappedType = 408; break; // Subscriptions
            case 158:  mappedType = 158; break; // Logo YouTube -> Ổn định
            case 0:    mappedType = 0;   break; // Cho qua

            // --- CÁC NÚT ĐANG TÀNG HÌNH (MAP CHẨN ĐOÁN) ---
            // Mục đích: Nhét icon Home/Shorts/Subs vào để ép chúng phải hiện hình!
        
            // Cụm Bottom Bar
            case 1160: mappedType = 406; break; // Dấu + -> Ép thành icon Home. (Xem giữa màn có mọc ra cái nhà ko)
            case 1156: mappedType = 776; break; // Tab Bạn -> Ép thành icon Shorts.
        
            // Cụm Top Bar (Chuông, Search, Cast)
            case 1161: mappedType = 408; break; // Chuông -> Ép thành icon Subs.
            case 181:  mappedType = 406; break; // Bí ẩn 1 -> Ép thành icon Home.
            case 192:  mappedType = 776; break; // Bí ẩn 2 -> Ép thành icon Shorts.
            case 967:  mappedType = 408; break; // Bí ẩn 3 -> Ép thành icon Subs.
            default:
                mappedType = iconType;
                isMapped = false;
                break;
        }

        String caller = getDeepCallerInfo();
        if (isMapped) {
            Log.i(TAG, "✅ [ENUM_MAPPED] Server: " + iconType + " -> Legacy: " + mappedType + " | Nguồn: " + caller);
        } else {
            Log.e(TAG, "🔥 [UNMAPPED_ENUM] SERVER BẮN ID MỚI: " + iconType + " | Chờ xem nó móc nối với ảnh nào... | Nguồn: " + caller);
        }
        return mappedType;
    }

    private static String getLinkedIconTypeStr() {
        Integer linkedType = lastIconTypeTracker.get();
        if (linkedType != null) {
            lastIconTypeTracker.remove(); 
            return " 🔗 [Từ IconType Server: " + linkedType + "]";
        }
        return "";
    }

    public static void logIdentifierRequest(String name, String defType, String defPackage) {
        if ("drawable".equals(defType) || "mipmap".equals(defType)) {
            Log.d(TAG, "🔍 [STRING_SEARCH] Tìm tên: '" + name + "'" + getLinkedIconTypeStr() + " | Nguồn: " + getDeepCallerInfo());
        }
    }

    public static void logSetImageResource(int resId, Context context) {
        if (resId == 0) return;
        String hexId = String.format("0x%08X", resId);
        String resName = "(Unknown)";
        if (context != null) {
            try { resName = context.getResources().getResourceEntryName(resId); } catch (Exception ignored) {}
        }
        Log.w(TAG, "🎯 [SET_IMAGE_RES] Bắn ID: " + resId + " (" + hexId + ") -> " + resName + getLinkedIconTypeStr() + " | Luồng: " + getDeepCallerInfo());
    }

    private static void logDrawableInfo(Resources resources, int id, String contextName) {
        boolean isToolbar = isToolbarMenuStack();
        String tagPrefix = isToolbar ? "🧲 [TOOLBAR]" : "📦 [DRAWABLE]";
        String caller = getDeepCallerInfo();
        String linkStr = getLinkedIconTypeStr();
        
        if (id == 0) {
            Log.v(TAG, "⏳ [QUEUE] " + tagPrefix + " ID = 0" + linkStr + " | Nguồn: " + caller);
            return;
        }

        String hexId = String.format("0x%08X", id);
        try {
            String resName = resources.getResourceEntryName(id);
            Log.d(TAG, tagPrefix + " Req: " + id + " (" + hexId + ") -> Res: " + resName + linkStr + " | Luồng: " + caller);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "❌ [MISSING_RES] " + tagPrefix + " ID: " + id + " (" + hexId + ") KHÔNG TỒN TẠI!" + linkStr + " | Luồng: " + caller);
        }
    }

    // =========================================================================
    // CÁC HÀM GET DRAWABLE BẢN LỀ CỦA HỆ THỐNG
    // =========================================================================
    
    public static Drawable getTransparentDrawable(Context context) {
        return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
    }

    public static Drawable getDrawable(Context context, int id) {
        logDrawableInfo(context.getResources(), id, "Context");
        if (id == 0) return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
        try { return context.getDrawable(id); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawable(context.getResources(), isToolbarMenuStack()); }
    }

    public static Drawable getDrawable(Resources resources, int id) {
        logDrawableInfo(resources, id, "Resources");
        if (id == 0) return getFallbackDrawable(resources, isToolbarMenuStack());
        try { return resources.getDrawable(id); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawable(resources, isToolbarMenuStack()); }
    }

    public static Drawable getDrawable(Resources resources, int id, Resources.Theme theme) {
        logDrawableInfo(resources, id, "Resources_Theme");
        if (id == 0) return getFallbackDrawable(resources, isToolbarMenuStack());
        try { return resources.getDrawable(id, theme); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawable(resources, isToolbarMenuStack()); }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density) {
        logDrawableInfo(resources, id, "Density");
        if (id == 0) return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack());
        try { return resources.getDrawableForDensity(id, density); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack()); }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density, Resources.Theme theme) {
        logDrawableInfo(resources, id, "Density_Theme");
        if (id == 0) return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());
        try { return resources.getDrawableForDensity(id, density, theme); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack()); }
    }

    private static Context getApplicationContext() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentApplicationMethod = activityThreadClass.getDeclaredMethod("currentApplication");
            currentApplicationMethod.setAccessible(true);
            return (Application) currentApplicationMethod.invoke(null);
        } catch (Exception ignored) { return null; }
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