package com.trixxwids.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.trixxwids.app.data.ElementType
import com.trixxwids.app.data.WidgetElement

class CanvasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val elements = mutableListOf<WidgetElement>()
    private var selectedElement: WidgetElement? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var lastTouchX = 0f
    private var lastTouchY = 0f

    var gyroOffsetX = 0f
    var gyroOffsetY = 0f
        set(value) {
            field = value
            invalidate()
        }

    fun addElement(element: WidgetElement) {
        elements.add(element)
        invalidate()
    }

    fun getElements(): List<WidgetElement> = elements

    // Converts the live canvas into a static image for the home screen widget
    fun generateBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Temporarily clear gyro offsets so the saved image is perfectly centered
        val tempX = gyroOffsetX
        val tempY = gyroOffsetY
        gyroOffsetX = 0f
        gyroOffsetY = 0f
        
        draw(canvas)
        
        gyroOffsetX = tempX
        gyroOffsetY = tempY
        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Transparent background so the widget blends into the user's wallpaper
        canvas.drawColor(Color.TRANSPARENT)

        // Draw elements back-to-front based on Z-Index
        val sortedElements = elements.sortedBy { it.zIndex }

        for (element in sortedElements) {
            // Apply a multiplying factor to Z-Index to create depth parallax effect
            val elementX = element.x + (gyroOffsetX * (element.zIndex + 1))
            val elementY = element.y + (gyroOffsetY * (element.zIndex + 1))

            paint.color = try { Color.parseColor(element.color) } catch (e: Exception) { Color.BLACK }
            paint.alpha = (element.opacity * 255).toInt()

            when (element.type) {
                ElementType.SHAPE_RECTANGLE -> {
                    val rect = RectF(elementX, elementY, elementX + element.width, elementY + element.height)
                    canvas.drawRoundRect(rect, element.cornerRadius, element.cornerRadius, paint)
                }
                ElementType.SHAPE_CIRCLE -> {
                    val radius = (element.width.coerceAtLeast(element.height)) / 2f
                    canvas.drawCircle(elementX + radius, elementY + radius, radius, paint)
                }
                ElementType.TEXT, ElementType.CLOCK, ElementType.DATE -> {
                    textPaint.color = paint.color
                    textPaint.alpha = paint.alpha
                    textPaint.textSize = element.fontSize
                    canvas.drawText(element.content, elementX, elementY + element.fontSize, textPaint)
                }
                ElementType.IMAGE -> {
                    // Placeholder for actual image loading logic
                    val rect = RectF(elementX, elementY, elementX + element.width, elementY + element.height)
                    canvas.drawRect(rect, paint)
                    textPaint.color = Color.WHITE
                    textPaint.textSize = 24f
                    canvas.drawText("IMG", elementX + 10f, elementY + 30f, textPaint)
                }
            }

            // Highlight the currently selected element with a red border
            if (element == selectedElement) {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.RED
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                }
                val rect = RectF(elementX - 5, elementY - 5, elementX + element.width + 5, elementY + element.height + 5)
                canvas.drawRect(rect, strokePaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Find if we touched an element (check top-most elements first)
                selectedElement = elements.reversed().find {
                    touchX >= it.x && touchX <= it.x + it.width &&
                    touchY >= it.y && touchY <= it.y + it.height + (it.fontSize.toInt()) // Account for text height
                }
                lastTouchX = touchX
                lastTouchY = touchY
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                selectedElement?.let {
                    val dx = touchX - lastTouchX
                    val dy = touchY - lastTouchY
                    it.x += dx
                    it.y += dy
                    lastTouchX = touchX
                    lastTouchY = touchY
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                // Drag complete
            }
        }
        return super.onTouchEvent(event)
    }
}
