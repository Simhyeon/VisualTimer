package com.example.myapplication

import android.content.Context
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat.getColor
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.math.log

class WaveTimerView(val context: Context, val rootView: View, val imageView: ImageView, val delayMilliSeconds: Int, var presetName: String, var givenSeconds: Int) : TimerResult {

    var waveDrawable: CorocWaveDrawable? = null
        private set

    var colorResSrc:Int = 0

    private var heightLevel= 0.0
    private var levelVariation = 0.0
    private var blockRun = false
    private var showFab = false
    private var jobId: Job? = null
    private var milliTimePassed = 0

    private var useOverTime = false

    init {
        if (delayMilliSeconds < 1) {
            throw IllegalArgumentException("DelayMilliSeconds should be natural numbers")
        }
        if (givenSeconds < 1) {
            throw IllegalArgumentException("Given seoncds should be positive integer")
        }
        this.levelVariation = CorocUtil.getLevelVariation(givenSeconds, this.delayMilliSeconds)
    }

    fun clearVars() {
        // Do not reset levelVariation
        waveDrawable!!.level = 0
        heightLevel= 0.0
        milliTimePassed = 0
        blockRun = false
        showFab = false
        jobId?.cancel()
        levelVariation = abs(levelVariation)

        useOverTime = false
        waveDrawable = CorocWaveDrawable(context, R.drawable.gradient_sulphur)
        imageView.setImageDrawable(waveDrawable)
    }


    fun setWaveDrawable(colorRes: Int) : CorocWaveDrawable? {
        colorResSrc = colorRes
        waveDrawable = CorocWaveDrawable(context, colorRes)
        imageView.setImageDrawable(waveDrawable)
        waveDrawable!!.level = 0
        return waveDrawable
    }

    fun setWaveDrawable(colorRes: Int, bgColorFilter: Int, filterMode: PorterDuff.Mode = PorterDuff.Mode.SRC) : CorocWaveDrawable? {
        colorResSrc = colorRes
        waveDrawable = CorocWaveDrawable(context, colorRes, bgColorFilter, filterMode)
        imageView.setImageDrawable(waveDrawable)
        return waveDrawable
    }

    fun toggleTimer() {
        if (!(context as DynamicActivity).isTimerRunning) {
            context.overButtonEnabled(false)
            context.hideMainFab(animation = false)
            context.isTimerRunning = true
        }

        if (showFab) {
            return
        }
        if (useOverTime){
            overTimer()
            return
        }
        if (blockRun){
            CorocUtil.timerToast(context, timerStart = false)
            jobId?.cancel()
            showFab = true
            context.showScreenFab(animation = true)
            context.overButtonEnabled(false)
            return
        } else { // blockRun == false
            blockRun = true
        }
        waveDrawable!!.level = 0
        CorocUtil.timerToast(context, timerStart = true)
        jobId =CoroutineScope(Dispatchers.Main).launch {
            while ((heightLevel in 0f..10000f)) {
                delay(delayMilliSeconds.toLong())
                heightLevel += levelVariation
                waveDrawable!!.level = heightLevel.toInt()
                milliTimePassed += delayMilliSeconds
            }
            if (heightLevel >= 10000) {
                waveDrawable!!.level = 10000
            }
            CorocUtil.timerToast(context, timerStart = false)
            milliTimePassed = givenSeconds * 1000
            showFab = true
            context.showScreenFab(animation = true)
            if (presetName.isNotEmpty()) {context.overButtonEnabled(true)}
        }
    }

    private fun overTimer() {
        if (showFab) {
            return
        }
        if (blockRun){
            CorocUtil.timerToast(context, timerStart = false)
            showFab = true
            jobId?.cancel()
            (context as DynamicActivity).showScreenFab(animation = true)
            context.overButtonEnabled(false)
            return
        } else {
            blockRun = true
        }
        //CorocUtil.resultOptionInt = R.layout.result_option
        heightLevel = 0.0
        CorocUtil.timerToast(context, timerStart = true)
        jobId =CoroutineScope(Dispatchers.Main).launch {
            while (blockRun) {
                delay(delayMilliSeconds.toLong())
                if (heightLevel >= 10000 || heightLevel < 0) {
                    levelVariation *= -1
                }
                when {
                    heightLevel > 10000.0 -> {
                        heightLevel = 10000.0
                        levelVariation = -abs(levelVariation)
                    }
                    heightLevel < 0.0 -> {
                        heightLevel = 0.0
                        levelVariation = abs(levelVariation)
                    }
                }
                heightLevel += levelVariation
                waveDrawable!!.level = heightLevel.toInt()
                milliTimePassed += delayMilliSeconds
            }
        }
    }

    override fun end() {
        (context as DynamicActivity).hideScreenFab(false)
        clearVars()
        context.isTimerRunning = false
        context.showMainFab(false)
    }

    override fun restart() {
        (context as DynamicActivity).hideScreenFab(false)
        clearVars()
        waveDrawable = CorocWaveDrawable(context, colorResSrc)
        imageView.setImageDrawable(waveDrawable)
        waveDrawable!!.level = 0
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
        (context as DynamicActivity).hideScreenFab(false)
        //isRunning = false
        showFab = false
        blockRun = false
        useOverTime = true
        waveDrawable = CorocWaveDrawable(context, R.drawable.gradient_firewatch)
        imageView.setImageDrawable(waveDrawable)
//        waveDrawable!!.level = 5000
//        timerView.visibility = View.VISIBLE
//        timerView.setTextColor(getColor(context, R.color.colorYellow))
        toggleTimer()
    }

    override fun refresh() {
        presetName = DynamicActivity.currentPreset.name.toString()
        givenSeconds = DynamicActivity.currentPreset.givenTime
        levelVariation = CorocUtil.getLevelVariation(givenSeconds, delayMilliSeconds)
        clearVars()
    }
}