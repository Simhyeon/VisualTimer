package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.add_preset_dialog.*
import kotlinx.android.synthetic.main.timer_dialog.cancelButton
import kotlinx.android.synthetic.main.timer_dialog.okButton

class DialogAddPreset(val context: Context) {

    fun showDialog() {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.add_preset_dialog)
        dialog.show()

        dialog.okButton.setOnClickListener {
            if (dialog.editPresetName.text.isNotEmpty()){
                if (!TextUtils.isEmpty(dialog.editPresetHour.text) &&
                        !TextUtils.isEmpty(dialog.editPresetMinute.text) &&
                        !TextUtils.isEmpty(dialog.editPresetSecond.text)) {

                    val name = dialog.editPresetName.text.toString()
                    val time = dialog.editPresetHour.text.toString().toInt() * 3600 + dialog.editPresetMinute.text.toString().toInt() * 60 + dialog.editPresetSecond.text.toString().toInt()
                    // Add to Database
                    (context as ActivityStatistics).updatePreset(Stat(name, time, 0,0,0,0, mutableListOf()))
                    StatManager.addPreset(name, time)


                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "서식을 전부 채워주십시오.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "서식을 전부 채워주십시오.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }
}