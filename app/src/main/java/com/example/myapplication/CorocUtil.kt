package com.example.myapplication

import android.content.Context
import android.graphics.Point
import androidx.core.graphics.ColorUtils
import android.view.WindowManager
import android.widget.Toast
import kotlin.math.abs


class CorocUtil {
    companion object {

        // 디바이스의 픽셀크기를 Pair로 반환 first가 x값 second가 y값임.
        fun getDevicePoint(windowManager : WindowManager): Pair<Int, Int> {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val width = size.x
            val height = size.y
            return Pair(width, height)
        }

        fun timerToast(context: Context, timerStart: Boolean){
            if (timerStart) {
                Toast.makeText(context, "타이머 시작", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "타이머 종료", Toast.LENGTH_SHORT).show()
            }
        }

        /*
        * 딜레이밀리세컨드 마다 어느정도의 level을 감소시켜야 하는가를 리턴함.
        * */
        fun getLevelVariation(totalSeconds: Int, delayMilliSeconds: Int, maxLevel: Double = 10000.0): Double {
            return (maxLevel / totalSeconds.toDouble()) / 1000.0 * delayMilliSeconds.toDouble()
        }

        // 시간초를 받아서 디지털 시계 시간 문자열로 반환하는 함수 00:00의 형태로 반환.
        fun timeToHMSFormat(givenSeconds: Int?, hangul: Boolean = false, hangulAbbreviation: Boolean = false) : String {
            if(givenSeconds == null) {
                return "없음"
            }

            val hours = abs(givenSeconds / (60*60))
            var seconds = abs(givenSeconds % (60*60))
            val minutes = abs(seconds / 60)
            seconds %= 60

            if(hours > 24) {
                throw Exception("Timer limit is by 24 hours")
            }

            var formatter = ""
            for (i in hours.toString().length .. 1) {
                formatter += 0
            }
            formatter += "$hours"
            formatter += if (hangul) {"시"} else {":"}
            for (i in minutes.toString().length .. 1) {
                formatter += 0
            }
            formatter += "$minutes"
            formatter += if (hangul) {"분"} else {":"}
            for (i in seconds.toString().length .. 1) {
                formatter += 0
            }
            formatter += "$seconds"
            if (hangul) {formatter +="초"}

            if (hangulAbbreviation) {
                val reg1 = Regex("00시")
                val reg2 = Regex("00분")
                val reg3 = Regex("00초")
                formatter = reg1.replace(formatter, "")
                formatter = reg2.replace(formatter, "")
                formatter = reg3.replace(formatter, "")
                if(formatter.isEmpty()) {
                    formatter += "0초"
                }
            }

            return formatter
        }

        // 시작 색상과 끝 색상 사이에서 비율에 따라서 블렌드된 색상값 (정수)를 반환
        fun getBlendedColor(startColor: Int, endColor: Int, ratio: Float, reverseRatio: Boolean = false) : Int {

            var newRatio = ratio
            if(reverseRatio) newRatio = 1 - ratio

            if (newRatio == 0f) {
                return startColor
            } else if (newRatio == 1f) { // Should not be reachable
                return endColor
            }
            return ColorUtils.blendARGB(startColor, endColor, newRatio)
        }

        fun booleanToHangul(boolean: Boolean, statSpecific: Boolean = false): String{
            if (statSpecific) {
                return if(!boolean) {
                    "시간 내 완료"
                } else {
                    "시간 초과"
                }
            }
            return if (boolean) {
                "참"
            } else {
                "거짓"
            }
        }
    }
}