package okik.tech.tutorialcopy

import android.graphics.Paint
import android.graphics.RecordingCanvas
import android.graphics.RenderEffect
import android.graphics.drawable.Drawable
import android.view.View

/**
 * This class is intended to be used to configure "BackgroundEffectRenderLayout" instances
 *
 * @param renderCanvasPositionCommand lets control the location/position of the recording canvas
 * that copies/draws the views behind a "BackgroundEffectRenderLayout". This callback also passes a
 * reference pf the BackgroundEffectRenderLayout so you can get things like its location on screen
 * which is useful when modifying the position of the recording canvas
 */
data class BackgroundSettings(
    val renderEffect: RenderEffect?,
    val shouldClipToBackground: Boolean,
    val backgroundDrawable: Drawable,
    val backgroundOverlayPaint: Paint,
    val padding: InnerPadding,
    val renderCanvasPositionCommand: (RecordingCanvas, View) -> Unit
)
