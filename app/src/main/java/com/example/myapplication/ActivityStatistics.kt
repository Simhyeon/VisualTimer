package com.example.myapplication

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_statistics.*


class ActivityStatistics: AppCompatActivity() {

    lateinit var mAdapter: DataAdapter
    lateinit var swipeController: SwipeController
    var presetChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        setPlayersDataAdapter()
        setupRecyclerView()

        fab_add_preset.bringToFront()
        fab_add_preset.setOnClickListener {
            showAddPresetDialog()
        }
        presetChanged = false
    }

    override fun onBackPressed() {
        val intent2 = Intent()
        intent2.putExtra("presetChanged", presetChanged)
        setResult(RESULT_OK, intent2)
        finish()
    }

    fun updatePreset(item: Stat) {
        mAdapter.statList.add(0, item)
        mAdapter.notifyItemInserted(0)
        mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount)
    }

    private fun showAddPresetDialog() {
        val dialog = DialogAddPreset(this)

        dialog.showDialog()
    }
    private fun setPlayersDataAdapter() {
        mAdapter = DataAdapter(StatManager.getStatList())
    }
    private fun setupRecyclerView() {
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = mAdapter
        swipeController = SwipeController(object : SwipeControllerActions() {
            override fun onLeftClicked(position: Int) {

                Snackbar.make(statRootView, "${mAdapter.statList[position].name}를 사용합니다.", Snackbar.LENGTH_SHORT).show()
                DynamicActivity.currentPreset = DynamicActivity.Preset(mAdapter.statList[position].name, mAdapter.statList[position].givenTime)
                presetChanged = true
            }

            override fun onRightClicked(position: Int) {
                if(DynamicActivity.currentPreset.name == mAdapter.statList[position].name) {
                    Snackbar.make(statRootView, "프리셋이 초기화됩니다.", Snackbar.LENGTH_SHORT).show()
                    DynamicActivity.currentPreset = DynamicActivity.Preset("", 600)
                }
                StatManager.delete(mAdapter.statList[position].name)
                mAdapter.statList.removeAt(position)
                mAdapter.notifyItemRemoved(position)
                mAdapter.notifyItemRangeChanged(position, mAdapter.itemCount)
                presetChanged = true
            }
        })
        val itemTouchhelper = ItemTouchHelper(swipeController)
        itemTouchhelper.attachToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(object : ItemDecoration() {
            override fun onDraw(c: Canvas, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
                swipeController.onDraw(c)
            }
        })
    }
}
