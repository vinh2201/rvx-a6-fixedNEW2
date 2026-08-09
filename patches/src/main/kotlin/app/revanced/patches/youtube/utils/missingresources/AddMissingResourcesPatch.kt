package app.revanced.patches.youtube.utils.missingresources

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.general.toolbar.attributeResolverFingerprint
import app.revanced.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.patches.youtube.utils.extension.Constants.UTILS_PATH
import app.revanced.patches.youtube.utils.patch.PatchList.ADD_MISSING_RESOURCES
import app.revanced.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.patches.youtube.utils.settings.settingsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.fingerprint.methodOrThrow

private const val EXTENSION_CLASS_DESCRIPTOR = "$UTILS_PATH/MissingResourcesPatch;"

private val addMissingResourcesBytecodePatch = bytecodePatch {
    execute {
        // 1. Hook Navigation Bar GetDrawable kèm điều kiện if-nez chống nuốt luồng
        navigationBarGetDrawableFingerprint.methodOrThrow().apply {
            addInstructionsWithLabels(
                0,
                """
                if-nez p1, :original
                invoke-static {p0, p1}, $EXTENSION_CLASS_DESCRIPTOR->getDrawable(Landroid/content/Context;I)Landroid/graphics/drawable/Drawable;
                move-result-object p0
                return-object p0
                """,
                ExternalLabel("original", getInstruction(0))
            )
        }

        // 2. Hook Attribute Resolver của Kitadai31
        attributeResolverFingerprint.methodOrThrow().apply {
            addInstructionsWithLabels(
                0,
                """
                if-nez p1, :original
                const/4 v0, 0x0
                return-object v0
                """,
                ExternalLabel("original", getInstruction(0))
            )
        }

        // 3. Hook Enum Icon Converter
        legacyIconEnumConverterFingerprint.methodOrThrow().apply {
            addInstructions(
                0,
                """
                invoke-static {p0}, $EXTENSION_CLASS_DESCRIPTOR->getLegacyIconType(I)I
                move-result p0
                """
            )
        }
    }
}

