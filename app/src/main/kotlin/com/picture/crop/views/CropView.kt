package com.picture.crop.views

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.Log
import android.view.*
import com.picture.crop.R

class CropView : View {
    private val rectPaint = Paint()
    private val backgroundPaint = Paint()
    private val ratio: Float
    private val cropRectanglePadding: Float
    private val rectangleColor: Int
    private val coverBackground: Int
    private var rectangleHeight = 0
    private var picture: Bitmap? = null
    private val scaleDetector = ScaleGestureDetector(context, ScaleGestureListener())
    private val scrollDetector = GestureDetector(context, ScrollGestureListener())
    private var transX = 0f
    private var transY = 0f
    private var mScaleFactor = 1f
    private val mPictureRect = RectF()
    private val mCropRect = RectF()
    var pictureRatio = 1f


    private var scaling = false


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, -1)
    constructor(context: Context, attributeSet: AttributeSet?, theme: Int) : super(context, attributeSet, theme) {

        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.CropView)
        ratio = typeArray.getFloat(R.styleable.CropView_ratio, 1f)
        rectangleColor = typeArray.getColor(R.styleable.CropView_rectangleColor, Color.WHITE)
        coverBackground = typeArray.getColor(R.styleable.CropView_coverColor, Color.BLACK)
        cropRectanglePadding = typeArray.getDimension(R.styleable.CropView_cropRectanglePadding, 4f)
        typeArray.recycle()
        rectPaint.color = rectangleColor
        rectPaint.strokeJoin = Paint.Join.ROUND
        rectPaint.style = Paint.Style.STROKE
        rectPaint.isAntiAlias = true
        rectPaint.isDither = true
        rectPaint.pathEffect = CornerPathEffect(6f)
        rectPaint.strokeWidth = 3f
        rectPaint.strokeCap = Paint.Cap.ROUND

        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = coverBackground
    }

    fun setBitmapResource(uri: Uri) {
        Log.i("cropView", "get uri: " + uri.toString())
        this.picture = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        viewTreeObserver.addOnGlobalLayoutListener {

            if (picture?.width ?: 0 > measuredWidth || picture?.height ?: 0 > measuredHeight) {
                pictureRatio = maxOf(picture?.height?.div(measuredHeight.toFloat()) ?: 1f, picture?.width?.div(measuredWidth.toFloat()) ?: 1f)

            }
            transY = measuredHeight.minus(picture?.height?.div(pictureRatio)?.toInt() ?: 0).div(2).toFloat()
            transX = measuredWidth.minus(picture?.width?.div(pictureRatio)?.toInt() ?: 0).div(2).toFloat()
            mPictureRect.set(transX, transY, transX.plus(picture?.width?.div(pictureRatio)?.toInt() ?: 0), transY.plus(picture?.height?.div(pictureRatio)?.toInt() ?: 0))
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        rectangleHeight = (measuredWidth - cropRectanglePadding).times(ratio).toInt()
        mCropRect.set(cropRectanglePadding / 2, (measuredHeight - rectangleHeight) / 2f, measuredWidth.toFloat() - cropRectanglePadding / 2,
                (measuredHeight + rectangleHeight) / 2f)
        canvas?.drawRect(0f, 0f, measuredHeight.toFloat(), measuredHeight.toFloat(), backgroundPaint)
        if (picture != null) {
            canvas?.drawBitmap(picture, null, mPictureRect, null)
        }
        canvas?.drawRect(cropRectanglePadding / 2, (measuredHeight - rectangleHeight) / 2f, measuredWidth.toFloat() - cropRectanglePadding / 2,
                (measuredHeight + rectangleHeight) / 2f, rectPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val scroll = scrollDetector.onTouchEvent(event)
        val scale = scaleDetector.onTouchEvent(event)
        if (scale || scroll) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
        return true
    }

    fun getCropBitmap(): Bitmap? {
        if (mPictureRect.top >= mCropRect.bottom || mPictureRect.bottom <= mCropRect.top || mPictureRect.left >= mCropRect.right ||
                mPictureRect.right < mCropRect.left) {
            return picture
        }

        val ratioX = mPictureRect.width().div(picture?.width?.toFloat() ?: 1f)
        val ratioY = mPictureRect.height().div(picture?.height?.toFloat() ?: 1f)

        var startY = 0f
        var startX = 0f
        var height = mPictureRect.height().div(ratioY)
        var width = mPictureRect.width().div(ratioX)
        if (mPictureRect.top < mCropRect.top) {
            startY = mCropRect.top - mPictureRect.top
            height = (mPictureRect.height() - startY).div(ratioY)
            startY = startY.div(ratioY)
        }
        if (mPictureRect.bottom > mCropRect.bottom) {
            height += (mCropRect.bottom - mPictureRect.bottom).div(ratioY)
        }

        if (mPictureRect.left < mCropRect.left) {
            startX = mCropRect.left - mPictureRect.left
            width = (mPictureRect.width() - startX).div(ratioX)
            startX = startX.div(ratioX)
        }
        if (mPictureRect.right > mCropRect.right) {
            width += (mCropRect.right - mPictureRect.right).div(ratioX)
        }
        return Bitmap.createBitmap(picture, startX.toInt(), startY.toInt(), width.toInt(), height.toInt())
    }

    inner class ScrollGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            if (!scaling) {
                mPictureRect.bottom -= distanceY
                mPictureRect.top -= distanceY
                mPictureRect.left -= distanceX
                mPictureRect.right -= distanceX
            }
            return true
        }
    }

    inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            scaling = true
            val scale = detector?.scaleFactor ?: 1f
            mScaleFactor *= scale

            if (mScaleFactor > 3f) {
                mScaleFactor = 3f
                scaling = false
                return true
            }
            if (mScaleFactor < 0.5f) {
                mScaleFactor = 0.5f
                scaling = false
                return true
            }
            val deltaX = picture?.width?.div(pictureRatio)?.times(scale - 1f)?.div(2) ?: 0f
            val deltaY = picture?.height?.div(pictureRatio)?.times(scale - 1f)?.div(2) ?: 0f
            mPictureRect.left = mPictureRect.left - deltaX
            mPictureRect.right = mPictureRect.right + deltaX
            mPictureRect.top = mPictureRect.top - deltaY
            mPictureRect.bottom = mPictureRect.bottom + deltaY
            scaling = false
            return true
        }
    }

}