package okik.tech.tutorialcopy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RenderNode
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils

/**
 * This component is just a frame layout wrapped in a rounded corner shape, the difference
 * with a material card is that this one doesn't have "card elevation" and also you can apply
 * effects to its background without affecting the foreground(its content). The background, which
 * enables to render effects, exclusively, without affecting the content of the container, is actually
 * a view that lives behind the rest of the content, and is added automatically when view is
 * instantiated, it is referred to as "effectHolderBackground"
 */
class DialogWrapperLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    private var paint: Paint = Paint()
    private val blurNode = RenderNode("BlurView node")
    private var backgroundViewRenderNode: RenderNode? = null
    private var fallBackDrawable: Drawable? = null
    private var blurBackgroundSettings: BlurBackgroundSettings? = null

    init {
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun drawChild(canvas: Canvas, child: View?, drawingTime: Long): Boolean {
        // child at 0 should always be the only child added, by the user, to this custom view
        if (getChildAt(0).id == child?.id) {
            focusArea?.also { fa ->
                // this custom layout should only have one child and we record its content with no effect here
                // so we can make a copy of the area we want to focus on
                contentCopy.setPosition(0, 0, width, height)

                val contentCopyRecordingCanvas = contentCopy.beginRecording()

                val isInvalidatedIssued =
                    super.drawChild(contentCopyRecordingCanvas, child, drawingTime)
                contentCopy.endRecording()

                // create a version of the original view that lives in "contentCopy" and apply the
                // passed effect
                contentWithEffect.setRenderEffect(fa.outerAreaEffect)

                contentWithEffect.setPosition(0, 0, width, height)
                val contentWithEffectRecordingCanvas = contentWithEffect.beginRecording()
                contentWithEffectRecordingCanvas.drawRenderNode(contentCopy)

                val overlayColor = ColorUtils.setAlphaComponent(fa.overlayColor, fa.overlayAlpha.toInt())
                contentWithEffectRecordingCanvas.drawColor(overlayColor)

                contentWithEffect.endRecording()

                // draw the applied effect to content to canvas of custom layout
                canvas.drawRenderNode(contentWithEffect)

                // make a copy of original content but only of the specified view and the requested
                // surrounding area
                if (fa.viewLocation.size == 2) {
                    var focusWidth = fa.view.width
                    var focusHeight = fa.view.height

                    var translationX = fa.viewLocation[0].toFloat()
                    var translationY = fa.viewLocation[1].toFloat()

                    var canvasTranslationX = -fa.viewLocation[0].toFloat()
                    var canvasTranslationY = -fa.viewLocation[1].toFloat()

                    // here is where we add the surrounding area thickness as a square which is a
                    // exact copy of the actual surrounding but, again, note that its limitation is
                    // that the surrounding area shape can only be a square
//                    if (fa.roundedCornerSurrounding == null) {
                    // if user wants to apply an effect to this surrounding area we need to make
                    // a copy of it and then another copy just or the view user wants to add focus on
                    // but if no effect added to this focus area there is no need

//                    }

                    focusedContent.setPosition(0, 0, focusWidth, focusHeight)

                    focusedContent.translationX = translationX
                    focusedContent.translationY = translationY

                    val focusAreaRecordingCanvas = focusedContent.beginRecording()

                    focusAreaRecordingCanvas.translate(
                        canvasTranslationX,
                        canvasTranslationY
                    )

                    focusAreaRecordingCanvas.drawRenderNode(contentCopy)

                    focusedContent.endRecording()

                    // note the focus area was not draw here yet, that is because it will be draw above its
                    // specified surrounding area which at the same time has to be rendered above
                    // the background overlay

                    return isInvalidatedIssued
                }

                return isInvalidatedIssued
            }
        }


        // this will be true only if user passed a rounded surrounding object, so we need to
        // render on canvas rounded background and then the view to focus
        if (child is FocusLayout) {
            if (focusArea != null) {
                if (focusArea!!.surroundingThickness.top > 0
                    || focusArea!!.surroundingThickness.bottom > 0
                    || focusArea!!.surroundingThickness.start > 0
                    || focusArea!!.surroundingThickness.end > 0
                ) {
                    val isInvalidateIssued = super.drawChild(canvas, child, drawingTime)

                    canvas.drawRenderNode(focusedContent)

                    return isInvalidateIssued
                } else {
                    canvas.drawRenderNode(focusedContent)
                    return false
                }
            }
        }

        // if no focus area has been specified just render the node
        val invalidated = super.drawChild(canvas, child, drawingTime)
        return invalidated
    }
}