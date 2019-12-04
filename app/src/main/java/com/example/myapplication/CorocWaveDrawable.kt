package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable

/**
 * 배경 필터를 설정할 수 있는 Drawable입니다.
 * 물결 타이머에 사용됩니다.
 * Drawable은 팀프로젝트에서 작성한 코드가 아닙니다.
 */
class CorocWaveDrawable: WaveDrawable {
    // Drawable의 생성자를 상속합니다.
    constructor(drawable: Drawable?) : super(drawable)
    // Drawable의 생성자를 상속합니다.
    constructor(context: Context, imgRes: Int) : super(context, imgRes)
    /**
     * 배경 컬러필터와 컬러필터 모드를 설정하는 상속자입니다.
     */
    constructor(context: Context, imgRes: Int, bgColorFilter: Int, filterMode: PorterDuff.Mode): super(context, imgRes) { // 배경색을 생성자에서 받아옴.
        val colorFilter = PorterDuffColorFilter(bgColorFilter, filterMode)
        bgFilter = colorFilter
    }
}