package com.example.myapplication

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.stat_row.view.*


class DataAdapter(var statList: MutableList<Stat>) : androidx.recyclerview.widget.RecyclerView.Adapter<DataAdapter.StatViewHolder>() {

    class StatViewHolder(view: View) : ViewHolder(view)
    private var resulTimeTypeArray: Array<TextView> = arrayOf()
    private var resulTimeTextArray: Array<TextView> = arrayOf()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): StatViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.stat_row, parent, false)
        return StatViewHolder(itemView)
    }

    override fun getItemCount(): Int = statList.size

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        resulTimeTypeArray = arrayOf(holder.itemView.resultType1, holder.itemView.resultType2, holder.itemView.resultType3)
        resulTimeTextArray = arrayOf(holder.itemView.resultTime1, holder.itemView.resultTime2, holder.itemView.resultTime3)
        val stat: Stat = statList[position]
        holder.itemView.presetName.text = stat.name
        holder.itemView.presetTime.text = CorocUtil.timeToHMSFormat(stat.givenTime, hangul = true, hangulAbbreviation = true)
        holder.itemView.timerCount.text = stat.totalCount.toString()
        holder.itemView.normalAverage.text = CorocUtil.timeToHMSFormat(stat.normalAverage.toInt(), hangul = true, hangulAbbreviation = true)
        holder.itemView.overAverage.text = CorocUtil.timeToHMSFormat(stat.overAverage.toInt(), hangul = true, hangulAbbreviation = true)
        for( x in stat.getHistory(3).indices) {
            resulTimeTypeArray[x].text = CorocUtil.booleanToHangul(stat.getHistory(3)[x].isOverTime, statSpecific = true)
            resulTimeTextArray[x].text = CorocUtil.timeToHMSFormat(stat.getHistory(3)[x].statTime, hangul = true, hangulAbbreviation = true)
        }
    }
}