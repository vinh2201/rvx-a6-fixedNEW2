package app.revanced.patches.youtube.utils.missingresources

import app.revanced.util.fingerprint.legacyFingerprint
import app.revanced.util.or
import com.android.tools.smali.dexlib2.AccessFlags

internal val navigationBarGetDrawableFingerprint = legacyFingerprint(
    name = "navigationBarGetDrawableFingerprint",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    returnType = "Landroid/graphics/drawable/Drawable;",
    parameters = listOf("Landroid/content/Context;", "I"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "a"
    }
)

internal val legacyIconEnumConverterFingerprint = legacyFingerprint(
    name = "legacyIconEnumConverterFingerprint",
    parameters = listOf("I"),
    customFingerprint = { method, classDef ->
        // Sửa lại cách check public class qua accessFlags
        method.name == "b" && 
        method.parameterTypes == listOf("I") && 
        AccessFlags.PUBLIC.isSet(classDef.accessFlags)
    }
)