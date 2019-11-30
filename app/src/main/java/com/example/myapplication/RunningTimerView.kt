package com.example.myapplication

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat.getColor
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.*

class RunningTimerView(val context: Context, val rootView: View, val runningView: ImageView, val timerView: TextView, val imageArray : Array<Int>, var animationDelay: Int, var presetName: String, var givenSeconds: Int, val startColorRes : Int, val endColorRes: Int) : TimerResult{

    private var runningJob: Job? = null
    private var timerJob: Job? = null
    var blockRun = false
    var showFab = false
    var duration = givenSeconds
    var timeLeft = givenSeconds
    var counter = 0

    private var useOverTime = false
    private val animationMultiplier: Double
        get() {
            return if (timeLeft != 0) {
                0.5 + 0.5 * (timeLeft.toFloat() / givenSeconds.toFloat())
            } else {
                0.5
            }
        }
    init {
        when{
            animationDelay < 0 -> {
                throw IllegalArgumentException("animationDelay should be positive integer")
            }
            givenSeconds < 0 -> {
                throw IllegalArgumentException("givenSeconds should be positive integer")
            }
            imageArray.isEmpty() -> {
                throw IllegalArgumentException("imageArray's length should be positive")
            }
        }
    }

    fun clearVars() {
        runningJob?.cancel()
        timerJob?.cancel()
        runningView.setImageResource(imageArray[0])
        timerView.text = CorocUtil.timeToHMSFormat(givenSeconds)
        blockRun = false
        showFab = false
        duration = givenSeconds
        timeLeft = givenSeconds
        counter = 0
        useOverTime = false

        runningView.setColorFilter(getColor(context, R.color.colorYellow))
        timerView.setTextColor(getColor(context, R.color.colorYellow))
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
        if (useOverTime) {
            overTimer()
            return
        }
        if (blockRun) {
            CorocUtil.timerToast(context, timerStart = false)
            runningJob?.cancel()
            timerJob?.cancel()
            showFab = true
            context.showScreenFab(animation = true)
            context.overButtonEnabled(false)
            return
        } else {
            blockRun = true
        }

        CorocUtil.timerToast(context, timerStart = true)
        runningJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                runningView.setImageResource(imageArray[counter])
                //imageNumber.text = counter.toString() + ":00"
                counter += 1
                if (counter > imageArray.size - 1) {
                    counter = 0
                }
                delay((animationDelay * animationMultiplier).toLong())
            }
        }
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            var blendedColor: Int
            while(timeLeft > 0 && blockRun) {
                delay(1000) // Wait for 1 second
                timeLeft -=1
                timerView.text = CorocUtil.timeToHMSFormat(timeLeft)

                blendedColor = CorocUtil.getBlendedColor(
                    getColor(context, startColorRes),
                    getColor(context, endColorRes),
                    timeLeft.toFloat() / duration
                )

                runningView.setColorFilter(blendedColor)
                timerView.setTextColor(blendedColor)
            }
            CorocUtil.timerToast(context, timerStart = false)
            showFab = true
            context.showScreenFab(animation = true)
            if (presetName.isNotEmpty()) {context.overButtonEnabled(true)}
            runningJob?.cancel()
        }
    }

    private fun overTimer() {
        if (showFab) {
            return
        }
        if (blockRun) {
            runningJob?.cancel()
            timerJob?.cancel()
            if(!showFab) {
                CorocUtil.timerToast(context, timerStart = false)
                showFab = true
                (context as DynamicActivity).showScreenFab(animation = true)
                context.overButtonEnabled(false)
            }
            return
        } else {
            blockRun = true
        }

        CorocUtil.timerToast(context, timerStart = true)
        runningJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                runningView.setImageResource(imageArray[counter])
                //imageNumber.text = counter.toString() + ":00"
                counter += 1
                if (counter > imageArray.size - 1) {
                    counter = 0
                }
                delay((animationDelay * 0.5f).toLong())
            }
        }
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while(blockRun) {
                delay(1000) // Wait for 1 second
                timeLeft -=1
                timerView.text = CorocUtil.timeToHMSFormat(timeLeft)
            }
        }
    }

    override fun end() {
        (context as DynamicActivity).hideScreenFab(false)
        clearVars()
        context.isTimerRunning = false
        context.showMainFab(false)
        val blendedColor = CorocUtil.getBlendedColor(
                getColor(context, startColorRes),
                getColor(context, endColorRes),
                timeLeft.toFloat() / duration
        )
        runningView.setColorFilter(blendedColor)
        timerView.setTextColor(blendedColor)
    }

    override fun restart() {
        (context as DynamicActivity).hideScreenFab(false)
        clearVars()
        timerView.text = CorocUtil.timeToHMSFormat(givenSeconds)
        val blendedColor = CorocUtil.getBlendedColor(
                getColor(context, startColorRes),
                getColor(context, endColorRes),
        timeLeft.toFloat() / duration
        )
        runningView.setColorFilter(blendedColor)
        timerView.setTextColor(blendedColor)
        toggleTimer()
    }

    override fun saveResult() {
        if (presetName == ""){
            Toast.makeText(context, "프리셋을 설정해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        StatManager.update(Result(presetName, useOverTime, givenSeconds, givenSeconds - timeLeft))
        end()
    }

    override fun overTime() {
        (context as DynamicActivity).hideScreenFab(false)
        //isRunning = false
        showFab = false
        blockRun = false
        useOverTime = true
        // set Different colo for overtime
        val overTimeColor = getColor(context, R.color.neonRed)

        runningView.setColorFilter(overTimeColor)
        timerView.setTextColor(overTimeColor)
        toggleTimer()
    }

    override fun refresh(){
        presetName = DynamicActivity.currentPreset.name.toString()
        givenSeconds = DynamicActivity.currentPreset.givenTime
        clearVars()
        timerView.text = CorocUtil.timeToHMSFormat(timeLeft)
    }
}