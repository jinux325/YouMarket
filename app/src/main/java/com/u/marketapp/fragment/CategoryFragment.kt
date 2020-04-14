package com.u.marketapp.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.u.marketapp.activity.CategoryActivity
import com.u.marketapp.R
import com.u.marketapp.activity.SearchActivity

class CategoryFragment : Fragment(), View.OnClickListener {

    companion object {
        private val TAG = CategoryFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    // 검색 페이지 이동
    private fun moveSearchActivity() {
        val intent = Intent(context, SearchActivity::class.java)
        startActivity(intent)
    }

    private fun moveCategoryActivity(title: String) {
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

    override fun onClick(v: View) {
        Log.i(TAG, "카테고리 클릭!!")
        when (v.id) {
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
