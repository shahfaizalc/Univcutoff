package com.faikan.univcutoff

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min


class ZoomableImageView : AppCompatImageView {
    private val matrix: Matrix = Matrix()
    private val savedMatrix: Matrix = Matrix()

    private var mode = NONE

    // For dragging
    private val start = PointF()

    // Zoom control
    private var scaleFactor = 1.0f
    private val minScale = 0.5f
    private val maxScale = 5.0f

    // Image bounds
    private val imageBounds = RectF()
    private val viewBounds = RectF()

    // Constructors
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        scaleType = ScaleType.MATRIX
        setOnTouchListener(TouchListener())
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        // Fit the new image to screen
        post { fitImageToScreen() }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        // Fit the new image to screen
        post { fitImageToScreen() }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            // Update view bounds
            viewBounds[0f, 0f, w.toFloat()] = h.toFloat()
            // Fit image to screen when the view size changes
            fitImageToScreen()
        }
    }

    /**
     * Fit the image to the screen while maintaining aspect ratio
     */
    fun fitImageToScreen() {
        val drawable = drawable
        if (drawable == null || !isShown || width == 0 || height == 0) {
            return  // View not ready yet
        }


        // Reset the matrix
        matrix.reset()

        val dWidth = drawable.intrinsicWidth
        val dHeight = drawable.intrinsicHeight
        val vWidth = width
        val vHeight = height

        if (dWidth <= 0 || dHeight <= 0) {
            return  // Invalid drawable size
        }


        // Calculate the scale needed to fit the image to the view
        val scaleX = vWidth.toFloat() / dWidth
        val scaleY = vHeight.toFloat() / dHeight
        val scale = min(
            scaleX.toDouble(),
            scaleY.toDouble()
        ).toFloat() // Use the smaller scale to fit the whole image


        // Calculate the center point of the view
        val centerX = vWidth / 2f
        val centerY = vHeight / 2f


        // Scale the image
        matrix.postScale(scale, scale, 0F, 0F)


        // Center the image
        val scaledWidth = dWidth * scale
        val scaledHeight = dHeight * scale
        val dx = centerX - scaledWidth / 2
        val dy = centerY - scaledHeight / 2

        matrix.postTranslate(dx, dy)


        // Update the scale factor
        scaleFactor = scale


        // Update image bounds
        updateImageBounds()


        // Apply the matrix to the ImageView
        imageMatrix = matrix
    }

    /**
     * Update the current bounds of the image after transformation
     */
    private fun updateImageBounds() {
        val drawable = drawable
        if (drawable != null) {
            imageBounds[0f, 0f, drawable.intrinsicWidth.toFloat()] =
                drawable.intrinsicHeight.toFloat()
            matrix.mapRect(imageBounds)
        }
    }

    /**
     * Ensure the image stays within the view bounds when zoomed out
     * and doesn't leave too much empty space when zoomed in
     */
    private fun checkImageBounds() {
        updateImageBounds()

        var deltaX = 0f
        var deltaY = 0f


        // If the image is smaller than the screen, center it
        if (imageBounds.width() <= viewBounds.width()) {
            deltaX = (viewBounds.width() - imageBounds.width()) / 2 - imageBounds.left
        } else if (imageBounds.left > 0) {
            deltaX = -imageBounds.left
        } else if (imageBounds.right < viewBounds.width()) {
            deltaX = viewBounds.width() - imageBounds.right
        }


        // Same for height
        if (imageBounds.height() <= viewBounds.height()) {
            deltaY = (viewBounds.height() - imageBounds.height()) / 2 - imageBounds.top
        } else if (imageBounds.top > 0) {
            deltaY = -imageBounds.top
        } else if (imageBounds.bottom < viewBounds.height()) {
            deltaY = viewBounds.height() - imageBounds.bottom
        }


        // Apply the translation
        if (deltaX != 0f || deltaY != 0f) {
            matrix.postTranslate(deltaX, deltaY)
            imageMatrix = matrix
        }
    }

    /**
     * Methods for programmatic zooming (for zoom buttons)
     */
    /**
     * Zoom in by 20%
     */
    fun zoomIn() {
        val newScale = scaleFactor * 1.2f
        if (newScale <= maxScale) {
            // Get the center of the view
            val centerX = width / 2f
            val centerY = height / 2f


            // Apply scale
            matrix.postScale(1.2f, 1.2f, centerX, centerY)
            scaleFactor = newScale
            imageMatrix = matrix
            checkImageBounds()
        }
    }

    /**
     * Zoom out by 20%
     */
    fun zoomOut() {
        val newScale = scaleFactor / 1.2f
        if (newScale >= minScale) {
            // Get the center of the view
            val centerX = width / 2f
            val centerY = height / 2f


            // Apply scale
            matrix.postScale(1 / 1.2f, 1 / 1.2f, centerX, centerY)
            scaleFactor = newScale
            imageMatrix = matrix
            checkImageBounds()
        }
    }

    /**
     * Reset zoom to fit the image to screen
     */
    fun resetZoom() {
        fitImageToScreen()
    }

    /**
     * Touch listener for handling drag events
     */
    private inner class TouchListener : OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    savedMatrix.set(matrix)
                    start[event.x] = event.y
                    mode = DRAG
                }

                MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                    // Only allow dragging if zoomed in
                    if (scaleFactor > min(
                            (width.toFloat() / drawable.intrinsicWidth).toDouble(),
                            (height.toFloat() / drawable.intrinsicHeight).toDouble()
                        )
                    ) {
                        // Dragging
                        matrix.set(savedMatrix)
                        matrix.postTranslate(event.x - start.x, event.y - start.y)
                        imageMatrix = matrix
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                    checkImageBounds()
                }
            }

            return true
        }
    }

    companion object {
        // States
        private const val NONE = 0
        private const val DRAG = 1
    }
}
