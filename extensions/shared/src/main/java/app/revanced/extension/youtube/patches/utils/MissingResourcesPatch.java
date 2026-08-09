package app.revanced.extension.youtube.patches.utils; // TODO: Đổi package name này cho khớp với project Kitadai31 của bạn

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

@SuppressWarnings("unused")
public final class MissingResourcesPatch {
    private static final int SETTINGS_ICON_TYPE = 44;
    private static final int SETTINGS_CAIRO_ICON_TYPE = 1162;

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

        public static int getLegacyIconType(int iconType) {
        switch (iconType) {
            case 1154: return 406; // Trang chủ (Home)
            case 1155: return 408; // Kênh đăng ký (Subscriptions)
            case 1156: return 410; // Thư viện / Bạn (Library / You)
            case 1157: return 776; // Shorts
            case 1158: 
            case 1159: return 135; // Tìm kiếm (Search)
            case 1160: return 181; // Nút Tạo (+)
            case 1161: return 158; // Thông báo (Notifications)
            case 1162: return 44;  // Cài đặt (Settings)
            case 1163:
            case 1164: 
            case 1165: return 67;  // Truyền (Cast)
            default:   return iconType;
        }
    }

    private static Drawable getFallbackDrawable(Resources resources, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            int fallbackId = findDrawableId(resources, TOOLBAR_FALLBACK_DRAWABLES);
            if (fallbackId != 0) {
                try {
                    return resources.getDrawable(fallbackId);
                } catch (Resources.NotFoundException ignored) {
                    // Fall through to transparent drawable.
                }
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
                } catch (Resources.NotFoundException ignored) {
                    // Fall through to transparent drawable.
                }
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
                } catch (Resources.NotFoundException ignored) {
                    // Fall through to transparent drawable.
                }
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