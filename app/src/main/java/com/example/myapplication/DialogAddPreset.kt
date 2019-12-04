package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.add_preset_dialog.*
import kotlinx.android.synthetic.main.timer_dialog.cancelButton
import kotlinx.android.synthetic.main.timer_dialog.okButton

/**
 * 프리셋을 더하는 다이얼로그관련 클래스입니다.
 * 직접 다이얼로그를 상속하지 않고 다이얼로그를 만들어 설정하고 표시하는 클래스입니다.
 *
 * @param[context] 다이얼로그를 표시할 컨텍스트입니다.
 */
class DialogAddPreset(val context: Context) {

    /**
     * 다이얼로그를 표시하고 onClickListner를 할당합니다.
     * 필요한 서식을 채우지 않으면 등록을 거부합니다.
     */
    fun showDialog() {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.add_preset_dialog)
        dialog.show()

        /**
         * OK 버튼을 누르면 다이얼로그의 정보에 따라 프리셋을 더합니다.
         * 서식을 맞추지 않으면 등록이 거부됩니다.
         */
        dialog.okButton.setOnClickListener {
            if (dialog.editPresetName.text.isNotEmpty()){
                if (!TextUtils.isEmpty(dialog.editPresetHour.text) &&
                        !TextUtils.isEmpty(dialog.editPresetMinute.text) &&
                        !TextUtils.isEmpty(dialog.editPresetSecond.text)) {

                    val name = dialog.editPresetName.text.toString()
                    val time = dialog.editPresetHour.text.toString().toInt() * 3600 + dialog.editPresetMinute.text.toString().toInt() * 60 + dialog.editPresetSecond.text.toString().toInt()
                    // Add to Database
                    (context as ActivityStatistics).addPreset(Stat(name, time, 0,0,0,0, mutableListOf()))
                    StatManager.addPreset(name, time)


                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "서식을 전부 채워주십시오.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "서식을 전부 채워주십시오.", Toast.LENGTH_SHORT).show()
            }
        }
        /**
         * 취소 버튼을 누르면 다이얼로그를 종료합니다.
         */
        dialog.cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }
}