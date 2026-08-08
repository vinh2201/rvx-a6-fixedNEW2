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

    private static class PendingIcon {
        final int cairoId;
        final long timestamp;
        PendingIcon(int cairoId) {
            this.cairoId = cairoId;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
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
    // TRẠM 1: CẮT ĐỨT DÂY DƯA VỚI LEGACY MAP
    // ---------------------------------------------------------
    public static int getLegacyIconType(int iconType) {
        if (LOGGED_ICON_TYPES.add(iconType)) {
            Log.w(TAG, "📡 [SERVER GỌI CAIRO] Số: " + iconType);
        }

        // Bỏ qua tất cả các logic gán Legacy sai lầm trước đây
        // Trả về 0 để ép app rơi vào hàm xử lý ID 0 của chúng ta
        if (iconType == 1239 || iconType == 1161 || iconType == 1021 || iconType == 1162) {
            PENDING_ZERO_IDS.offer(new PendingIcon(iconType));
            return 0; 
        }

        // Với Home/Shorts/Subs/Library thì app cũ vẫn hiểu tốt, giữ nguyên
        switch (iconType) {
            case 1154: return 406;
            case 1157: return 776;
            case 1155: return 408;
            case 1156: return 410;
            case 1160: return 181;
            default:
                return iconType;
        }
    }

    // ---------------------------------------------------------
    // TRẠM 2: VẼ DỰA TRÊN DỮ LIỆU TỪ HÀNG ĐỢI
    // ---------------------------------------------------------
    private static Drawable handleZeroId(Resources resources) {
        PendingIcon pending = PENDING_ZERO_IDS.poll();
        if (pending != null) {
            Log.i(TAG, "📤 [VẼ BÙ] Đang vẽ cho Cairo ID: " + pending.cairoId);
            String[] targetNames = null;
            
            if (pending.cairoId == 1239) {
                targetNames = new String[]{"yt_outline_search_black_24"};
            } else if (pending.cairoId == 1161) {
                targetNames = new String[]{"yt_outline_bell_black_24", "yt_outline_notification_black_24"};
            } else if (pending.cairoId == 1021) {
                targetNames = new String[]{"ic_outlined_media_route"};
            } else if (pending.cairoId == 1162) {
                targetNames = new String[]{"yt_outline_settings_black_24"};
            }

            if (targetNames != null) {
                int realId = findDrawableId(resources, targetNames);
                if (realId != 0) {
                    try { return resources.getDrawable(realId); } catch (Exception ignored) {}
                }
            }
        }
        return new ColorDrawable(Color.TRANSPARENT);
    }

    // Các hàm getDrawable giữ nguyên, chỉ cần đảm bảo nó gọi handleZeroId khi ID = 0
    public static Drawable getDrawable(Resources resources, int id) {
        if (id == 0) return handleZeroId(resources);
        try { return resources.getDrawable(id); } 
        catch (Exception ex) { return handleZeroId(resources); }
    }

    public static Drawable getDrawable(Resources resources, int id, Resources.Theme theme) {
        if (id == 0) return handleZeroId(resources);
        try { return resources.getDrawable(id, theme); } 
        catch (Exception ex) { return handleZeroId(resources); }
    }
    
    // ... (Giữ nguyên các hàm getDrawableForDensity tương tự)

    private static int findDrawableId(Resources resources, String[] names) {
        for (String name : names) {
            for (String resourcePackage : RESOURCE_PACKAGES) {
                int id = resources.getIdentifier(name, "drawable", resourcePackage);
                if (id != 0) return id;
            }
        }
        return 0;
    }
}