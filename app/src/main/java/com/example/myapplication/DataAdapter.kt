package com.example.myapplication

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.stat_row.view.*

/**
 * 통계뷰에 활용될 리사이클러뷰 어댑터입니다.
 *
 * @param[statList] DB에서 불러온 통계 리스트입니다.
 */
class DataAdapter(var statList: MutableList<Stat>) : RecyclerView.Adapter<DataAdapter.StatViewHolder>() {

    class StatViewHolder(view: View) : ViewHolder(view)
    private var resulTimeTypeArray: Array<TextView> = arrayOf()
    private var resulTimeTextArray: Array<TextView> = arrayOf()

    /**
     * 뷰홀더를 생성할 때 불리는 메서드입니다.
     */
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): StatViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.stat_row, parent, false)
        return StatViewHolder(itemView)
    }

    /**
     * 리사이클러 뷰 아이템의 개수를 표시합니다.
     */
    override fun getItemCount(): Int = statList.size

    /**
     * 뷰홀더에 바인드 되었을 때 해당하는 카드뷰에 맞춰서 적절한 정보들을 텍스트뷰에 할당합니다.
     */
    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        resulTimeTypeArray = arrayOf(holder.itemView.resultType1, holder.itemView.resultType2, holder.itemView.resultType3)
        resulTimeTextArray = arrayOf(holder.itemView.resultTime1, holder.itemView.resultTime2, holder.itemView.resultTime3)
        val stat: Stat = statList[position]
        holder.itemView.presetName.text = stat.name
        holder.itemView.presetTime.text = CorocUtil.timeToHMSFormat(stat.givenTime, hangul = true, hangulAbbreviation = true)
        holder.itemView.timerCount.text = stat.totalCount.toString()
        holder.itemView.normalAverage.text = CorocUtil.timeToHMSFormat(stat.normalAverage.toInt(), hangul = true, hangulAbbreviation = true)
        holder.itemView.overAverage.text = CorocUtil.timeToHMSFormat(stat.overAverage.toInt(), hangul = true, hangulAbbreviation = true)

        for( x in 0..2) {
            resulTimeTypeArray[x].text = "없음"
            resulTimeTextArray[x].text = "없음"
        }
        for( x in stat.getHistory(3).indices) {
            resulTimeTypeArray[x].text = CorocUtil.booleanToHangul(stat.getHistory(3)[x].isOverTime, statSpecific = true)
            resulTimeTextArray[x].text = CorocUtil.timeToHMSFormat(stat.getHistory(3)[x].statTime, hangul = true, hangulAbbreviation = true)
        }
    }
}