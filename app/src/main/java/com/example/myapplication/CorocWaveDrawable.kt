package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable

class CorocWaveDrawable: WaveDrawable {
    constructor(drawable: Drawable?) : super(drawable)
    constructor(context: Context, imgRes: Int) : super(context, imgRes)
    constructor(context: Context, imgRes: Int, bgColorFilter: Int, filterMode: PorterDuff.Mode): super(context, imgRes) { // 배경색을 생성자에서 받아옴.
        val colorFilter = PorterDuffColorFilter(bgColorFilter, filterMode)
        bgFilter = colorFilter
    }
}