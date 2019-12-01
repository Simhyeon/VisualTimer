package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_new_wave.*


class FragmentWave: androidx.fragment.app.Fragment(),  TimerController {

    var waveTimerView: WaveTimerView? = null
        private set
    private var myContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.activity_new_wave,
                container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        waveTimerView = WaveTimerView(myContext!!, waveRootView, waveView,33, DynamicActivity.currentPreset.name.toString() ,DynamicActivity.currentPreset.givenTime)
        waveTimerView!!.setWaveDrawable(R.drawable.gradient_sulphur, Color.argb(255,21,27,41), PorterDuff.Mode.SRC_ATOP)

        waveView.setOnClickListener {
            waveTimerView!!.toggleTimer()
        }
    }

    override fun endTimer() {
        waveTimerView?.end()
    }

    override fun restartTimer() {
        waveTimerView?.restart()
    }

    override fun saveTimer() {
        waveTimerView?.saveResult() // Should check here
    }

    override fun startOverTimer() {
        waveTimerView?.overTime()
    }

    override fun refreshTimer() {
        waveTimerView?.refresh()
    }
}