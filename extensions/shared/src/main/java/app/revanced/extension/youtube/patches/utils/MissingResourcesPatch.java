package app.revanced.extension.youtube.patches.utils; // TODO: Đổi package name này cho khớp với project Kitadai31 của bạn

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

@SuppressWarnings("unused")
public final class MissingResourcesPatch {
    private static final int SETTINGS_ICON_TYPE = 44;
    private static final int SETTINGS_CAIRO_ICON_TYPE = 1162;

    private static final String[] TOOLBAR_FALLBACK_DRAWABLES = {
            "yt_outline_more_vert_black_24",
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

    // ---------------------------------------------------------
    // HÀM CỐT LÕI: TỰ ĐỘNG ÉP XUỐNG ICON CŨ BẰNG CÁCH CHẶN TÊN
    // ---------------------------------------------------------
    private static int getDowngradedResourceId(Resources resources, String packageName, int originalId) {
        if (originalId == 0) return 0;
        try {
            String resName = resources.getResourceEntryName(originalId);
            
            // Nếu YouTube gọi icon giao diện mới (Cairo)
            if (resName != null && resName.contains("_cairo_")) {
                // Xóa chữ cairo đi (VD: yt_outline_bell_cairo_black_24 -> yt_outline_bell_black_24)
                String legacyResName = resName.replace("_cairo_", "_");
                int legacyId = resources.getIdentifier(legacyResName, "drawable", packageName);
                
                // Nếu tìm thấy icon cũ trong app, trả về ID của icon cũ
                if (legacyId != 0) {
                    Log.e("KhoaBug_Resource", "DA TRAO DOI: " + resName + " -> " + legacyResName);
                    return legacyId;
                }
            }
        } catch (Exception e) {
            // Bỏ qua lỗi
        }
        return originalId; // Nếu không phải cairo hoặc không tìm thấy, giữ nguyên ID gốc
    }
    // ---------------------------------------------------------


    public static Drawable getDrawable(Context context, int id) {
        if (id == 0) {
            return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
        }

        try {
            // ÉP SỬ DỤNG ID CŨ NẾU CÓ
            int targetId = getDowngradedResourceId(context.getResources(), context.getPackageName(), id);
            return context.getDrawable(targetId);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
        }
    }

    public static Drawable getDrawable(Resources resources, int id) {
        if (id == 0) {
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }

        try {
            // (Hack) Vì hàm này không có context, ta giả định package name của YouTube
            int targetId = getDowngradedResourceId(resources, "com.google.android.youtube", id);
            return resources.getDrawable(targetId);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }
    }

    public static Drawable getDrawable(Resources resources, int id, Resources.Theme theme) {
        if (id == 0) {
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }

        try {
            int targetId = getDowngradedResourceId(resources, "com.google.android.youtube", id);
            return resources.getDrawable(targetId, theme);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density) {
        if (id == 0) {
            return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack());
        }

        try {
            int targetId = getDowngradedResourceId(resources, "com.google.android.youtube", id);
            return resources.getDrawableForDensity(targetId, density);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack());
        }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density, Resources.Theme theme) {
        if (id == 0) {
            return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());
        }

        try {
            int targetId = getDowngradedResourceId(resources, "com.google.android.youtube", id);
            return resources.getDrawableForDensity(targetId, density, theme);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());
        }
    }

    public static int getLegacyIconType(int iconType) {
        // CẬP NHẬT MÃ ICONTYPE CHO THANH BOTTOM NAV CHUẨN NHẤT
        switch (iconType) {
            case 1154: return 406; // Trang chủ (Home)
            case 1157: return 776; // Shorts
            case 1155: return 408; // Kênh đăng ký (Subscriptions)
            case 1156: return 410; // Thư viện / Bạn (Library / You) - Có thể là 834 tùy bản cập nhật
            case 1160: return 181; // Nút Tạo (+)
            case 1162: return 44;  // Cài đặt (Settings)
            case 1158: return 777; // Shorts (variations)
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