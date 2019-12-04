package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_new_running.*

/**
 * 달리는 말 형태의 타이머 클래스
 * 말 형태 타이머 이미지 클래스를 매개하는 클래스입니다
 */
class FragmentRunning: androidx.fragment.app.Fragment(), TimerController {

    private var myContext: Context? = null

    // Fragment View에 부착되었을 때 불리는 함수, 컨텍스트를 저장함
    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    private var runningTimerView: RunningTimerView? = null
    // 순차적으로 순환할 리소스(정수) 배열
    private val imageArray : Array<Int> = arrayOf(
            R.drawable.ic_running_horse_1, R.drawable.ic_running_horse_2, R.drawable.ic_running_horse_3,
            R.drawable.ic_running_horse_4, R.drawable.ic_running_horse_5, R.drawable.ic_running_horse_6,
            R.drawable.ic_running_horse_7, R.drawable.ic_running_horse_8, R.drawable.ic_running_horse_9,
            R.drawable.ic_running_horse_10, R.drawable.ic_running_horse_11, R.drawable.ic_running_horse_12
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.activity_new_running,
                container, false)
    }

    // RunningTimerView 객체를 생성하고 현재 클래스에서 관리할 수 있게 지역변수에 할당합니다.
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        timerText.text = CorocUtil.timeToHMSFormat(MainActivity.currentPreset.givenTime)
        runningTimerView = RunningTimerView(
                myContext!!, runningImage, timerText, imageArray, 150, MainActivity.currentPreset.name.toString(), MainActivity.currentPreset.givenTime, R.color.colorYellow, R.color.neonRed
        )

        // 화면을 터치하면 타이머를 토글합니다.
        runningRootView.setOnClickListener {
            runningTimerView!!.toggleTimer()
        }
    }

    /**
     * 타이머 이미지의 end()를 호출합니다.
     */
    override fun endTimer() {
        runningTimerView?.end()
    }
    /**
     * 타이머 이미지의 restart()를 호출합니다.
     */
    override fun restartTimer() {
        runningTimerView?.restart()
    }
    /**
     * 타이머 이미지의 saveResult()를 호출합니다.
     */
    override fun saveTimer() {
        runningTimerView?.saveResult()
    }
    /**
     * 타이머 이미지의 overTime()를 호출합니다.
     */
    override fun startOverTimer() {
        runningTimerView?.overTime()
    }
    /**
     * 타이머 이미지의 refresh()를 호출합니다.
     */
    override fun refreshTimer(init: Boolean) {
        runningTimerView?.refresh(init)
    }
}