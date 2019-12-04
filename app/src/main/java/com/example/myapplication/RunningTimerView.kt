package com.example.myapplication

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat.getColor
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.*

/**
 * 말 타이머의  이미지 클래스
 *
 * @param[context] 타이머의 컨텍스트
 * @param[runningView] 말의 이미지 뷰
 * @param[timerView] 시간을 표현하는 텍스트 뷰열
 * @param[imageArray] 말의 애니메이션 이미지 세트 배열
 * @param[animationDelay] 애니메이션 이미지 변화 간격
 * @param[presetName] 초기 프리셋 이름
 * @param[givenSeconds] 초기 프리셋 시간
 * @param[startColorRes] 타이머의 시작 색상
 * @param[endColorRes] 타이머의 끝 색상
 */
class RunningTimerView(val context: Context, val runningView: ImageView, val timerView: TextView, val imageArray : Array<Int>, var animationDelay: Int, var presetName: String, var givenSeconds: Int, val startColorRes : Int, val endColorRes: Int) : TimerResult{

    private var runningJob: Job? = null
    private var timerJob: Job? = null
    private var vibrator: Vibrator? = null
    private var blockRun = false
    private var showFab = false
    private var duration = givenSeconds
    private var timeLeft = givenSeconds
    private var counter = 0

    private var useOverTime = false
    private val animationMultiplier: Double
        get() {
            return if (timeLeft != 0) {
                0.5 + 0.5 * (timeLeft.toFloat() / givenSeconds.toFloat())
            } else {
                0.5
            }
        }
    // 받은 변수가 조건에 맞는지를 검사합니다. 딜레이와 타이머 시간은 양의 정수여야 합니다.
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

        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * 변수를 초기화하고 코루틴 작업을 종료하는 함수입니다
     */
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
        // 말의 이미지를 딜레이시간마다 변경하는 코루틴
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
        // 시간 텍스트의 값을 변경하는 코루틴
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            var blendedColor: Int
            while(timeLeft > 0 && blockRun) {
                delay(1000) // Wait for 1 second
                timeLeft -=1
                timerView.text = CorocUtil.timeToHMSFormat(timeLeft)

                blendedColor = CorocUtil.getBlendedColor(
                    getColor(context, startColorRes),
                    getColor(context, endColorRes),
              timeLeft.toFloat() / duration,
                    reverseRatio = true
                )

                runningView.setColorFilter(blendedColor)
                timerView.setTextColor(blendedColor)
            }
            CorocUtil.timerToast(context, timerStart = false)
            showFab = true
            runningJob?.cancel()
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
        if (blockRun) {
            runningJob?.cancel()
            timerJob?.cancel()
            if(!showFab) {
                CorocUtil.timerToast(context, timerStart = false)
                showFab = true
                (context as MainActivity).showScreenFab(animation = true)
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
                delay((animationDelay * 0.7f).toLong())
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
        timerView.text = CorocUtil.timeToHMSFormat(givenSeconds)
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
        StatManager.update(Result(presetName, useOverTime, givenSeconds, givenSeconds - timeLeft))
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
        // set Different colo for overtime
        val overTimeColor = getColor(context, R.color.neonRed)

        runningView.setColorFilter(overTimeColor)
        timerView.setTextColor(overTimeColor)
        toggleTimer()
    }

    /**
     * 타이머를 업데이트 합니다. 프리셋이 변경될 경우, 지역 변수인 프리셋 이름과 총시간을 변경합니다.
     */
    override fun refresh(init: Boolean){
        presetName = MainActivity.currentPreset.name.toString()
        givenSeconds = MainActivity.currentPreset.givenTime
        duration = givenSeconds
        timeLeft = givenSeconds
        if (!init) timerView.text = CorocUtil.timeToHMSFormat(givenSeconds)
    }
}