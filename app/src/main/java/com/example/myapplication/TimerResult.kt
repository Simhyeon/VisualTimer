package com.example.myapplication

/**
 * 타이머에서 타이머 결과를 관리하기 위한 인터페이스입니다.
 * FragmentTimer 클래스는 타이머 이미지 클래스를 생성하고 지역 변수에 할당하여
 * 각각의 타이머 별로 다른 end, restart, saveResult, overTime, refresh 기능의 구현을 알지 않아도 됩니다.
 * 현재 Sector나 Ring의 경우에는 타이머 이미지 클래스와 FragmentTimer 클래스가 분리되어 있지 않습니다.
 */
interface TimerResult {
    fun end()
    fun restart()
    fun saveResult()
    fun overTime()
    fun refresh(init: Boolean)
}