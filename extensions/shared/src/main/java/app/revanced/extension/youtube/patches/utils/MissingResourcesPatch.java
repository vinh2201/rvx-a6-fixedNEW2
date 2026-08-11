package app.revanced.extension.youtube.patches.utils;

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
    
    // CHỈ THEO DÕI ĐÚNG NHỮNG THẰNG CẦN THIẾT, CÒN LẠI THẢ TỰ DO
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
    // LỌC CHUẨN XÁC: CHỈ GẮN KẾT CHO CÁC CASE ID ĐIỀU HƯỚNG, CÒN LẠI THẢ TỰ DO
    // =========================================================================
    public static int getLegacyIconType(int iconType) {
        int mappedType;
        boolean shouldTrack = true;

        switch (iconType) {
            case 1154: mappedType = 406; break; // Home
            case 1157: mappedType = 776; break; // Shorts
            case 1155: mappedType = 408; break; // Subscriptions
            case 158:  mappedType = 158; break; // Logo YouTube
            case 181:  mappedType = 406; break; // Search -> Mượn tạm Home
            case 192:  mappedType = 776; break; // Chuông -> Mượn tạm Shorts
            case 1161: mappedType = 408; break; // Nút Tạo -> Mượn tạm Subscriptions
            default:
                // Mọi icon khác trong app (lịch sử, tải xuống, v.v.): Trả về chính nó và THẢ TỰ DO!
                mappedType = iconType;
                shouldTrack = false;
                break;
        }

        if (shouldTrack) {
            lastIconTypeTracker.set(iconType);
        } else {
            lastIconTypeTracker.remove(); // Xóa sạch dấu vết để không ảnh hưởng chỗ khác
        }

        return mappedType;
    }

    private static String getLinkedIconTypeStr() {
        Integer linkedType = lastIconTypeTracker.get();
        if (linkedType != null) {
            return " 🔗 [Từ Server ID: " + linkedType + "]";
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
    // ÉP ĐÚNG CHỖ, DÙNG XONG TIÊU HÓA NGAY LẬP TỨC
    // =========================================================================
    private static int getOverriddenDrawableId(Resources resources, int originalId) {
        Integer serverType = lastIconTypeTracker.get();
        if (serverType == null) return originalId;

        // Tiêu hóa ngay lập tức để trả lại sự tự do cho các icon phía sau
        lastIconTypeTracker.remove();

        String targetName = null;
        switch (serverType) {
            case 181:  targetName = "yt_outline_search_black_24"; break;
            case 192:  targetName = "yt_outline_bell_black_24"; break;
            case 1161: targetName = "yt_outline_plus_black_24"; break;
            case 1154: targetName = "yt_outline_home_black_24"; break;
            case 1157: targetName = "yt_outline_youtube_shorts_black_24"; break;
            case 1155: targetName = "yt_outline_subscriptions_black_24"; break;
            default: break;
        }

        if (targetName != null) {
            for (String pkg : RESOURCE_PACKAGES) {
                int customId = resources.getIdentifier(targetName, "drawable", pkg);
                if (customId != 0) {
                    return customId; 
                }
            }
        }
        return originalId;
    }

    public static Drawable getTransparentDrawable(Context context) {
        return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
    }

    public static Drawable getDrawable(Context context, int id) {
        int finalId = getOverriddenDrawableId(context.getResources(), id);
        logDrawableInfo(context.getResources(), finalId, "Context");
        if (finalId == 0) return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
        try { return context.getDrawable(finalId); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawable(context.getResources(), isToolbarMenuStack()); }
    }

    public static Drawable getDrawable(Resources resources, int id) {
        int finalId = getOverriddenDrawableId(resources, id);
        logDrawableInfo(resources, finalId, "Resources");
        // ĐÃ SỬA context.getResources() THÀNH resources Ở DƯỚI ĐÂY
        if (finalId == 0) return getFallbackDrawable(resources, isToolbarMenuStack());
        try { return resources.getDrawable(finalId); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawable(resources, isToolbarMenuStack()); }
    }

    public static Drawable getDrawable(Resources resources, int id, Resources.Theme theme) {
        int finalId = getOverriddenDrawableId(resources, id);
        logDrawableInfo(resources, finalId, "Resources_Theme");
        // ĐÃ SỬA context.getResources() THÀNH resources Ở DƯỚI ĐÂY
        if (finalId == 0) return getFallbackDrawable(resources, isToolbarMenuStack());
        try { return resources.getDrawable(finalId, theme); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawable(resources, isToolbarMenuStack()); }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density) {
        int finalId = getOverriddenDrawableId(resources, id);
        logDrawableInfo(resources, finalId, "Density");
        if (finalId == 0) return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack());
        try { return resources.getDrawableForDensity(finalId, density); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack()); }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density, Resources.Theme theme) {
        int finalId = getOverriddenDrawableId(resources, id);
        logDrawableInfo(resources, finalId, "Density_Theme");
        if (finalId == 0) return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());
        try { return resources.getDrawableForDensity(finalId, density, theme); } 
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