package app.revanced.extension.youtube.patches.utils;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

@SuppressWarnings("unused")
public final class MissingResourcesPatch {
    private static final String TAG = "ReVancedIconLog";
    
    private static final String[] RESOURCE_PACKAGES = {
            "com.google.android.youtube",
            null
    };

    /**
     * BỘ MỔ XẺ REFLECTION: ÉP CLASS YOUTUBE NHẢ RA ICON_TYPE ID
     */
    private static void forceExtractIconType(Resources resources, int targetResId, String resName) {
        Log.e(TAG, "🕵️‍♂️ [BẮT ĐẦU MỔ XẺ] Tìm iconType ID cho Res: " + resName + " (" + String.format("0x%08X", targetResId) + ")");
        
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        // Quét ngược 10 tầng caller gần nhất trong StackTrace
        for (int i = 3; i < Math.min(stackTrace.length, 12); i++) {
            String className = stackTrace[i].getClassName();
            
            // Bỏ qua các class hệ thống
            if (className.startsWith("android.") || className.startsWith("java.") || className.startsWith("app.revanced")) {
                continue;
            }

            try {
                Class<?> clazz = Class.forName(className);
                Log.w(TAG, "🔍 [SOI CLASS] " + clazz.getName() + " | Method: " + stackTrace[i].getMethodName());

                // 1. Quét tất cả Field static/instance trong Class này
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object value = null;
                    
                    // Nếu là static field thì lấy trực tiếp, instance field thì bỏ qua tạm thời
                    if (Modifier.isStatic(field.getModifiers())) {
                        value = field.get(null);
                    }

                    if (value == null) continue;

                    // Trường hợp 1: Nó lưu dạng SparseArray (Rất phổ biến trong YouTube để map Enum -> ResId)
                    if (value instanceof SparseArray) {
                        SparseArray<?> sa = (SparseArray<?>) value;
                        for (int k = 0; k < sa.size(); k++) {
                            int key = sa.keyAt(k); // KEY CHÍNH LÀ ICON_TYPE ID!
                            Object val = sa.valueAt(k);
                            if (val instanceof Integer && (Integer) val == targetResId) {
                                Log.e(TAG, "🎯 [THÀNH CÔNG RỒI!] Tìm thấy SparseArray '" + field.getName() + "' trong " + clazz.getSimpleName() 
                                        + " -> Key (iconType ID) = " + key + " | ResId = " + targetResId);
                            }
                        }
                    } 
                    // Trường hợp 2: Nó lưu dạng Map (HashMap)
                    else if (value instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) value;
                        for (Map.Entry<?, ?> entry : map.entrySet()) {
                            if (entry.getValue() instanceof Integer && (Integer) entry.getValue() == targetResId) {
                                Log.e(TAG, "🎯 [THÀNH CÔNG RỒI!] Tìm thấy Map '" + field.getName() + "' trong " + clazz.getSimpleName() 
                                        + " -> Key (iconType ID) = " + entry.getKey() + " | ResId = " + targetResId);
                            }
                        }
                    }
                    // Trường hợp 3: Mảng int[] (int[] keys, int[] values)
                    else if (value instanceof int[]) {
                        int[] arr = (int[]) value;
                        for (int index = 0; index < arr.length; index++) {
                            if (arr[index] == targetResId) {
                                Log.e(TAG, "🎯 [PHÁT HIỆN] Tìm thấy ResId trong mảng int[] '" + field.getName() + "' tại Index: " + index + " của Class: " + clazz.getSimpleName());
                            }
                        }
                    }
                }

            } catch (Throwable ignored) {
                // Bỏ qua lỗi truy cập riêng tư nếu có
            }
        }
    }

    private static void inspectDrawableRequest(Resources resources, int id) {
        if (id == 0) return;

        try {
            String resName = resources.getResourceEntryName(id);
            
            // Lọc đúng các icon Toolbar nghi vấn để kích hoạt máy mổ xẻ
            if (resName.contains("search") || resName.contains("bell") || resName.contains("notification")) {
                Log.d(TAG, "🧲 [DRAWABLE_REQ] App đòi lấy: " + resName + " (" + String.format("0x%08X", id) + ")");
                
                // BẮT ĐẦU MỔ XẺ BỘ NHỚ ĐỂ ÉP LÒI ICON_TYPE ID
                forceExtractIconType(resources, id, resName);
            }
        } catch (Resources.NotFoundException ignored) {}
    }

    public static int getLegacyIconType(int iconType) {
        // Log lại toàn bộ iconType đi qua đây
        Log.d(TAG, "📥 [SERVER_ICON_TYPE_IN] -> " + iconType);
        
        switch (iconType) {
            case 1154: return 406;
            case 1157: return 776;
            case 1155: return 408;
            case 1156: return 410;
            case 1160: return 181;
            default:   return iconType;
        }
    }

    // =========================================================================
    // HOOK DRAWABLE METHODS
    // =========================================================================

    public static Drawable getTransparentDrawable(Context context) {
        return new ColorDrawable(Color.TRANSPARENT);
    }

    public static Drawable getDrawable(Context context, int id) {
        inspectDrawableRequest(context.getResources(), id);
        if (id == 0) return getTransparentDrawable(context);
        try { return context.getDrawable(id); } 
        catch (Resources.NotFoundException ex) { return getTransparentDrawable(context); }
    }

    public static Drawable getDrawable(Resources resources, int id) {
        inspectDrawableRequest(resources, id);
        if (id == 0) return new ColorDrawable(Color.TRANSPARENT);
        try { return resources.getDrawable(id); } 
        catch (Resources.NotFoundException ex) { return new ColorDrawable(Color.TRANSPARENT); }
    }

    public static Drawable getDrawable(Resources resources, int id, Resources.Theme theme) {
        inspectDrawableRequest(resources, id);
        if (id == 0) return new ColorDrawable(Color.TRANSPARENT);
        try { return resources.getDrawable(id, theme); } 
        catch (Resources.NotFoundException ex) { return new ColorDrawable(Color.TRANSPARENT); }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density) {
        inspectDrawableRequest(resources, id);
        if (id == 0) return new ColorDrawable(Color.TRANSPARENT);
        try { return resources.getDrawableForDensity(id, density); } 
        catch (Resources.NotFoundException ex) { return new ColorDrawable(Color.TRANSPARENT); }
    }

    public static Drawable getDrawableForDensity(Resources resources, int id, int density, Resources.Theme theme) {
        inspectDrawableRequest(resources, id);
        if (id == 0) return new ColorDrawable(Color.TRANSPARENT);
        try { return resources.getDrawableForDensity(id, density, theme); } 
        catch (Resources.NotFoundException ex) { return new ColorDrawable(Color.TRANSPARENT); }
    }
}