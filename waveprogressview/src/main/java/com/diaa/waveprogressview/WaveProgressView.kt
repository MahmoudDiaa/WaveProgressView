  package com.diaa.waveprogressview


import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.text.DecimalFormat
import kotlin.math.min
import kotlin.math.roundToInt


open class WaveProgressView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    private var radius = dp2px(55)
    private val textColor: Int
    private val textSize: Int
    private val progressColor: Int
    private val radiusColor: Int
    private val textPaint: Paint
    private val circlePaint: Paint
    private val pathPaint: Paint
    private var bitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null
    private var width = 0.0
    private var height = 0.0
    private var minPadding = 0
    private var progress: Float
    private val maxProgress: Float
    private val path: Path = Path()
    private val df: DecimalFormat = DecimalFormat("0.0")

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val exceptW: Int = paddingLeft + paddingRight + 2 * radius
        val exceptH: Int = paddingTop + paddingBottom + 2 * radius
        val width = resolveSize(exceptW, widthMeasureSpec)
        val height = resolveSize(exceptH, heightMeasureSpec)
        val min = min(width, height)
        this.height = min.toDouble()
        this.width = this.height

        val minLR: Int = min(paddingLeft, paddingRight)
        val minTB: Int = min(paddingTop, paddingBottom)
        minPadding = minLR.coerceAtMost(minTB)
        radius = (min - minPadding * 2) / 2
        setMeasuredDimension(min, min)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width.roundToInt(), height.toInt(), Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(bitmap!!)
        }
        bitmapCanvas?.save()
        bitmapCanvas?.translate(minPadding.toFloat(), minPadding.toFloat())
        bitmapCanvas?.drawCircle(radius.toFloat(), radius.toFloat(), radius.toFloat(), circlePaint)


        path.reset()
        val percent = progress * 1.0f / maxProgress
        val y = (1 - percent) * radius * 2
        path.moveTo((radius * 2).toFloat(), y)
        path.lineTo((radius * 2).toFloat(), (radius * 2).toFloat())
        path.lineTo(0F, (radius * 2).toFloat())

        path.lineTo(-(1 - percent) * radius * 2, y)
        if (progress != 0.0f) {

            val count = radius * 4 / 60
            val point = (1 - percent) * 15
            for (i in 0 until count) {
                path.rQuadTo(15F, -point, 30f, 0f)
                path.rQuadTo(15F, point, 30f, 0f)
            }
        }
        path.close()
        bitmapCanvas?.drawPath(path, pathPaint)

        val text = "$progress%"
        val textW: Float = textPaint.measureText(text)
        val fontMetrics: Paint.FontMetrics = textPaint.fontMetrics
        val baseLine: Float = radius - (fontMetrics.ascent + fontMetrics.descent) / 2
        bitmapCanvas?.drawText(text, radius - textW / 2, baseLine, textPaint)
        bitmapCanvas?.restore()
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
    }

    fun getProgress(): Float {
        return progress
    }

    private fun setProgress(progress: Float) {
        this.progress = java.lang.Float.valueOf(df.format(progress))
        invalidate()
    }

    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        )
            .toInt()
    }

    private class SavedState : BaseSavedState {
        var progress = 0f

        constructor(source: Parcel?) : super(source) {}
        constructor(superState: Parcelable?) : super(superState) {}

        companion object {
            @JvmField
            val CREATOR: Creator<SavedState?> = object : Creator<SavedState?> {
                override fun createFromParcel(`in`: Parcel): SavedState? {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState: Parcelable = super.onSaveInstanceState()!!
        val ss = SavedState(superState)
        ss.progress = progress
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        setProgress(ss.progress)
    }

    init {
        val a: TypedArray = getContext().obtainStyledAttributes(
            attrs,
            R.styleable.WaveProgressView,
            defStyleAttr,
            R.style.WaveProgressViewDefault
        )
        radius = a.getDimension(R.styleable.WaveProgressView_radius, radius.toFloat()).toInt()
        textColor = a.getColor(R.styleable.WaveProgressView_progress_text_color, 0)
        textSize = a.getDimensionPixelSize(R.styleable.WaveProgressView_progress_text_size, 0)
        progressColor = a.getColor(R.styleable.WaveProgressView_progress_color, 0)
        radiusColor = a.getColor(R.styleable.WaveProgressView_radius_color, 0)
        progress = a.getFloat(R.styleable.WaveProgressView_progress, 0f)
        maxProgress = a.getFloat(R.styleable.WaveProgressView_maxProgress, 100f)
        a.recycle()

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = textSize.toFloat()
        textPaint.color = textColor
        textPaint.isDither = true
        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint.color = radiusColor
        circlePaint.isDither = true
        pathPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        pathPaint.color = progressColor
        pathPaint.isDither = true
        pathPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }
}