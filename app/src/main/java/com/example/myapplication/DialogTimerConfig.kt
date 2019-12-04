package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.Toast
import kotlinx.android.synthetic.main.timer_dialog.*

/**
 * 타이머 설정을 입력하는 다이얼로그입니다.
 * @param[mycontext] 다이얼로그가 표시될 컨텍스트이며 메인 액티비티여야 합니다.
 */
class DialogTimerConfig(private val mycontext: Context): Dialog(mycontext) {
    var checkedRadio: String = ""
    var useVib = false

    /**
     * 타이머 설정 다이얼로그의 onclickListner를 설정합니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (mycontext as MainActivity).supportActionBar?.hide()
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

        /**
         * 체크박스 설정 여부에 따라 시간 설정 텍스트의 활성화 여부를 결정합니다.
         */
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

        /**
         * 타이머 진동 여부를 설정합니다.
         */
        useVibrate.setOnCheckedChangeListener {_, isChecked ->
            useVib = isChecked
        }

        /**
         * 다이얼로그를 취소합니다.
         */
        cancelButton.setOnClickListener {
            dismiss()
        }

        /**
         * 입력받은 타이머 정보에 따라 타이머를 업데이트합니다.
         * 서식이 맞춰지지 않았다면 거부됩니다.
         */
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
                    MainActivity.currentPreset = MainActivity.Preset("", time)
                    MainActivity.useVibrator = useVib
                    mycontext.refreshTimer(init = false)
                }
            }
            dismiss()
        }
    }
}

