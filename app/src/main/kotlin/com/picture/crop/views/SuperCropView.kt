package com.picture.crop.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewManager
import com.picture.crop.R
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.dip
import org.jetbrains.anko.displayMetrics
import org.jetbrains.anko.toast
import java.io.IOException
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


@Suppress("NOTHING_TO_INLINE")
inline fun ViewManager.squareCropView(theme: Int = 0) = mSquareCropView({}, theme)

inline fun ViewManager.mSquareCropView(init: SquareCropView.() -> Unit, theme: Int = 0) = ankoView(::SquareCropView, theme, init)

class SquareCropView : View {
    private val tag: String? by lazy {
        this::class.simpleName
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, theme: Int = 0) : super(context, attributeSet, theme)

    private var mSourcePath: Uri? = null
    private var srcBitmap: Bitmap? = null
    private var mScale = 1f
    private var mScaleX = 1f
    private var mScaleY = 1f
    private var mScaleFactor = 1f
    private val mDefaultRect: Rect by lazy {
        val rect = Rect()
        rect.top = dip(48)
        rect.bottom = dip(48) + width
        rect.left = 0
        rect.right = width
        rect
    }
    private val mRectPaint: Paint by lazy {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = context.resources.getColor(R.color.colorAccent)
        paint
    }
    private var mLatestX = 0f
    private var mLatestY = 0f
    private var isWidthFit = false
    private var mDefaultTransY = 0f
    private var mDefaultTransX = 0f
    private val mMaxTransY by lazy {
        dip(48).toFloat()
    }
    private val mOriginMatrix by lazy {
        Matrix()
    }

    private val mFlipMatrix by lazy {
        Matrix()
    }

    private val mRotateMatrix by lazy {
        Matrix()
    }

    private val mOptionMatrix by lazy {
        Matrix()
    }
    private val mPaint: Paint by lazy {
        val paint = Paint()
        paint.isDither = true
        paint.isAntiAlias = true
        paint
    }
    private val mScaleDetector: ScaleGestureDetector by lazy {
        ScaleGestureDetector(context, ScaleGestureListener())
    }
    private var scaling = false
    private val mPioX: Float by lazy {
        width.toFloat() / 2
    }
    private val mPioY: Float by lazy {
        mPioX + dip(48)
    }
    private var isBusy = false


    private var rotateDegree: Float = 0f
    private var isFlipped = false

