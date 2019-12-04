package com.example.myapplication

/**
 * 메인 액티비티에서 각각의 타이머들을 제어하기 위한 인터페이스입니다.
 * 메인 액티비티는 각 메서드의 효과만을 알고, 그 구현을 알지 않아도 됩니다.
 */
interface TimerController {

    fun endTimer()
    fun restartTimer()
    fun saveTimer()
    fun startOverTimer()
    fun refreshTimer(init: Boolean)
}