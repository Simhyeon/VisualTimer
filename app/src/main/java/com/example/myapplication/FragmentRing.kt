package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_new_ring_revised.*
import kotlinx.coroutines.*

class FragmentRing: Fragment(), TimerController, TimerResult {

    private lateinit var myContext: Context
    private lateinit var mainActivity: DynamicActivity
    private var showFab = false
    private var useOverTime = false
    private var blockRun = false
    private var jobId : Job? = null
    private var overJob : Job? = null

    private var fillLevel = 0.0
    private var levelVariation = 0.0
    private val delayMilliSeconds = 33
    private var milliTimePassed = 0

    private var presetName = ""
    private var givenSeconds = 120

    private fun clearVars() {
        showFab = false
        useOverTime = false
        blockRun = false
        jobId?.cancel()
        overJob?.cancel()
        fillLevel = 0.0
        milliTimePassed = 0
        ring.percent = 100f
        ringTimerText.text = CorocUtil.timeToHMSFormat(DynamicActivity.currentPreset.givenTime)

        ring.fgColorEnd = getColor(myContext, R.color.colorYellow)
        ring.fgColorStart = getColor(myContext, R.color.colorYellow)
        ringTimerText.setTextColor(getColor(myContext, R.color.colorYellow))
        ring.startAngle = 0f
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
        mainActivity = context as DynamicActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.activity_new_ring_revised,
                container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        ringTimerText.text = CorocUtil.timeToHMSFormat(DynamicActivity.currentPreset.givenTime)
        presetName = DynamicActivity.currentPreset.name.toString()
        givenSeconds = DynamicActivity.currentPreset.givenTime
        levelVariation = CorocUtil.getLevelVariation(givenSeconds, delayMilliSeconds)

        ringRootView.setOnClickListener {
            toggleTimer()
        }
    }

    fun toggleTimer(){
        if (!mainActivity.isTimerRunning) {
            mainActivity.overButtonEnabled(false)
            mainActivity.hideMainFab(animation = false)
            mainActivity.isTimerRunning = true
        }

        if (showFab) {
            return
        }
        if (useOverTime){
            overTimer()
            return
        }
        if (blockRun){
            CorocUtil.timerToast(myContext, timerStart = false)
            jobId?.cancel()
            overJob?.cancel()
            showFab = true
            mainActivity.showScreenFab(animation = true)
            mainActivity.overButtonEnabled(false)
            return
        } else { // blockRun == false
            blockRun = true
        }

        CorocUtil.timerToast(myContext, timerStart = true)
        jobId = CoroutineScope(Dispatchers.Main).launch {
            while ((milliTimePassed < givenSeconds * 1000)) {
                delay(delayMilliSeconds.toLong())
                fillLevel += levelVariation
                ring.percent= (fillLevel / 100).toFloat()
                milliTimePassed += delayMilliSeconds
                ringTimerText.text = CorocUtil.timeToHMSFormat( givenSeconds - milliTimePassed / 1000)
            }
            CorocUtil.timerToast(myContext, timerStart = false)
            milliTimePassed = givenSeconds * 1000
            showFab = true
            mainActivity.showScreenFab(animation = true)
            if (presetName.isNotEmpty()) {mainActivity.overButtonEnabled(true)}
        }
    }

    fun overTimer() {
        if (showFab) {
            return
        }
        if (blockRun) {
            CorocUtil.timerToast(myContext, timerStart = false)
            showFab = true
            jobId?.cancel()
            overJob?.cancel()
            mainActivity.showScreenFab(animation = true)
            mainActivity.overButtonEnabled(false)
            return
        } else {
            blockRun = true
        }

        ring.fgColorEnd = getColor(myContext, R.color.neonRed)
        ring.fgColorStart = getColor(myContext, R.color.neonRed)
        ringTimerText.setTextColor(getColor(myContext, R.color.neonRed))

        CorocUtil.timerToast(myContext, timerStart = true)
        overJob = CoroutineScope(Dispatchers.Main).launch {
            while (blockRun) {
                ring.percent = 33f
                var angle = 0f
                val increament = CorocUtil.getLevelVariation(givenSeconds, delayMilliSeconds, 360.0)
                while (blockRun) {
                    delay(delayMilliSeconds.toLong())
                    angle += increament.toFloat()
                    ring.startAngle = angle
                    if (angle > 360) {
                        angle -= 360
                    }
//                delay(delayMilliSeconds.toLong())
//                fillLevel += levelVariation
//                ring.percent= (fillLevel / 100).toFloat()
//                if (fillLevel > 10000) {
//                    fillLevel -= 10000
                }
            }
        }
        jobId = CoroutineScope(Dispatchers.Main).launch {
            while (blockRun) {
                delay(1000) // Wait for 1 second
                milliTimePassed += 1000
                ringTimerText.text = CorocUtil.timeToHMSFormat(milliTimePassed / 1000 - givenSeconds)
            }
        }
    }
    // TimerController
    override fun endTimer() { end() }
    override fun restartTimer() { restart() }
    override fun saveTimer() { saveResult() }
    override fun startOverTimer() { overTime() }
    override fun refreshTimer() { refresh() }

    // TimerResult
    override fun end() {
        mainActivity.hideScreenFab(false)
        clearVars()
        mainActivity.isTimerRunning = false
        mainActivity.showMainFab(false)
    }

    override fun restart() {
        mainActivity.hideScreenFab(false)
        clearVars()
        toggleTimer()
    }

    override fun saveResult() {
        if (presetName == ""){
            Toast.makeText(context, "프리셋을 설정해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        StatManager.update(Result(presetName, useOverTime, givenSeconds, milliTimePassed / 1000))
        end()
    }

    override fun overTime() {
        mainActivity.hideScreenFab(false)
        showFab = false
        blockRun = false
        useOverTime = true
        ring.percent = 0f
        toggleTimer()
    }

    override fun refresh() {
        presetName = DynamicActivity.currentPreset.name.toString()
        givenSeconds = DynamicActivity.currentPreset.givenTime
        levelVariation = CorocUtil.getLevelVariation(givenSeconds, delayMilliSeconds)
        clearVars()
    }
}