    /**
     * start crop
     * @param path source bitmap path
     */
    fun startCropProcess(@NonNull path: Uri) {
        mSourcePath = path
        // clear latest picture config
        mRotateMatrix.reset()
        mFlipMatrix.reset()
        mDefaultTransX = 0f
        mDefaultTransY = 0f
        isFlipped = false
        rotateDegree = 0f

        Observable.create<Boolean> {
            try {
                val stream = context.contentResolver.openInputStream(path)
                srcBitmap = BitmapFactory.decodeStream(stream)
                stream.close()
                it.onNext(true)
            } catch (e: IOException) {
                e.printStackTrace()
                it.onNext(false)
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        context.toast("error on crop measure")
                    }

                    override fun onNext(t: Boolean) {
                        if (t) {
                            val actualWidth = if (width != 0) width else context.displayMetrics.widthPixels
                            //cause we need a square picture
                            mScaleX = srcBitmap?.width?.div(actualWidth.toFloat()) ?: 1f
                            mScaleY = srcBitmap?.height?.div(actualWidth.toFloat()) ?: 1f
                            if (mScaleX >= mScaleY) {
                                heightFit(actualWidth)
                            } else {
                                widthFit(actualWidth)
                            }
                            postInvalidate()
                        } else {
                            context.toast("error on crop measure")
                        }
                    }

                    override fun onSubscribe(d: Disposable) {

                    }
                })
    }

    /**
     * start crop
     */
    fun startCropProcess(@NonNull bitmap: Bitmap) {
        // clear latest picture config
        mRotateMatrix.reset()
        mFlipMatrix.reset()
        mDefaultTransX = 0f
        mDefaultTransY = 0f
        isFlipped = false
        rotateDegree = 0f

        Observable.create<Boolean> {
            try {
                srcBitmap = bitmap
                it.onNext(true)
            } catch (e: IOException) {
                e.printStackTrace()
                it.onNext(false)
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        context.toast("error on crop measure")
                    }

                    override fun onNext(t: Boolean) {
                        if (t) {
                            val actualWidth = if (width != 0) width else context.displayMetrics.widthPixels
                            //cause we need a square picture
                            mScaleX = srcBitmap?.width?.div(actualWidth.toFloat()) ?: 1f
                            mScaleY = srcBitmap?.height?.div(actualWidth.toFloat()) ?: 1f
                            if (mScaleX >= mScaleY) {
                                heightFit(actualWidth)
                            } else {
                                widthFit(actualWidth)
                            }
                            postInvalidate()
                        } else {
                            context.toast("error on crop measure")
                        }
                    }

                    override fun onSubscribe(d: Disposable) {

                    }
                })
    }

    /**
     * rotate picture
     * @param degree rotate degree
     */
    fun rotate(degree: Float) {
        if (null == srcBitmap) {
            Log.e(tag, "no source find, please set source bitmap first")
            return
        }
        if (isBusy) {
            Log.e(tag, "option is not allowed when animation run")
            return
        }
        rotateDegree = (rotateDegree + degree) % 360
        mRotateMatrix.postRotate(degree, mPioX, mPioY)
        postInvalidate()
    }

    /**
     * flip current picture
     */
    fun flip() {
        if (null == srcBitmap) {
            Log.e(tag, "no source find, please set source bitmap first")
            return
        }
        if (isBusy) {
            Log.e(tag, "option is not allowed when animation run")
            return
        }
        isFlipped = !isFlipped
        mFlipMatrix.postScale(-1f, 1f, mPioX, mPioY)
        postInvalidate()
    }

    /**
     * reset all the option you did on this picture
     */
    fun reset() {
        if (null == srcBitmap) {
            Log.e(tag, "no source find, please set source bitmap first")
            return
        }
        if (isBusy) {
            Log.e(tag, "option is not allowed when animation run")
            return
        }
        isFlipped = false
        rotateDegree = 0f
        mFlipMatrix.reset()
        mRotateMatrix.reset()
        mScaleFactor = 1f
        mOriginMatrix.setScale(1.div(mScale), 1.div(mScale))
        mOriginMatrix.postTranslate(mDefaultTransX, mDefaultTransY)
        postInvalidate()
    }

    /**
     * call this to crop picture and result store to dst path from you request, you would better call this in non ui thread
     */
    fun applyCropOption(): Bitmap? {
        srcBitmap?.let {
            val mrBitmap = Bitmap.createBitmap(width, height, it.config)
            val canvas = Canvas(mrBitmap)
            canvas.matrix = mOptionMatrix
            canvas.drawBitmap(it, 0f, 0f, mPaint)
            val result = Bitmap.createBitmap(mrBitmap, mDefaultRect.left, mDefaultRect.top, mDefaultRect.width(), mDefaultRect.height())
            mrBitmap.recycle()
            return result
        }
        return null
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        srcBitmap?.let {
            mOptionMatrix.set(mOriginMatrix)
            mOptionMatrix.postConcat(mFlipMatrix)
            mOptionMatrix.postConcat(mRotateMatrix)
            canvas?.drawBitmap(it, mOptionMatrix, mPaint)
        }
        canvas?.drawRect(mDefaultRect, mRectPaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isBusy) {
            return false
        }
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mLatestX = event.x
                mLatestY = event.y
                mScaleDetector.onTouchEvent(event)
            }

            MotionEvent.ACTION_MOVE -> {
                mScaleDetector.onTouchEvent(event)
                if (!scaling) {
                    val deltaX = event.x - mLatestX
                    val deltaY = event.y - mLatestY
                    val pointF = calculateDelta(deltaX, deltaY)
                    mOriginMatrix.postTranslate(pointF.x, pointF.y)
                }
                mLatestX = event.x
                mLatestY = event.y
                ViewCompat.postInvalidateOnAnimation(this)
            }

            MotionEvent.ACTION_UP -> {
                fixTrans()
            }
        }
        return true
    }

    private fun heightFit(actualWidth: Int) {
        srcBitmap?.let {
            isWidthFit = false
            mDefaultTransY = dip(48).toFloat()
            val mWidth = it.width.div(mScaleY).toInt()
            mDefaultTransX = (actualWidth - mWidth) / 2f
            Log.i(tag, "m default transY: $mDefaultTransY, scaleY :$mScaleY")
            mOriginMatrix.setScale(1.div(mScaleY), 1.div(mScaleY))
            mOriginMatrix.postTranslate(mDefaultTransX, mDefaultTransY)
            mScale = mScaleY
        }
    }

    private fun widthFit(actualWidth: Int) {
        srcBitmap?.let {
            isWidthFit = true
            val mHeight: Int = it.height.div(mScaleX).toInt()
            mDefaultTransY = dip(48) - (mHeight - actualWidth) / 2f
            Log.i(tag, "m default transY: $mDefaultTransY")
            mOriginMatrix.setScale(1.div(mScaleX), 1.div(mScaleX))
            mOriginMatrix.postTranslate(mDefaultTransX, mDefaultTransY)
            mScale = mScaleX
            Log.i(tag, "width fit")
        }
    }

    private fun calculateDelta(inputX: Float, inputY: Float): PointF {
        var resultX = inputX
        var resultY = inputY
        when (rotateDegree) {
            0f -> {
                if (isFlipped) {
                    resultX = -inputX
                }
            }
            90f -> {
                resultX = if (isFlipped) -inputY else inputY
                resultY = -inputX
            }
            180f -> {
                resultX = if (isFlipped) inputX else -inputX
                resultY = -inputY
            }
            270f -> {
                resultX = if (isFlipped) inputY else -inputY
                resultY = inputX
            }
        }
        return PointF(resultX, resultY)
    }

    private fun fixTrans() {
        val values = FloatArray(9)
        mOriginMatrix.getValues(values)
        val animator = ObjectAnimator.ofFloat(0f, 1f)
        var mFinalTransX = values[2]
        var mFinalTransY = values[5]
        var needFix = false
        animator.duration = 300
        if (values[2] > 0) {
            needFix = true
            mFinalTransX = 0f
        } else if (values[2] < (width - (srcBitmap?.width?.times(values[0]) ?: 0f))) {
            mFinalTransX = width - (srcBitmap?.width?.times(values[0]) ?: 0f)
            needFix = true
        }

        if (values[5] > mMaxTransY) {
            mFinalTransY = mMaxTransY
            needFix = true
        } else if (values[5] < width + dip(48) - (srcBitmap?.height?.times(values[0]) ?: 0f)) {
            mFinalTransY = width + dip(48) - (srcBitmap?.height?.times(values[0]) ?: 0f)
            needFix = true
        }
        if (!needFix) {
            return
        }
//        mFinalTransX = mFinalTransX
//        mFinalTransY = mFinalTransY
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            mOriginMatrix.setScale(values[0], values[0])
            mOriginMatrix.postTranslate(values[2] + value * (mFinalTransX - values[2]), values[5] + value * (mFinalTransY - values[5]))
            ViewCompat.postInvalidateOnAnimation(this)
        }
        animator.start()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                isBusy = false
                super.onAnimationEnd(animation)
            }

            override fun onAnimationStart(animation: Animator?) {
                isBusy = true
                super.onAnimationStart(animation)
            }
        })
    }

    inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            val scale = detector?.scaleFactor ?: 1f
            mScaleFactor *= scale

            if (mScaleFactor > 2f) {
                mScaleFactor = 2f
                scaling = false
                return true
            }
            if (mScaleFactor < 1f) {
                mScaleFactor = 1f
                scaling = false
                return true
            }
            mOriginMatrix.postScale(scale, scale, mPioX, mPioY)
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            Log.i(tag, "on scale begin")
            scaling = true
            return super.onScaleBegin(detector)
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            Log.i(tag, "on scale end")
            scaling = false
            super.onScaleEnd(detector)
        }
    }
}