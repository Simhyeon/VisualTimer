package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_new_running.*

class FragmentRunning: androidx.fragment.app.Fragment(), TimerController {

    private var myContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    private var runningTimerView: RunningTimerView? = null
    // 순차적으로 순환할 리소스(정수) 배열
    private val imageArray : Array<Int> = arrayOf( // 벡터 파일은 제공되지 않음.
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        timerText.text = CorocUtil.timeToHMSFormat(DynamicActivity.currentPreset.givenTime)
        runningTimerView = RunningTimerView(
                myContext!!,runningRootView, runningImage, timerText, imageArray, 150, DynamicActivity.currentPreset.name.toString(), DynamicActivity.currentPreset.givenTime, R.color.colorYellow, R.color.neonRed
        )

        runningImage.setOnClickListener {
            runningTimerView!!.toggleTimer()
        }
    }

    override fun onStop() {
        //StatManager.update(Result(intent.getStringExtra("PresetID"), false, intent.getIntExtra("GivenTime", 50), 20))
        super.onStop()
        if (runningTimerView != null) {
            runningTimerView!!.clearVars()
        }
    }

    override fun endTimer() {
        runningTimerView?.end()
    }

    override fun restartTimer() {
        runningTimerView?.restart()
    }

    override fun saveTimer() {
        runningTimerView?.saveResult()
    }

    override fun startOverTimer() {
        runningTimerView?.overTime()
    }

    override fun refreshTimer() {
        runningTimerView?.refresh()
    }
}