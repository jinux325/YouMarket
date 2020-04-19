package com.u.marketapp.activity

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.tabs.TabLayout
import com.u.marketapp.R
import com.u.marketapp.history.SalesFragment1
import com.u.marketapp.history.SalesFragment2
import com.u.marketapp.history.SalesFragment3
import kotlinx.android.synthetic.main.activity_sales_history.*

class SalesHistoryActivity : AppCompatActivity() {

    private lateinit var actionbar: ActionBar
    private var backPressedTime: Long = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_history)
        initView()
    }

    private fun initView() {
        setActionBar()
        setStatePageAdapter()
        setTabLayout()
    }

    // 액션바
    private fun setActionBar() {
        setSupportActionBar(toolbar)
        actionbar = supportActionBar!!
        actionbar.title = "판매내역"
        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    private fun setStatePageAdapter() {
        val adapter = PagerAdapter(supportFragmentManager)
        adapter.addFragment(SalesFragment1(), "판매중")
        adapter.addFragment(SalesFragment2(), "거래완료")
        adapter.addFragment(SalesFragment3(), "숨김")

        view_pager.adapter = adapter
        layout_tab.setupWithViewPager(view_pager, true)
    }

    private fun setTabLayout() {
        view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(layout_tab))
        layout_tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager.currentItem = tab.position
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                val count = fm.backStackEntryCount
                if (count >= 1) supportFragmentManager.popBackStack()
                ft.commit()
            }
        })
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() > backPressedTime + 2000) {
            backPressedTime = System.currentTimeMillis()
            showToastMessage()
            return
        }

        if (System.currentTimeMillis() <= backPressedTime + 2000) {
            this.finish()
        }
    }

    private fun showToastMessage() {
        Toast.makeText(this, "뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    inner class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList: MutableList<Fragment> = ArrayList<Fragment>()
        private val fragmentTitleList: MutableList<String> = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int = fragmentList.size

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }

    }

}
