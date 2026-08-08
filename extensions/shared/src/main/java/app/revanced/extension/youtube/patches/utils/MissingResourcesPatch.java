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
            "quantum_ic_more_vert_white_24"
    };
    
    private static final String[] RESOURCE_PACKAGES = {
            "com.google.android.youtube",
            null
    };

    public static Drawable getTransparentDrawable(Context context) {
        return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
    }

    // ---------------------------------------------------------
    // HÀM CỐT LÕI: TỰ ĐỘNG ĐỔI TÊN CAIRO VÀ "TRÁO HÀNG ĐÍCH DANH"
    // ---------------------------------------------------------
    private static int getDowngradedResourceId(Resources resources, String packageName, int originalId) {
        if (originalId == 0) return 0;
        try {
            String resName = resources.getResourceEntryName(originalId);
            if (resName == null) return originalId;

            String mappedLegacyName = null;

            // 1. TRÁO HÀNG ĐÍCH DANH (Dùng .equals để bắt chính xác 100%, không bắt nhầm)
            // Sửa nút Tìm kiếm (bị nhầm thành Reload / Sync)
            if (resName.equals("yt_outline_reload_black_24") || resName.equals("yt_outline_sync_black_24")) {
                mappedLegacyName = "yt_outline_search_black_24";
            } 
            // Sửa nút Thông báo (bị nhầm thành Autoplay)
            else if (resName.equals("yt_outline_autoplay_black_24") || resName.equals("yt_fill_autoplay_black_24")) {
                mappedLegacyName = "yt_outline_bell_black_24";
            }
            // (Nếu nút Cast bị biến thành hình khác, bạn có thể soi Logcat để thêm 1 dòng else if tương tự vào đây)

            String[] packagesToSearch = { packageName, "com.google.android.youtube", null };

            // Nếu có hàng thủ công, ưu tiên tráo ngay lập tức
            if (mappedLegacyName != null) {
                for (String pkg : packagesToSearch) {
                    int legacyId = resources.getIdentifier(mappedLegacyName, "drawable", pkg);
                    if (legacyId != 0) {
                        Log.e("KhoaBug_Resource", "DA BAN TIA ICON: " + resName + " -> " + mappedLegacyName);
                        return legacyId;
                    }
                }
            }
            
            // 2. XỬ LÝ TỰ ĐỘNG CHO CÁC ICON CÓ CHỮ _cairo_ (Fix triệt để lỗi sinh ra __black)
            if (resName.contains("_cairo_")) {
                String legacyResName = resName.replace("_cairo_", ""); 
                
                for (String pkg : packagesToSearch) {
                    int legacyId = resources.getIdentifier(legacyResName, "drawable", pkg);
                    if (legacyId != 0) {
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
        if (id == 0) {
            return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
        }

        try {
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

    // MAP CÁC MÃ ĐÃ BIẾT Ở ĐÂY ĐỂ ĐẢM BẢO CHUẨN BACKEND
    public static int getLegacyIconType(int iconType) {
        switch (iconType) {
            case 1154: return 406; // Trang chủ (Home)
            case 1157: return 776; // Shorts
            case 1155: return 408; // Kênh đăng ký (Subscriptions)
            case 1156: return 410; // Thư viện / Bạn (Library / You)
            case 1160: return 181; // Nút Tạo (+)
            case 1158: return 777; // Shorts (variations)
            case 1162: return 44;  // Rất có thể đây là Settings (Bánh răng)
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