package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.android.synthetic.main.dynamic_main.*
import kotlinx.android.synthetic.main.extra_license.view.*
import java.io.InputStream


class DynamicActivity: AppCompatActivity() {

    companion object {
        lateinit var currentPreset: Preset
    }
    lateinit var pref : SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    var isTimerRunning = false
    var controller: TimerController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatManager.init(this)

        pref = this.getPreferences(0)
        currentPreset = Preset(pref.getString("Name", ""), pref.getInt("GivenTime", 300))

        setContentView(R.layout.dynamic_main)

        //currentPreset = readFromPreferences()
        supportFragmentManager.beginTransaction().add(R.id.fragmentView, FragmentRunning()).commit()

        menu_extra_license.setOnClickListener {
            showExtraLicense()
        }

        menu_maven_oss.setOnClickListener {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }
        menu_item_timer.setOnClickListener {
            showTimerSetting()
        }
        menu_item_list.setOnClickListener {
            val intent = Intent(this, ActivityStatistics::class.java)
            startActivity(intent)
        }

        refreshFloatingMenu()
    }

    fun refreshFloatingMenu() {

        mainFab.bringToFront()
        screenFab.bringToFront()
        hideScreenFab(animation = false)

        supportFragmentManager.executePendingTransactions()
        controller = supportFragmentManager.findFragmentById(R.id.fragmentView) as TimerController

        // Fabs onclick listenr
        menu_end_timer.setOnClickListener {
            controller?.endTimer()
        }
        menu_save_timer.setOnClickListener {
            controller?.saveTimer()
        }
        menu_over_timer.setOnClickListener {
            controller?.startOverTimer()
        }
        menu_restart_timer.setOnClickListener {
            controller?.restartTimer()
        }
    }

    fun refreshTimer() {
        controller = supportFragmentManager.findFragmentById(R.id.fragmentView) as TimerController
        controller?.refreshTimer()
        presetText.text = currentPreset.name
    }


    fun hideMainFab(animation: Boolean){
        presetText.visibility = View.INVISIBLE
        mainFab.hideMenu(animation)
    }
    fun showMainFab(animation: Boolean){
        presetText.visibility = View.VISIBLE
        mainFab.showMenu(animation)
    }
    fun hideScreenFab(animation: Boolean){
        screenFab.hideMenu(animation)
    }
    fun showScreenFab(animation: Boolean){
        screenFab.showMenu(animation)
    }
    fun overButtonEnabled(value: Boolean){
        menu_over_timer.isEnabled = value
    }

    //Dialogue

    private fun showTimerSetting() {
        val dialog = DialogTimerConfig(this)
        dialog.show()
    }

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

    fun changeTimer(fragmentName: String) {
        when(fragmentName) {
            getString(R.string.waveTimer) -> supportFragmentManager.beginTransaction().replace(R.id.fragmentView, FragmentWave()).commit()
            getString(R.string.runningTimer) -> supportFragmentManager.beginTransaction().replace(R.id.fragmentView, FragmentRunning()).commit()
            getString(R.string.sectorTimer) -> supportFragmentManager.beginTransaction().replace(R.id.fragmentView, FragmentSector()).commit()
            getString(R.string.ringTimer) -> supportFragmentManager.beginTransaction().replace(R.id.fragmentView, FragmentRing()).commit()
        }

        refreshFloatingMenu()
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        refreshTimer()
    }

    override fun onPause() {
        super.onPause()
        editor = pref.edit()
        editor.putString("Name", currentPreset.name)
        editor.putInt("GivenTime", currentPreset.givenTime)
        editor.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    class Preset(val name: String?, val givenTime: Int)
}