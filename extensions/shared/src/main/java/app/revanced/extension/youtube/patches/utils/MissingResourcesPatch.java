package app.revanced.extension.youtube.patches.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

@SuppressWarnings("unused")
public final class MissingResourcesPatch {

    private static final String[] RESOURCE_PACKAGES = {
            "com.google.android.youtube",
            null
    };

    public static Drawable getTransparentDrawable(Context context) {
        return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
    }

    public static Drawable getDrawable(Context context, int id) {
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
        if (id == 0) {
            return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());
        }
        try {
            return resources.getDrawableForDensity(id, density, theme);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());
        }
    }

    // ---------------------------------------------------------
    // BẢNG MAP ENUM CAIRO AN TOÀN (Giữ nguyên các tab chính)
    // ---------------------------------------------------------
    public static int getLegacyIconType(int iconType) {
        switch (iconType) {
            case 1154: return 406; // Home
            case 1157: return 776; // Shorts
            case 1155: return 408; // Subscriptions
            case 1156: return 410; // Library / You
            case 1160: return 181; // Create (+)
            
            // TUYỆT ĐỐI KHÔNG ÉP CÁC ICON TOPBAR (Thông báo, Tìm kiếm, Cài đặt...) 
            // VỀ SỐ LEGACY NỮA ĐỂ TRÁNH BỊ GÁN NHẦM ICON!
            // Hãy để app gọi trực tiếp tên Cairo đã được ta map alias trong drawables.xml
            default:   
                return iconType; 
        }
    }

    // ---------------------------------------------------------
    // CƠ CHẾ FALLBACK DỰA TRÊN TÊN GỐC (DIRECT NAME RESOLUTION)
    // ---------------------------------------------------------
    private static Drawable getFallbackDrawable(Resources resources, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            // Thay vì dùng danh sách chung chung, ta ưu tiên truy vấn thẳng tên icon gốc trong APK
            String[] specificIcons = {
                "yt_outline_search_black_24", 
                "yt_outline_bell_black_24", 
                "yt_outline_settings_black_24",
                "quantum_ic_more_vert_black_24"
            };
            
            for (String iconName : specificIcons) {
                int fallbackId = findDrawableIdByName(resources, iconName);
                if (fallbackId != 0) {
                    try {
                        return resources.getDrawable(fallbackId);
                    } catch (Resources.NotFoundException ignored) {}
                }
            }
        }

        return new ColorDrawable(Color.TRANSPARENT);
    }

    private static Drawable getFallbackDrawableForDensity(Resources resources, int density, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            String[] specificIcons = {
                "yt_outline_search_black_24", 
                "yt_outline_bell_black_24", 
                "yt_outline_settings_black_24",
                "quantum_ic_more_vert_black_24"
            };
            
            for (String iconName : specificIcons) {
                int fallbackId = findDrawableIdByName(resources, iconName);
                if (fallbackId != 0) {
                    try {
                        return resources.getDrawableForDensity(fallbackId, density);
                    } catch (Resources.NotFoundException ignored) {}
                }
            }
        }

        return new ColorDrawable(Color.TRANSPARENT);
    }

    private static Drawable getFallbackDrawableForDensity(Resources resources, int density, Resources.Theme theme, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            String[] specificIcons = {
                "yt_outline_search_black_24", 
                "yt_outline_bell_black_24", 
                "yt_outline_settings_black_24",
                "quantum_ic_more_vert_black_24"
            };
            
            for (String iconName : specificIcons) {
                int fallbackId = findDrawableIdByName(resources, iconName);
                if (fallbackId != 0) {
                    try {
                        return resources.getDrawableForDensity(fallbackId, density, theme);
                    } catch (Resources.NotFoundException ignored) {}
                }
            }
        }

        return new ColorDrawable(Color.TRANSPARENT);
    }

    private static int findDrawableIdByName(Resources resources, String name) {
        for (String resourcePackage : RESOURCE_PACKAGES) {
            int id = resources.getIdentifier(name, "drawable", resourcePackage);
            if (id != 0) {
                return id;
            }
        }
        return 0;
    }

    private static boolean isToolbarMenuStack() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String methodName = element.getMethodName();
            if ("onCreateOptionsMenu".equals(methodName) || "onCreatePanelMenu".equals(methodName) || methodName.contains("Toolbar")) {
                return true;
            }
        }
        return false;
    }
}