package com.example.myapplication

import android.content.Context
import android.graphics.Point
import androidx.core.graphics.ColorUtils
import android.view.WindowManager
import android.widget.Toast
import kotlin.math.abs


/**
 * 자주 사용되는 메소드들을 따로 추출해낸 유틸리티 클래스입니다.
 *
 */
class CorocUtil {
    companion object {

        /**
         * 디바이스의 필셀크기를 가져옵니다.
         * match_parent 없이 bitmap 크기를 지정할  사용됩니다.때
         *
         * @return first는 x값이고 second는 y값입니다.
         */
        fun getDevicePoint(windowManager : WindowManager): Pair<Int, Int> {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val width = size.x
            val height = size.y
            return Pair(width, height)
        }

        /**
         * 타이머의 시작과 종료를 토스트로 표시하는 메서드입니다.
         * 각각의 타이머에서 호출됩니다.
         *
         * @param[timerStart] 참일경우 시작을 출력하고 거짓일 경우 종료를 출력합니다.
         */
        fun timerToast(context: Context, timerStart: Boolean){
            if (timerStart) {
                Toast.makeText(context, "타이머 시작", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "타이머 종료", Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * 주어진 밀리세컨드, 시간, 크기에 따라서 변동값을 반환합니다.
         * 밀리세컨드 간격으로 변동할 경우 정확히 주어진 시간에 maxLevel에 도달합니다.
         *
         * @param[totalSeconds] 총시간을 의미합니다. 총시간동안 maxLevel만큼 변화합니다.
         * @param[delayMilliSeconds] 변동간격을 의미합니다.
         * @param[maxLevel] 변동의 범위를 결정합니다.
         * @return 가공된 변동값입니다.
         * @sample getLevelVariation(30, 33, 10000)
         */
        fun getLevelVariation(totalSeconds: Int, delayMilliSeconds: Int, maxLevel: Double = 10000.0): Double {
            return (maxLevel / totalSeconds.toDouble()) / 1000.0 * delayMilliSeconds.toDouble()
        }

        /**
         * 시간초를 받아서 HH:mm:ss 포맷 또는 HH시mm분ss초 포맷으로 반환합니다.
         * 축약을 원하면 00시 00초를 제외합니다.
         *
         * @param[givenSeconds] 변환할 시간초입니다.
         * @param[hangul] 포맷을 한글로 출력할지 여부를 결정합니다.
         * @param[hangulAbbreviation] 한글 출력시 축약할 지 여부를 결정합니다.
         * @return 가공된 시간 포맷 문자열입니다.
         */
        fun timeToHMSFormat(givenSeconds: Int?, hangul: Boolean = false, hangulAbbreviation: Boolean = false) : String {
            if(givenSeconds == null) {
                return "없음"
            }

            val hours = abs(givenSeconds / (60*60))
            var seconds = abs(givenSeconds % (60*60))
            val minutes = abs(seconds / 60)
            seconds %= 60

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
        /**
         * 두 색상 값과 비율을 입력받아 비율에 따라 섞인 색상을 반환합니다.
         *
         * @param[startColor] 시작 색상입니다.
         * @param[endColor] 끝 색상입니다.
         * @param[ratio] 색상의 조합 비율을 의미합니다.
         * @param[reverseRatio] ratio을 역전시켜 적용할 지 여부를 의미합니다.
         * @return 시작 색상과 끝 색상이 주어진 비율에 따라 섞인 색상값입니다.
         * @sample getBlendedColor(startColorId, endColorId, 0.5f, reversedRatio = false)
         */
        fun getBlendedColor(startColor: Int, endColor: Int, ratio: Float, reverseRatio: Boolean = false) : Int {
            var newRatio = ratio
            if(reverseRatio) newRatio = 1 - ratio

            if (newRatio == 0f) {
                return startColor
            } else if (newRatio == 1f) {
                return endColor
            }
            return ColorUtils.blendARGB(startColor, endColor, newRatio)
        }

        /**
         * Boolean값을 한글로 표시합니다.
         *
         * @param[boolean] 변환할 Boolean값입니다.
         * @param[statSpecific] 통계에서 사용할 특정 문자열로 변환할지를 결정합니다.
         * @return 변환된 문자열입니다.
         */
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