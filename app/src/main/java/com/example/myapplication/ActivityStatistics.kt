package com.example.myapplication

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_statistics.*

/**
 * 프리셋 설정 액티비티 클래스입니다.
 * 리사이클러뷰에 프리셋 설정/통계 카드뷰를 할당하고
 * 프리셋을 추가하는 Floating action button을 관리합니다.
 */
class ActivityStatistics: AppCompatActivity() {

    lateinit var adapter: DataAdapter
    lateinit var swipeController: SwipeController
    var presetChanged = false

    /**
     * 리사이클러뷰에 어댑터를 설정하고
     * Floating Action menu에 onclicklistener를 할당합니다.
     */
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

    /**
     * 뒤로 가기 버튼을 눌렀을 경우
     * result에 프리셋 변경 여부를 담아서 메인 액티비티로 반환합니다.
     */
    override fun onBackPressed() {
        val intent2 = Intent()
        intent2.putExtra("presetChanged", presetChanged)
        setResult(RESULT_OK, intent2)
        finish()
    }

    /**
     * 프리셋을 리사이클러 뷰에 더합니다.
     *
     * @param[item] Stat(통계) 형태로 표현된 프리셋입니다.
     */
    fun addPreset(item: Stat) {
        adapter.statList.add(0, item)
        adapter.notifyItemInserted(0)
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    /**
     * 프리셋 추가 다이얼로그를 호출합니다.
     */
    private fun showAddPresetDialog() {
        val dialog = DialogAddPreset(this)

        dialog.showDialog()
    }

    /**
     * 어댑터를 할당합니다.
     */
    private fun setPlayersDataAdapter() {
        adapter = DataAdapter(StatManager.getStatList())
    }

    /**
     * 리사이클러뷰를 설정합니다.
     * 카드뷰를 스와이프 할 경우의 액션을 설정합니다.
     * 우로 스와이프 할 경우에는 설정하기 버튼을 그리고
     * 좌로 스와이프 할 경우에는 지우기 버튼을 그립니다.
     *
     * 설정하기 버튼 클릭시에는 현재 프리셋이 업데이트 됩니다.
     * 지우기 버튼을 클릭시에는 리사이클러뷰에서 지워지며, DB에서도 지워집니다.
     */
    private fun setupRecyclerView() {
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        swipeController = SwipeController(object : SwipeControllerActions() {

            // 왼쪽 버튼을 누를 경우 현재 프리셋으로 설정합니다.
            override fun onLeftClicked(position: Int) {

                Snackbar.make(statRootView, "${adapter.statList[position].name}를 사용합니다.", Snackbar.LENGTH_SHORT).show()
                MainActivity.currentPreset = MainActivity.Preset(adapter.statList[position].name, adapter.statList[position].givenTime)
                presetChanged = true
            }

            // 오른쪽 버튼을 누를 경우 해당 프리셋을 삭제합니다.
            override fun onRightClicked(position: Int) {
                if(MainActivity.currentPreset.name == adapter.statList[position].name) {
                    Snackbar.make(statRootView, "프리셋이 초기화됩니다.", Snackbar.LENGTH_SHORT).show()
                    MainActivity.currentPreset = MainActivity.Preset("", 600)
                }
                StatManager.delete(adapter.statList[position].name)
                adapter.statList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, adapter.itemCount)
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
