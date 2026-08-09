package app.revanced.extension.youtube.patches.utils; // TODO: Đổi package name này cho khớp với project Kitadai31 của bạn

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

@SuppressWarnings("unused")
public final class MissingResourcesPatch {
    private static final String TAG = "ReVancedIconLog";
    
    // Cầu nối tĩnh lưu lại Server IconType gần nhất vừa được gọi
    private static volatile int lastServerIconType = -1;

    private static final String[] TOOLBAR_FALLBACK_DRAWABLES = {
            "quantum_ic_more_vert_black_24",
            "ic_more_vert_black_24",
            "quantum_ic_more_vert_white_24"
    };
    
    private static final String[] RESOURCE_PACKAGES = {
            "com.google.android.youtube",
            null
    };

    private static void logDrawableInfo(Resources resources, int id, String contextName) {
        boolean isToolbar = isToolbarMenuStack();
        String toolbarFlag = isToolbar ? "[TOOLBAR]" : "[OTHER]";
        
        if (id == 0) {
            Log.d(TAG, toolbarFlag + " [" + contextName + "] ID = 0 (Triggered Fallback) | Server IconType: " + lastServerIconType);
            return;
        }
        try {
            String resName = resources.getResourceEntryName(id);
            Log.d(TAG, toolbarFlag + " [" + contextName + "] Requested ID: " + id 
                    + " [Server IconType: " + lastServerIconType + "] | Resolved Name: " + resName);
        } catch (Resources.NotFoundException e) {
            Log.d(TAG, toolbarFlag + " [" + contextName + "] Requested ID: " + id 
                    + " [Server IconType: " + lastServerIconType + "] | Resolved Name: (Not Found - lệch ID)");
        }
    }

    public static Drawable getTransparentDrawable(Context context) {
        return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
    }

    public static Drawable getDrawable(Context context, int id) {
        logDrawableInfo(context.getResources(), id, "getDrawable_Context");
        if (id == 0) {
            return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
        }

        try {
            return context.getDrawable(id);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
        }
    }

    public static Drawable getDrawable(Resources resources, int id) {
        logDrawableInfo(resources, id, "getDrawable_Resources");
        if (id == 0) {
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }

        try {
            return resources.getDrawable(id);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }
    }

    public static Drawable getDrawable(Resources resources, int id, Resources.Theme theme) {
        logDrawableInfo(resources, id, "getDrawable_Theme");
        if (id == 0) {
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }

        try {
            return resources.getDrawable(id, theme);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density) {
        logDrawableInfo(resources, id, "getDrawableForDensity");
        if (id == 0) {
            return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack());
        }

        try {
            return resources.getDrawableForDensity(id, density);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack());
        }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density, Resources.Theme theme) {
        logDrawableInfo(resources, id, "getDrawableForDensity_Theme");
        if (id == 0) {
            return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());
        }

        try {
            return resources.getDrawableForDensity(id, density, theme);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());
        }
    }

    public static int getLegacyIconType(int iconType) {
        // Lưu vết Server IconType ngay khi app nhận được
        lastServerIconType = iconType;
        
        int mappedType;
        String label;
        
        switch (iconType) {
            case 1154: mappedType = 406; label = "Home (Trang chủ)"; break;
            case 1157: mappedType = 776; label = "Shorts"; break;
            case 1155: mappedType = 408; label = "Subscriptions (Kênh đăng ký)"; break;
            case 1156: mappedType = 410; label = "Library / You (Thư viện)"; break;
            case 1160: mappedType = 181; label = "Create (+) (Tạo)"; break;
            case 1161: mappedType = 158; label = "Notifications (Thông báo)"; break;
            case 1162: mappedType = 44;  label = "Settings (Cài đặt)"; break;
            default:   
                mappedType = iconType; 
                label = "UNKNOWN / CHƯA XÁC ĐỊNH"; 
                break;
        }
        
        Log.d(TAG, "[IconMapping] Type: " + iconType + " [" + label + "] -> Mapped Legacy: " + mappedType + " | IsToolbar: " + isToolbarMenuStack());

        if (label.contains("UNKNOWN")) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StringBuilder sb = new StringBuilder("[CallStack Trace for Unknown IconType]: ");
            for (int i = 3; i < Math.min(8, stackTrace.length); i++) {
                sb.append("\n    -> ").append(stackTrace[i].getClassName()).append(".").append(stackTrace[i].getMethodName()).append("(").append(stackTrace[i].getLineNumber()).append(")");
            }
            Log.d(TAG, sb.toString());
        }

        return mappedType;
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
                if (id != 0) {
                    return id;
                }
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