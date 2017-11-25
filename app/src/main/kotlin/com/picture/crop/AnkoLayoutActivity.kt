package com.picture.crop

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.picture.crop.views.CropView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class AnkoLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnkoLayoutAcitivityUI().setContentView(this)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }
}

class AnkoLayoutAcitivityUI : AnkoComponent<AnkoLayoutActivity> {
    override fun createView(ui: AnkoContext<AnkoLayoutActivity>) = ui.apply {
        val ID_NAME = 1
        val ID_AGE = 2
        verticalLayout {
            val name = textView("name: jake") {
                textSize = 16f
                gravity = Gravity.CENTER_HORIZONTAL
                width = dip(64)
                height = dip(24)
            }
            button(R.string.app_name) {
                id = ID_NAME
                onClick { Toast.makeText(ctx, "Hello, ${name.text}", LENGTH_SHORT).show() }
            }.lparams {
                width = matchParent
                topMargin = dip(12)
                leftMargin = dip(32)
                rightMargin = dip(32)
            }
            val age = textView("age: 25") {
                gravity = Gravity.CENTER_HORIZONTAL
                textSize = 16f
                width = dip(64)
                height = dip(24)
            }
            button("tell me your age") {
                id = ID_AGE
                onClick { Toast.makeText(ctx, "my age is ${age.text}", LENGTH_SHORT).show() }
            }.lparams {
                width = matchParent
                topMargin = dip(12)
                leftMargin = dip(32)
                rightMargin = dip(32)
            }
            CropView(ctx, attributeSet = null).lparams {
            }
        }
    }.view
}