package com.u.marketapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.u.marketapp.utils.SharedPreferencesUtils
import kotlinx.android.synthetic.main.activity_filter.*

class FilterActivity : AppCompatActivity() {

    companion object {
        private val TAG = FilterActivity::class.java.simpleName
    }

    private lateinit var actionbar: ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        initView()
    }

    // 화면 초기화
    private fun initView() {
        setActionbar() // 액션바 설정
        setCategory()
    }

    // 액션바
    private fun setActionbar() {
        setSupportActionBar(toolbar)
        actionbar = supportActionBar!!
        actionbar.title = resources.getString(R.string.title_filter)
        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    private fun setCategory() {
        val list = SharedPreferencesUtils.instance.getStringArrayPref(this, "category")
        if (list.isNullOrEmpty()) {
            val array = arrayListOf<String>(*resources.getStringArray(R.array.array_category))
            SharedPreferencesUtils.instance.setStringArrayPref(this, "category", array)
        }
        Log.i(TAG, list.toString())
        checkCategory(list)
    }

    private fun checkCategory(list: ArrayList<String>) {
        if (list.isEmpty()) {
            check_box_digital.isChecked = true
            check_box_furniture.isChecked = true
            check_box_life.isChecked = true
            check_box_book.isChecked = true
            check_box_buy.isChecked = true
            check_box_etc.isChecked = true
            check_box_game.isChecked = true
            check_box_woman.isChecked = true
            check_box_man.isChecked = true
            check_box_sport.isChecked = true
        } else {
            if (list.contains(check_box_digital.text)) check_box_digital.isChecked = true
            if (list.contains(check_box_furniture.text)) check_box_furniture.isChecked = true
            if (list.contains(check_box_life.text)) check_box_life.isChecked = true
            if (list.contains(check_box_book.text)) check_box_book.isChecked = true
            if (list.contains(check_box_buy.text)) check_box_buy.isChecked = true
            if (list.contains(check_box_etc.text)) check_box_etc.isChecked = true
            if (list.contains(check_box_game.text)) check_box_game.isChecked = true
            if (list.contains(check_box_woman.text)) check_box_woman.isChecked = true
            if (list.contains(check_box_man.text)) check_box_man.isChecked = true
            if (list.contains(check_box_sport.text)) check_box_sport.isChecked = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // 뒤로가기
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        setPrefAndResult()
        super.onBackPressed()
    }

    private fun setPrefAndResult() {
        val list = ArrayList<String>()

        if (check_box_digital.isChecked) list.add(check_box_digital.text.toString())
        if (check_box_furniture.isChecked) list.add(check_box_furniture.text.toString())
        if (check_box_life.isChecked) list.add(check_box_life.text.toString())
        if (check_box_book.isChecked) list.add(check_box_book.text.toString())
        if (check_box_buy.isChecked) list.add(check_box_buy.text.toString())
        if (check_box_etc.isChecked) list.add(check_box_etc.text.toString())
        if (check_box_game.isChecked) list.add(check_box_game.text.toString())
        if (check_box_woman.isChecked) list.add(check_box_woman.text.toString())
        if (check_box_man.isChecked) list.add(check_box_man.text.toString())
        if (check_box_sport.isChecked) list.add(check_box_sport.text.toString())
        
        SharedPreferencesUtils.instance.setStringArrayPref(this, "category", list)
        setResult(Activity.RESULT_OK)
    }

}
