package com.udacity.clippingobjects

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.os.Build
import android.util.AttributeSet
import android.view.View

class ClippedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = resources.getDimension(R.dimen.strokeWidth)
        textSize = resources.getDimension(R.dimen.textSize)
    }

    private val path = Path()

    private val clipRectRight = resources.getDimension(R.dimen.clipRectRight)
    private val clipRectBottom = resources.getDimension(R.dimen.clipRectBottom)
    private val clipRectTop = resources.getDimension(R.dimen.clipRectTop)
    private val clipRectLeft = resources.getDimension(R.dimen.clipRectLeft)

    private val rectInset = resources.getDimension(R.dimen.rectInset)
    private val smallRectOffset = resources.getDimension(R.dimen.smallRectOffset)

    private val circleRadius = resources.getDimension(R.dimen.circleRadius)

    private val textOffset = resources.getDimension(R.dimen.textOffset)
    private val textSize = resources.getDimension(R.dimen.textSize)

    private val columnOne = rectInset
    private val columnTwo = columnOne + rectInset + clipRectRight

    private val rowOne = rectInset
    private val rowTwo = rowOne + rectInset + clipRectBottom
    private val rowThree = rowTwo + rectInset + clipRectBottom
    private val rowFour = rowThree + rectInset + clipRectBottom
    private val textRow = rowFour + (1.5f * clipRectBottom)
    private val rejectRow = rowFour + rectInset + 2 * clipRectBottom

    private var rectF =
        RectF(rectInset, rectInset, clipRectRight - rectInset, clipRectBottom - rectInset)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.GRAY)
        drawBackAndUnclippedRectangle(canvas)
        drawDifferenceClippingExample(canvas)
        drawCircularClippingExample(canvas)
        drawIntersectionClippingExample(canvas)
        drawCombinedClippingExample(canvas)
        drawRoundedRectangleClippingExample(canvas)
        drawOutsideClippingExample(canvas)
        drawSkewedTextExample(canvas)
        drawTranslatedTextExample(canvas)
        drawQuickRejectExample(canvas)
    }

    private fun drawClippedRectangle(canvas: Canvas) {
        // Draw the white square
        canvas.clipRect(clipRectLeft, clipRectTop, clipRectRight, clipRectBottom)
        canvas.drawColor(Color.WHITE)

        // Draw the diagonal line inside the sqaure
        paint.color = Color.RED
        canvas.drawLine(clipRectLeft, clipRectTop, clipRectRight, clipRectBottom, paint)

        // Draw the green circle inside the square
        paint.color = Color.GREEN
        canvas.drawCircle(circleRadius, clipRectBottom - circleRadius, circleRadius, paint)

        // Draw the blue text line inside the square
        paint.color = Color.BLUE
        paint.textSize = textSize
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(context.getString(R.string.clipping), clipRectRight, textOffset, paint)
    }

    private fun drawGeneric(canvas: Canvas, column: Float, row: Float, action: (Canvas) -> Unit) {
        canvas.save()
        canvas.translate(column, row)
        action.invoke(canvas)
        drawClippedRectangle(canvas)
        canvas.restore()
    }

    private fun drawBackAndUnclippedRectangle(canvas: Canvas) {
        drawGeneric(canvas, columnOne, rowOne) {
            // do nothing
        }
    }

    private fun drawDifferenceClippingExample(canvas: Canvas) {
        drawGeneric(canvas, columnTwo, rowOne) {
            // Use the subtraction of two clipping rectangles to create a frame.
            it.clipRect(
                2 * rectInset, 2 * rectInset,
                clipRectRight - 2 * rectInset,
                clipRectBottom - 2 * rectInset
            )
            // The method clipRect(float, float, float, float, Region.Op
            // .DIFFERENCE) was deprecated in API level 26. The recommended
            // alternative method is clipOutRect(float, float, float, float),
            // which is currently available in API level 26 and higher.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                it.clipRect(
                    4 * rectInset, 4 * rectInset,
                    clipRectRight - 4 * rectInset,
                    clipRectBottom - 4 * rectInset,
                    Region.Op.DIFFERENCE
                )
            else {
                it.clipOutRect(
                    4 * rectInset, 4 * rectInset,
                    clipRectRight - 4 * rectInset,
                    clipRectBottom - 4 * rectInset
                )
            }
        }
    }

    private fun drawCircularClippingExample(canvas: Canvas) {
        drawGeneric(canvas, columnOne, rowTwo) {
            // Clears any lines and curves from the path but unlike reset(),
            // keeps the internal data structure for faster reuse.
            path.rewind()
            path.addCircle(
                circleRadius, clipRectBottom - circleRadius,
                circleRadius, Path.Direction.CCW
            )
            // The method clipPath(path, Region.Op.DIFFERENCE) was deprecated in
            // API level 26. The recommended alternative method is
            // clipOutPath(Path), which is currently available in
            // API level 26 and higher.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                it.clipPath(path, Region.Op.DIFFERENCE)
            } else {
                it.clipOutPath(path)
            }
        }
    }

    private fun drawIntersectionClippingExample(canvas: Canvas) {
        drawGeneric(canvas, columnTwo, rowTwo) {
            it.clipRect(
                clipRectLeft, clipRectTop,
                clipRectRight - smallRectOffset,
                clipRectBottom - smallRectOffset
            )
            // The method clipRect(float, float, float, float, Region.Op
            // .INTERSECT) was deprecated in API level 26. The recommended
            // alternative method is clipRect(float, float, float, float), which
            // is currently available in API level 26 and higher.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                it.clipRect(
                    clipRectLeft + smallRectOffset,
                    clipRectTop + smallRectOffset,
                    clipRectRight, clipRectBottom,
                    Region.Op.INTERSECT
                )
            } else {
                it.clipRect(
                    clipRectLeft + smallRectOffset,
                    clipRectTop + smallRectOffset,
                    clipRectRight, clipRectBottom
                )
            }
        }
    }

    private fun drawCombinedClippingExample(canvas: Canvas) {
        drawGeneric(canvas, columnOne, rowThree) {
            path.rewind()
            path.addCircle(
                clipRectLeft + rectInset + circleRadius,
                clipRectTop + circleRadius + rectInset,
                circleRadius, Path.Direction.CCW
            )
            path.addRect(
                clipRectRight / 2 - circleRadius,
                clipRectTop + circleRadius + rectInset,
                clipRectRight / 2 + circleRadius,
                clipRectBottom - rectInset, Path.Direction.CCW
            )
            it.clipPath(path)
        }
    }

    private fun drawRoundedRectangleClippingExample(canvas: Canvas) {
        drawGeneric(canvas, columnTwo, rowThree) {
            path.rewind()
            path.addRoundRect(rectF, clipRectRight / 4, clipRectRight / 4, Path.Direction.CCW)
            it.clipPath(path)
        }
    }

    private fun drawOutsideClippingExample(canvas: Canvas) {
        drawGeneric(canvas, columnOne, rowFour) {
            it.clipRect(
                2 * rectInset, 2 * rectInset,
                clipRectRight - 2 * rectInset,
                clipRectBottom - 2 * rectInset
            )
        }
    }

    private fun drawTranslatedTextExample(canvas: Canvas) {
        canvas.save()
        paint.color = Color.GREEN
        // Align the RIGHT side of the text with the origin.
        paint.textAlign = Paint.Align.LEFT
        // Apply transformation to canvas.
        canvas.translate(columnTwo, textRow)
        // Draw text.
        canvas.drawText(context.getString(R.string.translated), clipRectLeft, clipRectTop, paint)
        canvas.restore()
    }

    private fun drawSkewedTextExample(canvas: Canvas) {
        canvas.save()
        paint.color = Color.YELLOW
        paint.textAlign = Paint.Align.RIGHT
        // Position text.
        canvas.translate(columnTwo, textRow)
        // Apply skew transformation.
        canvas.skew(0.2f, 0.3f)
        canvas.drawText(context.getString(R.string.skewed), clipRectLeft, clipRectTop, paint)
        canvas.restore()
    }

    private fun drawQuickRejectExample(canvas: Canvas) {
        val inClipRectangle = RectF(
            clipRectRight / 2,
            clipRectBottom / 2,
            clipRectRight * 2,
            clipRectBottom * 2
        )

        val notInClipRectangle = RectF(
            RectF(
                clipRectRight + 1,
                clipRectBottom + 1,
                clipRectRight * 2,
                clipRectBottom * 2
            )
        )

        canvas.save()
        canvas.translate(columnOne, rejectRow)
        canvas.clipRect(
            clipRectLeft, clipRectTop,
            clipRectRight, clipRectBottom
        )
        if (canvas.quickReject(inClipRectangle, Canvas.EdgeType.AA)) {
            canvas.drawColor(Color.WHITE)
        } else {
            canvas.drawColor(Color.BLACK)
            canvas.drawRect(inClipRectangle, paint)
        }
        canvas.restore()
    }
}