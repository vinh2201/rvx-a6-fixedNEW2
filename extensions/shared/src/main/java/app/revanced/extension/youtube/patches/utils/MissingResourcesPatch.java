package app.revanced.extension.youtube.patches.utils; // TODO: Đổi package name này cho khớp với project Kitadai31

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

@SuppressWarnings("unused")
public final class MissingResourcesPatch {
    
    private static String cachedPackageName = "com.google.android.youtube";

    // Khôi phục mã Settings cho chuẩn xác
    private static final int SETTINGS_ICON_TYPE = 44;
    private static final int SETTINGS_CAIRO_ICON_TYPE = 1162;

    // ĐÃ XÓA KÍNH LÚP RA KHỎI ĐÂY ĐỂ TRÁNH LỖI PHÂN THÂN
    private static final String[] TOOLBAR_FALLBACK_DRAWABLES = {
            "yt_outline_more_vert_black_24",
            "ic_more_vert_black_24",
            "quantum_ic_more_vert_white_24"
    };

    // MẢNG TRUY LÙNG PACKAGE - Chìa khóa để không bị tàng hình logo
    private static final String[] RESOURCE_PACKAGES = {
            "com.google.android.youtube",
            "app.revanced.android.youtube",
            null
    };

    public static Drawable getTransparentDrawable(Context context) {
        return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
    }

    // ---------------------------------------------------------
    // HÀM CỐT LÕI: TRUY LÙNG RESOURCE ĐA PACKAGE
    // ---------------------------------------------------------
    private static int getDowngradedResourceId(Resources resources, int originalId) {
        if (originalId == 0) return 0;
        try {
            String resName = resources.getResourceEntryName(originalId);
            
            if (resName != null && resName.contains("_cairo_")) {
                String legacyResName = resName.replace("_cairo_", "_");
                
                // Quét qua tất cả các package có thể có để tìm bằng được icon cũ
                String[] searchPackages = { "com.google.android.youtube", cachedPackageName, "app.revanced.android.youtube", null };
                
                for (String pkg : searchPackages) {
                    int legacyId = resources.getIdentifier(legacyResName, "drawable", pkg);
                    if (legacyId != 0) {
                        Log.e("KhoaBug_Resource", "DA TRAO DOI THANH CONG: " + resName + " -> " + legacyResName + " (Package: " + pkg + ")");
                        return legacyId;
                    }
                }
            }
        } catch (Exception e) {
            // Bỏ qua lỗi
        }
        return originalId;
    }
    // ---------------------------------------------------------

    public static Drawable getDrawable(Context context, int id) {
        cachedPackageName = context.getPackageName();
        if (id == 0) return getFallbackDrawable(context.getResources(), isToolbarMenuStack());

        try {
            // Không truyền cachedPackageName vào hàm nữa, để hàm tự dùng mảng quét
            int targetId = getDowngradedResourceId(context.getResources(), id);
            return context.getDrawable(targetId);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
        }
    }

    public static Drawable getDrawable(Resources resources, int id) {
        if (id == 0) return getFallbackDrawable(resources, isToolbarMenuStack());

        try {
            int targetId = getDowngradedResourceId(resources, id);
            return resources.getDrawable(targetId);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }
    }

    public static Drawable getDrawable(Resources resources, int id, Resources.Theme theme) {
        if (id == 0) return getFallbackDrawable(resources, isToolbarMenuStack());

        try {
            int targetId = getDowngradedResourceId(resources, id);
            return resources.getDrawable(targetId, theme);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawable(resources, isToolbarMenuStack());
        }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density) {
        if (id == 0) return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack());

        try {
            int targetId = getDowngradedResourceId(resources, id);
            return resources.getDrawableForDensity(targetId, density);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack());
        }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density, Resources.Theme theme) {
        if (id == 0) return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());

        try {
            int targetId = getDowngradedResourceId(resources, id);
            return resources.getDrawableForDensity(targetId, density, theme);
        } catch (Resources.NotFoundException ex) {
            return getFallbackDrawableForDensity(resources, density, theme, isToolbarMenuStack());
        }
    }

    public static int getLegacyIconType(int iconType) {
        switch (iconType) {
            case 1154: return 406; // Trang chủ
            case 1157: return 776; // Shorts
            case 1155: return 408; // Đăng ký
            case 1156: return 410; // Thư viện
            case 1160: return 181; // Dấu +
            case 1158: return 777; // Shorts (variations)
            case 1162: return 44;  // Cài đặt (Bổ sung lại cho chuẩn)
            default:   return iconType;
        }
    }

    private static Drawable getFallbackDrawable(Resources resources, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            int fallbackId = findDrawableId(resources, TOOLBAR_FALLBACK_DRAWABLES);
            if (fallbackId != 0) {
                try { return resources.getDrawable(fallbackId); } 
                catch (Resources.NotFoundException ignored) {}
            }
        }
        return getTransparentDrawable();
    }

    private static Drawable getFallbackDrawableForDensity(Resources resources, int density, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            int fallbackId = findDrawableId(resources, TOOLBAR_FALLBACK_DRAWABLES);
            if (fallbackId != 0) {
                try { return resources.getDrawableForDensity(fallbackId, density); } 
                catch (Resources.NotFoundException ignored) {}
            }
        }
        return getTransparentDrawable();
    }

    private static Drawable getFallbackDrawableForDensity(Resources resources, int density, Resources.Theme theme, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            int fallbackId = findDrawableId(resources, TOOLBAR_FALLBACK_DRAWABLES);
            if (fallbackId != 0) {
                try { return resources.getDrawableForDensity(fallbackId, density, theme); } 
                catch (Resources.NotFoundException ignored) {}
            }
        }
        return getTransparentDrawable();
    }

    private static int findDrawableId(Resources resources, String[] names) {
        // Dùng mảng RESOURCE_PACKAGES mới để quét toàn diện
        String[] packagesToSearch = { cachedPackageName, "com.google.android.youtube", "app.revanced.android.youtube", null };
        
        for (String name : names) {
            for (String resourcePackage : packagesToSearch) {
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