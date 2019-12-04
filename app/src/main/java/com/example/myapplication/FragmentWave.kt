package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_new_wave.*

/**
 * 물결 형태 타이머 클래스
 * 물결 형태 타이머 이미지를 매개하는 클래스입니다.
 */
class FragmentWave: androidx.fragment.app.Fragment(),  TimerController {

    var waveTimerView: WaveTimerView? = null
        private set
    private var myContext: Context? = null

    // Fragment View에 부착되었을 때 불리는 함수, 컨텍스트를 저장함
    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.activity_new_wave,
                container, false)
    }

    // WaveTimerView 객체를 생성하고 현재 클래스에서 관리할 수 있게 지역변수에 할당합니다.
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        waveTimerView = WaveTimerView(myContext!!, waveView,33, MainActivity.currentPreset.name.toString() ,MainActivity.currentPreset.givenTime)
        waveTimerView!!.setWaveDrawable(R.drawable.gradient_sulphur, Color.argb(255,21,27,41), PorterDuff.Mode.SRC_ATOP)

        // 화면을 터치하면 타이머를 토글합니다.
        waveView.setOnClickListener {
            waveTimerView!!.toggleTimer()
        }
    }
    /**
     * 타이머 이미지의 end()를 호출합니다.
     */
    override fun endTimer() {
        waveTimerView?.end()
    }
    /**
     * 타이머 이미지의 restart()를 호출합니다.
     */
    override fun restartTimer() {
        waveTimerView?.restart()
    }
    /**
     * 타이머 이미지의 saveResult()를 호출합니다.
     */
    override fun saveTimer() {
        waveTimerView?.saveResult()
    }
    /**
     * 타이머 이미지의 overTime()를 호출합니다.
     */
    override fun startOverTimer() {
        waveTimerView?.overTime()
    }
    /**
     * 타이머 이미지의 refresh()를 호출합니다.
     */
    override fun refreshTimer(init: Boolean) {
        waveTimerView?.refresh(init)
    }
}