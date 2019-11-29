package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.widget.AppCompatRadioButton
import android.text.InputType
import android.text.TextUtils
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.Toast
import kotlinx.android.synthetic.main.timer_dialog.*


class DialogTimerConfig(private val mycontext: Context): Dialog(mycontext) {
    var checkedRadio: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (mycontext as DynamicActivity).supportActionBar?.hide()
        mycontext.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.timer_dialog)

        // Disable timer input
        editTimerHour.isEnabled= false
        editTimerMinute.isEnabled= false
        editTimerSecond.isEnabled= false
        editTimerHour.inputType= InputType.TYPE_NULL
        editTimerMinute.inputType= InputType.TYPE_NULL
        editTimerSecond.inputType= InputType.TYPE_NULL

        timerTypeRadio.setOnCheckedChangeListener { radioGroup, id ->
            checkedRadio = radioGroup.findViewById<RadioButton>(id).text.toString()
        }

        useDirectTime.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                editTimerHour.isEnabled= true
                editTimerMinute.isEnabled= true
                editTimerSecond.isEnabled= true
                editTimerHour.inputType= InputType.TYPE_CLASS_NUMBER
                editTimerMinute.inputType= InputType.TYPE_CLASS_NUMBER
                editTimerSecond.inputType= InputType.TYPE_CLASS_NUMBER
            } else {
                editTimerHour.isEnabled= false
                editTimerMinute.isEnabled= false
                editTimerSecond.isEnabled= false
                editTimerHour.inputType= InputType.TYPE_NULL
                editTimerMinute.inputType= InputType.TYPE_NULL
                editTimerSecond.inputType= InputType.TYPE_NULL
            }
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
        okButton.setOnClickListener {

            //Change Timer
            mycontext.changeTimer(checkedRadio)
            // Set anonymous preset if user want
            if (useDirectTime.isChecked) {
                if (TextUtils.isEmpty(editTimerHour.text) ||
                        TextUtils.isEmpty(editTimerMinute.text) ||
                        TextUtils.isEmpty(editTimerSecond.text)) {
                    Toast.makeText(mycontext, "시간을 전부 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else {
                    val time = editTimerHour.text.toString().toInt() * 3600 + editTimerMinute.text.toString().toInt() * 60 + editTimerSecond.text.toString().toInt()
                    DynamicActivity.currentPreset = DynamicActivity.Preset("", time)
                    mycontext.refreshTimer()
                }
            }
            dismiss()
        }
    }
}

