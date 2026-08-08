package app.revanced.extension.youtube.patches.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("unused")
public final class MissingResourcesPatch {
    private static final String TAG = "YTIconDebug";
    
    private static final Set<Integer> LOGGED_ICON_TYPES = Collections.synchronizedSet(new HashSet<>());
    private static final Set<Integer> LOGGED_DRAWABLE_IDS = Collections.synchronizedSet(new HashSet<>());

    // Lớp chứa icon chờ được vẽ cùng mốc thời gian
    private static class PendingIcon {
        final int cairoId;
        final long timestamp;
        PendingIcon(int cairoId) {
            this.cairoId = cairoId;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    // Hàng đợi giữa Luồng Data (Ngầm) và Luồng UI (Giao diện)
    private static final Queue<PendingIcon> PENDING_ZERO_IDS = new ConcurrentLinkedQueue<>();

    private static final String[] TOOLBAR_FALLBACK_DRAWABLES = {
            "quantum_ic_more_vert_black_24",
            "ic_more_vert_black_24",
            "quantum_ic_more_vert_white_24"
    };
    
    private static final String[] RESOURCE_PACKAGES = {
            "com.google.android.youtube",
            null
    };

    // ---------------------------------------------------------
    // TRẠM 1: CỖ MÁY THỜI GIAN (DỊCH CAIRO -> LEGACY)
    // ---------------------------------------------------------
    public static int getLegacyIconType(int iconType) {
        if (LOGGED_ICON_TYPES.add(iconType)) {
            if (iconType > 1000) Log.w(TAG, "📡 [SERVER GỌI CAIRO] Số: " + iconType);
            else Log.d(TAG, "📡 [SERVER GỌI LEGACY] Số: " + iconType);
        }

        // Nhóm Topbar (Chuông, Kính Lúp, Cast) - App cũ bị mù bọn này, chắc chắn sẽ văng ra ID = 0
        if (iconType == 1239 || iconType == 1161 || iconType == 1021 || iconType == 1162) {
            Log.w(TAG, "📥 [HÀNG ĐỢI] Cất vào hàng đợi chờ vẽ: " + iconType);
            PENDING_ZERO_IDS.offer(new PendingIcon(iconType));
            return iconType; // Ép về nguyên bản để app chạy vào case ID = 0
        }

        // Nhóm Bottombar (Home, Shorts...) - App cũ vẫn nhận diện được
        switch (iconType) {
            case 1154: return 406; // Home
            case 1157: return 776; // Shorts
            case 1155: return 408; // Subs
            case 1156: return 410; // Library
            case 1160: return 181; // Create (+)
            default:
                return iconType;
        }
    }

    // ---------------------------------------------------------
    // TRẠM 2: VẼ VÀ BÙ ĐẮP CHO ID = 0
    // ---------------------------------------------------------
    private static void logAppRequestName(Resources resources, int id) {
        if (id != 0 && LOGGED_DRAWABLE_IDS.add(id)) {
            try {
                String name = resources.getResourceEntryName(id);
                if (name.startsWith("yt_") || name.startsWith("abc_") || name.startsWith("ic_") || name.startsWith("quantum_")) {
                    Log.i(TAG, "🔎 [APP VẼ FILE] Tên: " + name + " (ID: " + id + ")");
                }
            } catch (Exception ignored) {}
        }
    }

    // Hàm cứu hộ khi app gào thét vì ID = 0
    private static Drawable handleZeroId(Resources resources) {
        long now = System.currentTimeMillis();
        int validCairoId = 0;

        // Quét hàng đợi, vứt bỏ rác cũ (sau 5 giây) và lấy icon mới nhất
        while (!PENDING_ZERO_IDS.isEmpty()) {
            PendingIcon pending = PENDING_ZERO_IDS.poll();
            if (pending != null && (now - pending.timestamp) <= 5000) {
                validCairoId = pending.cairoId;
                break; // Lấy đúng thằng vừa bị Server gọi
            }
        }

        // Nếu lấy được, bơm thẳng tên file gốc vào để vẽ!
        if (validCairoId != 0) {
            Log.i(TAG, "📤 [HÀNG ĐỢI] Kéo ra vẽ bù cho ID=0. Cairo mã: " + validCairoId);
            String[] targetNames = null;
            
            if (validCairoId == 1239) {
                targetNames = new String[]{"yt_outline_search_black_24", "ic_search_black_24"};
            } else if (validCairoId == 1161) {
                targetNames = new String[]{"yt_outline_bell_black_24", "yt_outline_notification_black_24"};
            } else if (validCairoId == 1021) {
                targetNames = new String[]{"ic_outlined_media_route", "yt_outline_cast_black_24"};
            } else if (validCairoId == 1162) {
                targetNames = new String[]{"yt_outline_settings_black_24", "ic_settings_black_24"};
            }

            if (targetNames != null) {
                int realId = findDrawableId(resources, targetNames);
                if (realId != 0) {
                    try { return resources.getDrawable(realId); } catch (Exception ignored) {}
                }
            }
        }

        // Nếu hàng đợi rỗng (hoặc ID = 0 do lỗi khác), trả về fallback chuẩn
        return getFallbackDrawable(resources, isToolbarMenuStack());
    }

    public static Drawable getTransparentDrawable(Context context) {
        return getFallbackDrawable(context.getResources(), isToolbarMenuStack());
    }

    public static Drawable getDrawable(Context context, int id) {
        logAppRequestName(context.getResources(), id);
        if (id == 0) return handleZeroId(context.getResources());

        try {
            return context.getDrawable(id);
        } catch (Resources.NotFoundException ex) {
            return handleZeroId(context.getResources());
        }
    }

    public static Drawable getDrawable(Resources resources, int id) {
        logAppRequestName(resources, id);
        if (id == 0) return handleZeroId(resources);
        try { return resources.getDrawable(id); } 
        catch (Resources.NotFoundException ex) { return handleZeroId(resources); }
    }

    public static Drawable getDrawable(Resources resources, int id, Resources.Theme theme) {
        logAppRequestName(resources, id);
        if (id == 0) return handleZeroId(resources);
        try { return resources.getDrawable(id, theme); } 
        catch (Resources.NotFoundException ex) { return handleZeroId(resources); }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density) {
        logAppRequestName(resources, id);
        if (id == 0) return handleZeroId(resources);
        try { return resources.getDrawableForDensity(id, density); } 
        catch (Resources.NotFoundException ex) { return getFallbackDrawableForDensity(resources, density, isToolbarMenuStack()); }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density, Resources.Theme theme) {
        logAppRequestName(resources, id);
        if (id == 0) return handleZeroId(resources);
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
        return new ColorDrawable(Color.TRANSPARENT);
    }

    private static Drawable getFallbackDrawableForDensity(Resources resources, int density, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            int fallbackId = findDrawableId(resources, TOOLBAR_FALLBACK_DRAWABLES);
            if (fallbackId != 0) {
                try { return resources.getDrawableForDensity(fallbackId, density); } catch (Resources.NotFoundException ignored) {}
            }
        }
        return new ColorDrawable(Color.TRANSPARENT);
    }

    private static Drawable getFallbackDrawableForDensity(Resources resources, int density, Resources.Theme theme, boolean preferToolbarIcon) {
        if (preferToolbarIcon) {
            int fallbackId = findDrawableId(resources, TOOLBAR_FALLBACK_DRAWABLES);
            if (fallbackId != 0) {
                try { return resources.getDrawableForDensity(fallbackId, density, theme); } catch (Resources.NotFoundException ignored) {}
            }
        }
        return new ColorDrawable(Color.TRANSPARENT);
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
}