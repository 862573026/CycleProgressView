package com.newx.lib.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import kotlin.math.cos
import kotlin.math.sin


/**
 * 进度View
 */
class CycleProgressView : View {
    private val TAG = "CycleProgressView"
    var progress = 0
    private var text = "0%"
    private var maxSize = 100
    private var textSize = 26f // 文字大小
    private var fullTextSize = 22f // 满进度文字大小
    private var maxTipText = "" // 满进度提示
    private var cycleSize = 20f // 圆弧笔触大小
    @ColorInt
    private var textColor = -0x99999a // 文字颜色
    @ColorInt
    private var fullTextColor = -0xb0b1 // 满的提示字体颜色
    @ColorInt
    private var bgColor = -0x404041 // 背景颜色
    @ColorInt
    private var lowColor = -0xa74811 // 少进度的颜色
    @ColorInt
    private var highColor = -0x54e6 // 高进度的颜色
    @ColorInt
    private var fullColor = -0x54e6 // 满的时候的颜色
    private val paint = Paint()
    private val circleRect = RectF() // 圆形的矩形
    private val startRect = RectF()
    private val endRect = Rect()
    private val endTopRect = Rect()
    private val endBottomRect = Rect()
    private var padding = 0 // 边距

    constructor(context: Context?) : super(context) {}
    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        initAttributeSet(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initAttributeSet(context, attrs)
    }

    private fun initAttributeSet(
        context: Context,
        attrs: AttributeSet?
    ) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.CycleProgressView)
        textSize = typedArray.getDimension(R.styleable.CycleProgressView_cpvTextSize, 26f)
        fullTextSize = typedArray.getDimension(R.styleable.CycleProgressView_cpvFullTextSize, 22f)
        maxSize = typedArray.getInteger(R.styleable.CycleProgressView_cpvMaxSize, 100)
        maxTipText = typedArray.getString(R.styleable.CycleProgressView_cpvMaxTipText) ?: ""
        cycleSize = typedArray.getDimension(R.styleable.CycleProgressView_cpvCycleSize, 20f)
        textColor = typedArray.getColor(R.styleable.CycleProgressView_cpvTextColor, -0x99999a)
        fullTextColor = typedArray.getColor(R.styleable.CycleProgressView_cpvFullTextColor, -0xb0b1)
        bgColor = typedArray.getColor(R.styleable.CycleProgressView_cpvBgColor, -0x404041)
        lowColor = typedArray.getColor(R.styleable.CycleProgressView_cpvLowColor, -0xa74811)
        highColor = typedArray.getColor(R.styleable.CycleProgressView_cpvHighColor, -0x54e6)
        fullColor = typedArray.getColor(R.styleable.CycleProgressView_cpvFullColor, -0xb0b1)
        typedArray.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val minRadius = cycleSize / 2f
        padding = paddingLeft.coerceAtLeast(
            paddingTop.coerceAtLeast(
                paddingRight.coerceAtLeast(paddingBottom)
            )
        ) // 取大的，这个根据设计
        circleRect[minRadius + padding, minRadius + padding, w - minRadius - padding] =
            w - minRadius - padding
        startRect[w / 2f - minRadius, 0f + padding, w / 2f + minRadius] = cycleSize + padding
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredWidth) // 设置为正方形
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 背景圈
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        val stroke = cycleSize //笔触，是最小圆半径的两倍
        val minRadius = stroke / 2f
        val width = measuredWidth.toFloat()
        val radius = (width - stroke) / 2f //最大圆的半径
        paint.strokeWidth = stroke
        paint.color = bgColor //背景圆颜色
        canvas.drawCircle(width / 2L, width / 2L, radius - padding, paint) // 结合padding
        // 占用圈
        when {
            progress <= 50 -> {
                paint.color = lowColor // 低进度颜色
            }
            progress in 51..99 -> {
                paint.color = highColor //高进度颜色
            }
            else -> {
                paint.color = fullColor //满进度颜色
            }
        }
        paint.strokeWidth = stroke
        // 需要优化，不能在onDraw创建对象
//        RectF oval = new RectF(minRadius, minRadius, width - minRadius, width - minRadius);
// 这是画蓝弧，第三个参数false是指，不连接到圆心
        canvas.drawArc(circleRect, -90f, -360f * progress / maxSize, false, paint)
        if (progress != 0) { // 起始点
            paint.style = Paint.Style.FILL
            paint.strokeWidth = 1f
            canvas.drawArc(startRect, 90f, -180f, false, paint)
            // 结束点 为了简单画圆点，可优化
            val endX = width / 2 - (radius - stroke) * sin(360f * progress / maxSize * Math.PI / 180)
            val endY = width / 2 - (radius - stroke) * cos(360f * progress / maxSize * Math.PI / 180)
            val len = radius * cos(360f * progress / maxSize * Math.PI / 180)
            Log.i(TAG, (360 * progress / maxSize).toString() + "," + endX + "," + endY+","+len)
            canvas.drawCircle(endX.toFloat(), endY.toFloat(), minRadius, paint)
        }
        // 写字 会有越界问题
        paint.strokeWidth = 1.0f
        paint.style = Paint.Style.STROKE
        if (TextUtils.isEmpty(maxTipText) || progress < 100) {
            paint.textSize = textSize
            paint.getTextBounds(text, 0, text.length, endRect)
            paint.color = textColor
            paint.style = Paint.Style.FILL
            canvas.drawText(
                text, width / 2f - endRect.width() / 2f,
                width / 2 + endRect.height() / 2f, paint
            )
        } else {
            paint.textSize = textSize
            paint.getTextBounds(text, 0, text.length, endTopRect)
            paint.color = textColor
            paint.style = Paint.Style.FILL
            canvas.drawText(
                text, width / 2f - endTopRect.width() / 2f,
                width / 2 - endTopRect.height() / 5f * 1f, paint
            )
            paint.textSize = fullTextSize
            paint.getTextBounds(maxTipText, 0, maxTipText.length, endBottomRect)
            paint.color = fullTextColor
            canvas.drawText(
                maxTipText, width / 2 - endBottomRect.width() / 2f,
                width / 2 + endBottomRect.height() / 4f * 5f, paint
            )
        }
    }

    /**
     * 初始设置当前进度的最大值-默认100
     *
     * @param max
     */
    fun setMaxSize(max: Int) {
        maxSize = max
    }

    /**
     * 更新进度和文字
     *
     * @param progress
     * @param text
     */
    fun setProgressAndText(progress: Int, text: String) {
        this.progress = progress
        this.text = text
        postInvalidate()
    }
}