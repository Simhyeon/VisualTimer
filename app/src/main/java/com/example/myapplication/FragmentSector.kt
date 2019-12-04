package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_new_sector_revised.*
import kotlinx.coroutines.*

/**
 * 부채꼴 형태 타이머 클래스
 * 부채꼴 형태의 타이머 이미지를 직접 관리합니다.
 */
class FragmentSector: Fragment(), TimerController, TimerResult {

    private lateinit var myContext: Context
    private lateinit var mainActivity: MainActivity
    private var vibrator: Vibrator? = null
    private var showFab = false
    private var useOverTime = false
    private var blockRun = false
    private var jobId : Job? = null
    private var overJob: Job? = null

    private var fillLevel = 0.0
    private var levelVariation = 0.0
    private val delayMilliSeconds = 33
    private var milliTimePassed = 0

    private var presetName = ""
    private var givenSeconds = 120

    /**
     * 변수를 초기화하고 코루틴 작업을 종료하는 함수
     */
    private fun clearVars() {
        showFab = false
        useOverTime = false
        blockRun = false
        jobId?.cancel()
        overJob?.cancel()
        fillLevel = 0.0
        milliTimePassed = 0
        sector.percent = 0f
        sectorTimerText.text = CorocUtil.timeToHMSFormat(MainActivity.currentPreset.givenTime)
        sector.startAngle = 0f
    }

    // Fragment View에 부착되었을 때 불리는 함수, 컨텍스트를 저장함
    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
        mainActivity = context as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.activity_new_sector_revised,
                container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sectorTimerText.text = CorocUtil.timeToHMSFormat(MainActivity.currentPreset.givenTime)
        presetName = MainActivity.currentPreset.name.toString()
        givenSeconds = MainActivity.currentPreset.givenTime
        levelVariation = CorocUtil.getLevelVariation(givenSeconds, delayMilliSeconds)

        // 화면을 터치하면 타이머를 토글합니다.
        sectorRootView.setOnClickListener {
            toggleTimer()
        }

        vibrator = myContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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
            // overJob?.cancel()
            showFab = true
            mainActivity.showScreenFab(animation = true)
            mainActivity.overButtonEnabled(false)
            return
        } else { // blockRun == false
            blockRun = true
        }
        CorocUtil.timerToast(myContext, timerStart = true)
        // 부채꼴의 퍼센트를 변경하는 코루틴
        jobId = CoroutineScope(Dispatchers.Main).launch {
            while ((milliTimePassed < givenSeconds * 1000)) {
                delay(delayMilliSeconds.toLong())
                fillLevel += levelVariation
                sector.percent= (fillLevel / 100).toFloat()
                milliTimePassed += delayMilliSeconds
                sectorTimerText.text = CorocUtil.timeToHMSFormat( givenSeconds - milliTimePassed / 1000)
            }
            CorocUtil.timerToast(myContext, timerStart = false)
            milliTimePassed = givenSeconds * 1000
            showFab = true
            mainActivity.showScreenFab(animation = true)
            if (MainActivity.useVibrator) vibrator?.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE))
            if (presetName.isNotEmpty()) mainActivity.overButtonEnabled(true)
        }
    }

    /**
     * 초과 타이머를 토글합니다. (시작, 종료)
     * Screen floating action button을 클릭했을 때 호출되며
     * 처음 토글시에는 새로운 코루틴을 시작하여 초과한 시간을 표시하고 타이머 이미지를 업데이트합니다.
     * 다시 토글시에는 코루틴을 종료하고 Screen floating action menu를 호출합니다.
     */
    fun overTimer() {
        if (showFab) {
            return
        }
        if (blockRun){
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

        sector.fgColor = getColor(myContext, R.color.neonRed)
        background.fgColorStart = getColor(myContext, R.color.neonRed)
        background.fgColorEnd = getColor(myContext, R.color.neonRed)
        sectorTimerText.setTextColor(getColor(myContext, R.color.neonRed))

        CorocUtil.timerToast(myContext, timerStart = true)
        overJob = CoroutineScope(Dispatchers.Main).launch {
            sector.percent = 50f
            var angle = 0f
            val increament = CorocUtil.getLevelVariation(10, delayMilliSeconds, 360.0)
            while (blockRun) {
                delay(delayMilliSeconds.toLong())
                angle += increament.toFloat()
                sector.startAngle = angle
                if (angle > 360) {
                    angle -= 360
                }
            }
        }

        jobId =CoroutineScope(Dispatchers.Main).launch {
            while (blockRun) {
                delay(1000) // Wait for 1 second
                milliTimePassed += 1000
                sectorTimerText.text = CorocUtil.timeToHMSFormat(milliTimePassed / 1000 - givenSeconds)
            }
        }
    }

    // TimerController 인터페이스 함수입니다.
    // 인터페이스 주석을 참조
    override fun endTimer() { end() }
    override fun restartTimer() { restart() }
    override fun saveTimer() { saveResult() }
    override fun startOverTimer() { overTime() }
    override fun refreshTimer(init: Boolean) { refresh(init) }

    // TimerResult 인터페이스 함수
    /**
     * 타이머를 종료합니다.
     * 타이머 색상을 원래대로 초기화하고 main FLoating action menu를 보입니다.
     */
    override fun end() {
        mainActivity.hideScreenFab(false)
        clearVars()

        sector.fgColor = getColor(myContext, R.color.colorYellow)
        background.fgColorStart = getColor(myContext, R.color.colorYellow)
        background.fgColorEnd = getColor(myContext, R.color.colorYellow)
        sectorTimerText.setTextColor(getColor(myContext, R.color.colorYellow))

        mainActivity.isTimerRunning = false
        mainActivity.showMainFab(false)
    }

    /**
     * 타이머를 재시작합니다.
     * 지역 변수와 코루틴을 초기화하고 타이머를 토글합니다.
     */
    override fun restart() {
        mainActivity.hideScreenFab(false)
        clearVars()

        sector.fgColor = getColor(myContext, R.color.colorYellow)
        background.fgColorStart = getColor(myContext, R.color.colorYellow)
        background.fgColorEnd = getColor(myContext, R.color.colorYellow)
        sectorTimerText.setTextColor(getColor(myContext, R.color.colorYellow))

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
        Toast.makeText(context, "Saved result", Toast.LENGTH_SHORT).show()
        StatManager.update(Result(presetName, useOverTime, givenSeconds, milliTimePassed / 1000))
        end()
    }

    /**
     * 초과 타이머를 시작합니다.
     * 일부 변수를 초기화하고
     * 타이머를 토글합니다.
     */
    override fun overTime() {
        mainActivity.hideScreenFab(false)
        showFab = false
        blockRun = false
        useOverTime = true
        fillLevel = 0.0
        sector.percent = 0f
        toggleTimer()
    }

    /**
     * 타이머를 업데이트 합니다. 프리셋이 변경될 경우, 지역 변수인 프리셋 이름과 총시간을 변경합니다.
     */
    override fun refresh(init: Boolean) {
        presetName = MainActivity.currentPreset.name.toString()
        givenSeconds = MainActivity.currentPreset.givenTime
        levelVariation = CorocUtil.getLevelVariation(givenSeconds, delayMilliSeconds)
        if (!init) sectorTimerText.text = CorocUtil.timeToHMSFormat(MainActivity.currentPreset.givenTime)
    }
}