package com.picture.crop

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.picture.crop.views.SquareCropView
import com.picture.crop.views.squareCropView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class SuperCropActivity : AppCompatActivity() {
    private val tag by lazy {
        this::javaClass.name
    }

    companion object {
        val SELECT_PICTURE_FROM_GALLERY = 1
    }

    private val mUI by lazy {
        ConversionActivityUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUI.setContentView(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        Observable.create<Bitmap> {
            val options = BitmapFactory.Options()
            options.inScaled = false
            val defaultPic = BitmapFactory.decodeResource(resources, R.mipmap.example1, options)
            it.onNext(defaultPic)
        }.subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Bitmap> {
                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onNext(t: Bitmap) {
                        mUI.squareCropView.startCropProcess(t)
                    }

                    override fun onSubscribe(d: Disposable) {

                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Activity.RESULT_OK != resultCode) {
            return
        }
        when (requestCode) {
            SELECT_PICTURE_FROM_GALLERY -> {
                val uri = data?.data
                if (null != uri) {
                    mUI.squareCropView.startCropProcess(uri)
                }
            }
        }
    }

    internal fun selectFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, SELECT_PICTURE_FROM_GALLERY)
    }
}

class ConversionActivityUI : AnkoComponent<SuperCropActivity> {
    private val idSelect = 1
    private val idRotate90 = 2
    private val idRotate180 = 3
    private val idReset = 4
    internal lateinit var squareCropView: SquareCropView
    override fun createView(ui: AnkoContext<SuperCropActivity>): View = ui.apply {
        relativeLayout {
            squareCropView = squareCropView().lparams {
                width = matchParent
                height = matchParent
            }
            button("Select_from_gallery") {
                id = idSelect
                onClick {
                    ui.owner.selectFromGallery()
                }
            }.lparams {
                height = dip(48)
                alignParentBottom()
                centerHorizontally()
            }

            val cropImg = imageView {
                visibility = View.GONE
            }.lparams {
                centerInParent()
            }

            button("RESET") {
                id = idReset
                onClick {
                    cropImg.visibility = View.GONE
                    squareCropView.visibility = View.VISIBLE
                    squareCropView.reset()
                }
            }.lparams {
                above(idSelect)
                bottomMargin = dip(24)
                leftMargin = dip(10)
            }

            button("ROTATE_90") {
                onClick {
                    squareCropView.rotate(90f)
                }
                id = idRotate90
            }.lparams {
                above(idSelect)
                alignParentRight()
                bottomMargin = dip(24)
                rightMargin = dip(10)
            }
            button("mirror") {
                id = idRotate180
                onClick {
                    squareCropView.flip()
                }
            }.lparams {
                above(idSelect)
                leftOf(idRotate90)
                bottomMargin = dip(24)
                rightMargin = dip(10)
            }
            button("apply") {
                onClick {
                    isClickable = false
                    Observable.create<Bitmap> {
                        val result = squareCropView.applyCropOption()
                        if (null != result) {
                            it.onNext(result)
                        }
                        it.onComplete()
                    }.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : Observer<Bitmap> {
                                override fun onError(e: Throwable) {
                                    e.printStackTrace()
                                    isClickable = true
                                }

                                override fun onComplete() {
                                    isClickable = true
                                }

                                override fun onNext(t: Bitmap) {
                                    cropImg.visibility = View.VISIBLE
                                    cropImg.imageBitmap = t
                                    squareCropView.visibility = View.GONE
                                }

                                override fun onSubscribe(d: Disposable) {

                                }
                            })
                }
            }
        }
    }.view
}

