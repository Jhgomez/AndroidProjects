package okik.tech.tutorialcopy

import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.drawable.Drawable

/**
 * This class is intended to be used to configure "BackgroundEffectRenderLayout" instances that are
 * passed to "TutorialDisplayLayout" as dialogs
 */
data class BackgroundSettings(
    val renderEffect: RenderEffect?,
    val shouldClipToBackground: Boolean,
    val backgroundDrawable: Drawable,
    val backgroundOverlayPaint: Paint,
    val padding: InnerPadding
)
