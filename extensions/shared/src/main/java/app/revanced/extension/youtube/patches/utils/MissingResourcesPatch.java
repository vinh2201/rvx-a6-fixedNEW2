package app.revanced.extension.youtube.patches.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("unused")
public final class MissingResourcesPatch {
    private static final String TAG = "YTIconDebug";
    
    private static final Queue<Integer> PENDING_CAIRO_IDS = new ConcurrentLinkedQueue<>();
    // Mốc thời gian app bắt đầu chạy để chống sốc lúc khởi động (3 giây đầu)
    private static final long APP_START_TIME = System.currentTimeMillis();
    private static final long WARMUP_DELAY_MS = 3000; 

    public static int getLegacyIconType(int iconType) {
        // Chỉ bắt đầu ghi nhận sau khi app đã qua giai đoạn khởi động nặng nhọc
        if (System.currentTimeMillis() - APP_START_TIME > WARMUP_DELAY_MS) {
            if (iconType == 1239 || iconType == 1161 || iconType == 1021 || iconType == 1162) {
                PENDING_CAIRO_IDS.offer(iconType);
            }
        }

        switch (iconType) {
            case 1154: return 406;
            case 1157: return 776;
            case 1155: return 408;
            case 1156: return 410;
            case 1160: return 181;
            default: return iconType;
        }
    }

    private static Drawable getSafeDrawable(Resources res, int cairoId) {
        String[] targetNames = null;
        if (cairoId == 1239) targetNames = new String[]{"yt_outline_search_black_24"};
        else if (cairoId == 1161) targetNames = new String[]{"yt_outline_bell_black_24", "yt_outline_notification_black_24"};
        else if (cairoId == 1021) targetNames = new String[]{"ic_outlined_media_route"};
        else if (cairoId == 1162) targetNames = new String[]{"yt_outline_settings_black_24"};

        if (targetNames != null) {
            for (String name : targetNames) {
                int id = res.getIdentifier(name, "drawable", "com.google.android.youtube");
                if (id != 0) {
                    try { return res.getDrawable(id); } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }

    public static Drawable getDrawable(Resources res, int id) {
        // Tuyệt đối không can thiệp trong 3 giây đầu tiên mở app để tránh crash
        if (id == 0 && (System.currentTimeMillis() - APP_START_TIME > WARMUP_DELAY_MS)) {
            Integer cairoId = PENDING_CAIRO_IDS.poll();
            if (cairoId != null) {
                Drawable d = getSafeDrawable(res, cairoId);
                if (d != null) return d;
            }
        }
        try { return res.getDrawable(id); } catch (Exception e) { return null; }
    }

    public static Drawable getDrawable(Resources res, int id, Resources.Theme theme) {
        if (id == 0 && (System.currentTimeMillis() - APP_START_TIME > WARMUP_DELAY_MS)) {
            Integer cairoId = PENDING_CAIRO_IDS.poll();
            if (cairoId != null) {
                Drawable d = getSafeDrawable(res, cairoId);
                if (d != null) return d;
            }
        }
        try { return res.getDrawable(id, theme); } catch (Exception e) { return null; }
    }
}