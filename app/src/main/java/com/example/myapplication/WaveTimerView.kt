package com.example.myapplication

import android.content.Context
import android.graphics.PorterDuff
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import kotlinx.coroutines.*
import kotlin.math.abs

/**
 * 물결 타이머의  이미지 클래스
 *
 * @param[context] 타이머의 컨텍스트
 * @param[imageView] 물결의 이미지 뷰
 * @param[delayMilliSeconds] 물결 타이머의 상승 간격 (프레임)
 * @param[presetName] 초기 프리셋 이름
 * @param[givenSeconds] 초기 프리셋 시간
 */
class WaveTimerView(val context: Context, val imageView: ImageView, val delayMilliSeconds: Int, var presetName: String, var givenSeconds: Int) : TimerResult {

    var waveDrawable: CorocWaveDrawable? = null
        private set
    var vibrator: Vibrator? = null
    var colorResSrc:Int = 0

    private var heightLevel= 0.0
    private var levelVariation = 0.0
    private var blockRun = false
    private var showFab = false
    private var jobId: Job? = null
    private var milliTimePassed = 0

    private var useOverTime = false

    // 받은 변수가 조건에 맞는지를 검사합니다. 딜레이와 타이머 시간은 양의 정수여야 합니다.
    init {
        if (delayMilliSeconds < 1) {
            throw IllegalArgumentException("DelayMilliSeconds should be natural numbers")
        }
        if (givenSeconds < 1) {
            throw IllegalArgumentException("Given seoncds should be positive integer")
        }
        this.levelVariation = CorocUtil.getLevelVariation(givenSeconds, this.delayMilliSeconds)

        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * 변수를 초기화하고 코루틴 작업을 종료하는 함수입니다
     */
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

    /**
     * 타이머를 토글합니다. (시작, 종료)
     * 최초 터치할 경우에는 main floating aciton menu를 숨기고 코루틴을 시작합니다.
     * 다시 터치할 경우에는 screen floating action menu를 보이고 코루틴을 종료합니다.
     *
     * 코루틴은 딜레이마다 타이머 이미지와 남은 시간 텍스트를 업데이트합니다.
     * 남은 시간이 0이 될 경우에는 코루틴을 종료하고 Screen faloting action menu 보여줍니다.
     * 진동을 선택했을 경우에는 1.5초간 진동합니다.
     *
     * 만약 초과 타이머가 선택되었을 경우에는 OverTimer로 분기합니다.
     */
    fun toggleTimer() {
        if (!(context as MainActivity).isTimerRunning) {
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
        // 물결의 level(높이)를 변경하는 코루틴
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
            if (presetName.isNotEmpty()) context.overButtonEnabled(true)
            if (MainActivity.useVibrator) vibrator?.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    /**
     * 초과 타이머를 토글합니다. (시작, 종료)
     * Screen floating action button을 클릭했을 때 호출되며
     * 처음 토글시에는 새로운 코루틴을 시작하여 초과한 시간을 표시하고 타이머 이미지를 업데이트합니다.
     * 다시 토글시에는 코루틴을 종료하고 Screen floating action menu를 호출합니다.
     */
    private fun overTimer() {
        if (showFab) {
            return
        }
        if (blockRun){
            CorocUtil.timerToast(context, timerStart = false)
            showFab = true
            jobId?.cancel()
            (context as MainActivity).showScreenFab(animation = true)
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

    /**
     * 타이머를 종료합니다.
     * 타이머 색상을 원래대로 초기화하고 main FLoating action menu를 보입니다.
     */
    override fun end() {
        (context as MainActivity).hideScreenFab(false)
        clearVars()
        context.isTimerRunning = false
        context.showMainFab(false)
    }

    /**
     * 타이머를 재시작합니다.
     * 지역 변수와 코루틴을 초기화하고 타이머를 토글합니다.
     */
    override fun restart() {
        (context as MainActivity).hideScreenFab(false)
        clearVars()
        waveDrawable = CorocWaveDrawable(context, colorResSrc)
        imageView.setImageDrawable(waveDrawable)
        waveDrawable!!.level = 0
        toggleTimer()
    }

    /**
     * 타이머 결과를 저장합니다.
     * Result 객체를 생성하고 StatManager를 통해서 DB에 저장합니다.
     * 선택된 타이머가 없다면 저장하지 않습니다.
     */
    override fun saveResult() {
        if (presetName == ""){
            Toast.makeText(context, "프리셋을 설정해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        StatManager.update(Result(presetName, useOverTime, givenSeconds, milliTimePassed / 1000))
        end()
    }

    /**
     * 초과 타이머를 시작합니다.
     * 일부 변수를 초기화하고
     * 타이머를 토글합니다.
     */
    override fun overTime() {
        (context as MainActivity).hideScreenFab(false)
        //isRunning = false
        showFab = false
        blockRun = false
        useOverTime = true
        waveDrawable = CorocWaveDrawable(context, R.drawable.gradient_firewatch)
        imageView.setImageDrawable(waveDrawable)
        toggleTimer()
    }

    /**
     * 타이머를 업데이트 합니다. 프리셋이 변경될 경우, 지역 변수인 프리셋 이름과 총시간을 변경합니다.
     */
    override fun refresh(init: Boolean) {
        presetName = MainActivity.currentPreset.name.toString()
        givenSeconds = MainActivity.currentPreset.givenTime
        levelVariation = CorocUtil.getLevelVariation(givenSeconds, delayMilliSeconds)
    }
}