package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.android.synthetic.main.dynamic_main.*
import kotlinx.android.synthetic.main.extra_license.view.*

// 주석은 공식 KDoc 표준에 따라 작성되었습니다. JavaDoc의 코틀린 버전입니다.

/**
 * 메인액티비티로서 어플리케이션 실행시 최초로 실행됩니다.
 * 타이머 설정 다이얼로그, 프리셋 설정, 크레디트를 호출하는 Floating Action Menu를 관리하고
 * Fragment에 할당될 타이머의 종류와 업데이트를 관리합니다.
 */
class MainActivity: AppCompatActivity() {

    companion object {
        lateinit var currentPreset: Preset
        lateinit var currentTimer: String
        var useVibrator = false
    }
    lateinit var pref : SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    var isTimerRunning = false
    var controller: TimerController? = null

    /**
     * DB를 관리하는 StatManager에서 데이터를 불러오고
     * Preference에 저장된 프리셋과 타이머 종류 정보를 받아옵니다.
     * 불러온 프리셋과 타이머 종류에 따라서 불러올 프래그먼트를 결정하고 시간을 수정합니다.
     * Floating action button에 onClickListener를 할당합니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatManager.init(this)

        pref = this.getPreferences(0)
        currentPreset = Preset(pref.getString("Name", ""), pref.getInt("GivenTime", 300))
        currentTimer = pref.getString("Timer", "")
        Log.d("LOGLOG", "$currentTimer")
        setContentView(R.layout.dynamic_main)

        when (currentTimer) {
            getString(R.string.runningTimer) -> supportFragmentManager.beginTransaction().add(R.id.fragmentView, FragmentRunning()).commit()
            getString(R.string.waveTimer) -> supportFragmentManager.beginTransaction().add(R.id.fragmentView, FragmentWave()).commit()
            getString(R.string.sectorTimer) -> supportFragmentManager.beginTransaction().add(R.id.fragmentView, FragmentSector()).commit()
            getString(R.string.ringTimer) -> supportFragmentManager.beginTransaction().add(R.id.fragmentView, FragmentRing()).commit()
            else -> supportFragmentManager.beginTransaction().add(R.id.fragmentView, FragmentRunning()).commit()
        }

        //기타 라이선스 정보를 출력합니다.
        menu_extra_license.setOnClickListener {
            showExtraLicense()
        }

        // 플러그인에서 지원한느 메이븐 라이선스 액티비티를 출력합니다.
        menu_maven_oss.setOnClickListener {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }
        // 타이머 설정 다이얼로그를 출력합니다.
        menu_item_timer.setOnClickListener {
            showTimerSetting()
        }
        // 프리셋 설정 액티비티를 출력합니다.
        menu_item_list.setOnClickListener {
            val intent = Intent(this, ActivityStatistics::class.java)
            startActivityForResult(intent, 1)
        }
        refreshFloatingMenu()
        refreshTimer(init = true)
    }

    /**
     * Floating action menu의 내용을 현재 표시되는 타이머의 TimerController 인터페이스와 연결합니다.
     * 프래그먼트가 바뀌어도 동일한 메소드를 통해서 각각 다른 타이머의 함수를 호출할 수 있게 됩니다.
     */
    fun refreshFloatingMenu() {

        mainFab.bringToFront()
        screenFab.bringToFront()
        hideScreenFab(animation = false)

        supportFragmentManager.executePendingTransactions()
        controller = supportFragmentManager.findFragmentById(R.id.fragmentView) as TimerController

        //타이머를 종료합니다.
        menu_end_timer.setOnClickListener {
            controller?.endTimer()
        }
        //타이머 결과를 저장합니다.
        menu_save_timer.setOnClickListener {
            controller?.saveTimer()
        }
        //초과 타이머를 시작합니다.
        menu_over_timer.setOnClickListener {
            controller?.startOverTimer()
        }
        //타이머를 재시작합니다.
        menu_restart_timer.setOnClickListener {
            controller?.restartTimer()
        }
    }

    /**
     * 설정이 바뀌었을 경우 현재 표시되는 타이머에 refreshTimer()를 호출하여 정보를 업데이트합니다.
     */
    fun refreshTimer(init: Boolean) {
        Log.d("LOGLOG", "Refresh tiemer Called")
        controller = supportFragmentManager.findFragmentById(R.id.fragmentView) as TimerController
        controller?.refreshTimer(init)
        presetText.text = currentPreset.name
        if (presetText.text.isEmpty()) {
            presetText.text = "프리셋없음"
        }
    }