@Suppress("unused")
val addMissingResourcesPatch = resourcePatch(
    ADD_MISSING_RESOURCES.title,
    ADD_MISSING_RESOURCES.summary,
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(
        settingsPatch,
        addMissingResourcesBytecodePatch,
    )

    execute {
        // Lấy danh sách drawable hiện có để tránh tạo trùng hoặc gọi target không tồn tại
        val existingDrawableNames = mutableSetOf<String>()
        document("res/values/drawables.xml").use { doc ->
            val nodes = doc.getElementsByTagName("drawable")
            for (i in 0 until nodes.length) {
                nodes.item(i).attributes?.getNamedItem("name")?.nodeValue?.let {
                    existingDrawableNames.add(it)
                }
            }
        }

        // Map đã làm sạch - Loại bỏ toàn bộ Key trùng lặp
        val aliases = mapOf(
            // Cairo Navigation Icons
            "yt_fill_home_cairo_black_24" to "@drawable/yt_fill_home_black_24",
            "yt_outline_home_cairo_black_24" to "@drawable/yt_outline_home_black_24",
            "yt_fill_subscriptions_cairo_black_24" to "@drawable/yt_fill_subscriptions_black_24",
            "yt_outline_subscriptions_cairo_black_24" to "@drawable/yt_outline_subscriptions_black_24",
            "yt_fill_youtube_shorts_cairo_black_24" to "@drawable/yt_fill_youtube_shorts_black_24",
            "yt_outline_youtube_shorts_cairo_black_24" to "@drawable/yt_outline_youtube_shorts_black_24",
            "yt_fill_library_cairo_black_24" to "@drawable/yt_fill_library_black_24",
            "yt_outline_library_cairo_black_24" to "@drawable/yt_outline_library_black_24",
            "yt_fill_bell_cairo_black_24" to "@drawable/yt_fill_bell_black_24",
            "yt_outline_bell_cairo_black_24" to "@drawable/yt_outline_bell_black_24",

            // Shorts Top Bar Icons
            "yt_fill_search_cairo_black_24" to "@drawable/yt_fill_search_black_24",
            "yt_outline_search_cairo_black_24" to "@drawable/yt_outline_search_black_24",
            "yt_outline_overflow_vertical_cairo_black_24" to "@drawable/yt_outline_overflow_vertical_black_24",
            "yt_fill_chromecast_cairo_black_24" to "@drawable/yt_fill_chromecast_black_24",
            "yt_outline_chromecast_cairo_black_24" to "@drawable/yt_outline_chromecast_black_24",
            "yt_fill_compass_cairo_black_24" to "@drawable/yt_fill_compass_black_24",
            "yt_outline_compass_cairo_black_24" to "@drawable/yt_outline_compass_black_24",
            "yt_outline_gear_cairo_black_24" to "@drawable/yt_outline_gear_black_24",
            "yt_outline_share_cairo_black_24" to "@drawable/yt_outline_share_black_24",
            "yt_outline_arrow_left_cairo_black_24" to "@drawable/yt_outline_arrow_left_black_24",

            // Shorts Action Buttons
            "youtube_shorts_like_outline_32dp" to "@drawable/ic_right_like_off_32c",
            "youtube_shorts_like_fill_32dp" to "@drawable/ic_right_like_on_32c",
            "youtube_shorts_thumbs_up_outline_28dp" to "@drawable/ic_right_like_off_32c",
            "youtube_shorts_thumbs_up_fill_28dp" to "@drawable/ic_right_like_on_32c",
            "youtube_shorts_dislike_outline_32dp" to "@drawable/ic_right_dislike_off_32c",
            "youtube_shorts_dislike_fill_32dp" to "@drawable/ic_right_dislike_on_32c",
            "youtube_shorts_thumbs_down_outline_28dp" to "@drawable/ic_right_dislike_off_32c",
            "youtube_shorts_thumbs_down_fill_28dp" to "@drawable/ic_right_dislike_on_32c",
            "youtube_shorts_comment_outline_28dp" to "@drawable/ic_right_comment_32c",
            "youtube_shorts_comment_outline_32dp" to "@drawable/ic_right_comment_32c",
            "youtube_shorts_share_outline_28dp" to "@drawable/ic_right_share_32c",
            "youtube_shorts_share_outline_32dp" to "@drawable/ic_right_share_32c",
            "youtube_shorts_remix_outline_28dp" to "@drawable/ic_remix_filled_white_24",
            "youtube_shorts_remix_outline_32dp" to "@drawable/ic_remix_filled_white_24",
            "youtube_shorts_save_outline_28dp" to "@drawable/yt_outline_bookmark_black_24",
            "youtube_shorts_save_outline_32dp" to "@drawable/yt_outline_bookmark_black_24",
            "youtube_shorts_save_fill_28dp" to "@drawable/yt_fill_bookmark_black_24",
            "youtube_shorts_save_fill_32dp" to "@drawable/yt_fill_bookmark_black_24",
            "youtube_shorts_save_fill_selected_32dp" to "@drawable/yt_fill_bookmark_black_24",
            "youtube_shorts_save_fill_unselected_32dp" to "@drawable/yt_outline_bookmark_black_24",
            "youtube_shorts_original_sound_16dp" to "@drawable/quantum_ic_music_note_white_24",
            "youtube_shorts_pivot_fab" to "@drawable/ic_youtube_shorts_24",

            // Comments
            "yt_outline_thumb_up_cairo_black_18" to "@drawable/yt_outline_thumb_up_black_24",
            "yt_outline_thumb_down_cairo_black_18" to "@drawable/yt_outline_thumb_down_black_24",
            "yt_fill_thumb_up_cairo_black_18" to "@drawable/yt_fill_thumb_up_black_24",
            "yt_fill_thumb_down_cairo_black_18" to "@drawable/yt_fill_thumb_down_black_24",
            "yt_fill_spark_cairo_black_24" to "@drawable/yt_fill_sparkle_white_24"
        )

        document("res/values/drawables.xml").use { document ->
            val rootNode = document.documentElement
            aliases.forEach { (key, value) ->
                if (key !in existingDrawableNames) {
                    val newElement = document.createElement("drawable")
                    newElement.setAttribute("name", key)
                    newElement.textContent = value
                    rootNode.appendChild(newElement)
                    existingDrawableNames.add(key)
                }
            }
        }

        copyResources(
            "addmissingresources",
            ResourceGroup("drawable", "ic_waveform_elements.xml")
        )

        addPreference(ADD_MISSING_RESOURCES)
    }
}