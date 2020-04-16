package com.u.marketapp.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.u.marketapp.activity.CategoryActivity
import com.u.marketapp.R
import com.u.marketapp.activity.SearchActivity
import kotlinx.android.synthetic.main.fragment_category.*

class CategoryFragment : Fragment(), View.OnClickListener {

    companion object {
        private val TAG = CategoryFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_category, container, false)
        initView(view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        setActionbar()
    }

    private fun initView(v: View) {
//        setActionbar()
        v.findViewById<LinearLayout>(R.id.layout_category_digital).setOnClickListener(this) // 디지털
        v.findViewById<LinearLayout>(R.id.layout_category_furniture).setOnClickListener(this) // 가구
        v.findViewById<LinearLayout>(R.id.layout_category_life).setOnClickListener(this) // 생활
        v.findViewById<LinearLayout>(R.id.layout_category_woman).setOnClickListener(this) // 여성패션
        v.findViewById<LinearLayout>(R.id.layout_category_man).setOnClickListener(this) // 남성패션
        v.findViewById<LinearLayout>(R.id.layout_category_sport).setOnClickListener(this) // 스포츠
        v.findViewById<LinearLayout>(R.id.layout_category_game).setOnClickListener(this) // 게임
        v.findViewById<LinearLayout>(R.id.layout_category_book).setOnClickListener(this) // 도서
        v.findViewById<LinearLayout>(R.id.layout_category_etc).setOnClickListener(this) // 기타 중고물품
        v.findViewById<LinearLayout>(R.id.layout_category_buy).setOnClickListener(this) // 삽니다
    }

    // 액션바
    private fun setActionbar() {
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
            val actionbar = (activity as AppCompatActivity).supportActionBar!!
            actionbar.setDisplayShowTitleEnabled(true)
            actionbar.setDisplayHomeAsUpEnabled(false)
        }
    }

    // 검색 페이지 이동
    private fun moveSearchActivity() {
        Log.i(TAG, "검색 화면으로 이동!!!!")
        val intent = Intent(context, SearchActivity::class.java)
        startActivity(intent)
    }

    private fun moveCategoryActivity(title: String) {
        Log.i(TAG, "카테고리 화면으로 이동")
        val intent = Intent(context, CategoryActivity::class.java)
        intent.putExtra("title", title)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_category, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                moveSearchActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View?) {
        Log.i(TAG, "카테고리 클릭!!")
        when (v?.id) {
            R.id.layout_category_digital -> { // 디지털
                moveCategoryActivity(resources.getString(R.string.category_digital))
            }
            R.id.layout_category_furniture -> { // 가구
                moveCategoryActivity(resources.getString(R.string.category_furniture))
            }
            R.id.layout_category_life -> { // 생활
                moveCategoryActivity(resources.getString(R.string.category_life))
            }
            R.id.layout_category_woman -> { // 여성패션
                moveCategoryActivity(resources.getString(R.string.category_woman))
            }
            R.id.layout_category_man -> { // 남성패션
                moveCategoryActivity(resources.getString(R.string.category_man))
            }
            R.id.layout_category_sport -> { // 스포츠
                moveCategoryActivity(resources.getString(R.string.category_sport))
            }
            R.id.layout_category_game -> { // 게임
                moveCategoryActivity(resources.getString(R.string.category_game))
            }
            R.id.layout_category_book -> { // 도서
                moveCategoryActivity(resources.getString(R.string.category_book))
            }
            R.id.layout_category_etc -> { // 기타 중고물품
                moveCategoryActivity(resources.getString(R.string.category_etc))
            }
            R.id.layout_category_buy -> { // 삽니다
                moveCategoryActivity(resources.getString(R.string.category_buy))
            }
        }
    }

}