    /**
     * 타이머 설정, 크레디트, 프리셋 설정과 관련한 Main Floating actino menu를 숨깁니다.
     * 버튼이 펼쳐져 있다면 닫습니다.
     */
    fun hideMainFab(animation: Boolean){
        presetText.visibility = View.INVISIBLE
        mainFab.hideMenu(animation)
        mainFab.close(false)
    }
    /**
     * Main Floating actino menu를 보이게 합니다.
     */
    fun showMainFab(animation: Boolean){
        presetText.visibility = View.VISIBLE
        mainFab.showMenu(animation)
    }
    /**
     * 타이머 종료, 타이머 저장, 타이머 재시작 ,초과 타이머를 관리하는 Screen Fab을 숨깁니다.
     */
    fun hideScreenFab(animation: Boolean){
        screenFab.hideMenu(animation)
        screenFab.close(false)
    }
    /**
     * Screen Fab을 보입니다.
     */
    fun showScreenFab(animation: Boolean){
        screenFab.showMenu(animation)
    }

    /**
     * 초과 타이머의 활성화 여부를 결정합니다.
     *
     * @param[value] 초과 타이머의 활성화를 결정하는 인자입니다.
     */
    fun overButtonEnabled(value: Boolean){
        menu_over_timer.isEnabled = value
    }

    //Dialogue

    /**
     * 타이머 설정 다이얼로그를 호출합니다.
     */
    private fun showTimerSetting() {
        val dialog = DialogTimerConfig(this)
        dialog.show()
    }

    /**
     * 구글 플러그인이 지원하지 않는 라이선스를 표시하는 다이얼로그를 호출합니다.
     *
     * raw resource 폴더에 저장되어 있는 텍스트 파일을 읽어 표시합니다.
     */
    private fun showExtraLicense() {
        val licenseText = resources.openRawResource(R.raw.extralicense).use {
            return@use it.readBytes().toString(Charsets.UTF_8)
        }
        val inflater = LayoutInflater.from(this)
        val view: View = inflater.inflate(R.layout.extra_license, null)

        view.licenseText.text = licenseText
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialog.setView(view)
        val alert: AlertDialog = alertDialog.create()
        alert.show()
    }

    /**
     * 표시될 타이머를 변경합니다.
     *
     * 프래그먼트를 변경하고 프레퍼런스로 저장될 currentTimer에 값을 할당합니다.
     *
     * @param[TimerName] 변경할 타이머의 이름입니다.
     */
    fun changeTimer(TimerName: String) {
        when(TimerName) {
            getString(R.string.waveTimer) -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentView, FragmentWave()).commit()
                currentTimer = getString(R.string.waveTimer)
                Log.d("LOGLOG", "$currentTimer")
            }
            getString(R.string.runningTimer) -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentView, FragmentRunning()).commit()
                currentTimer = getString(R.string.runningTimer)
                Log.d("LOGLOG", "$currentTimer")
            }
            getString(R.string.sectorTimer) -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentView, FragmentSector()).commit()
                currentTimer = getString(R.string.sectorTimer)
                Log.d("LOGLOG", "$currentTimer")
            }
            getString(R.string.ringTimer) -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentView, FragmentRing()).commit()
                currentTimer = getString(R.string.sectorTimer)
                Log.d("LOGLOG", "$currentTimer")
            }
        }

        refreshFloatingMenu()
        refreshTimer(init = false)
    }

    /**
     * 프리셋 설정 액티비티에서 설정한 프리셋 변경 여부를 인텐트에서 받아오고
     * 변경되었다면 타이머를 업데이트 합니다.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data!= null) {
            if (data.getBooleanExtra("presetChanged", false)) {
                refreshTimer(init = false)
            }
        }
    }


    /**
     * 어떤 경우에도 상태창과 액션바가 보이지 않게 합니다.
     */
    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //refreshTimer()
    }

    /**
     * 프레퍼런스에 프리셋 정보와 타이머 정보를 저장합니다.
     */
    override fun onPause() {
        super.onPause()
        editor = pref.edit()
        editor.putString("Name", currentPreset.name)
        editor.putInt("GivenTime", currentPreset.givenTime)
        editor.putString("Timer", currentTimer)
        editor.commit()
    }

    /**
     * 프레퍼런스에 활용할 간단한 클래스입니다.
     */
    class Preset(val name: String?, val givenTime: Int)
}