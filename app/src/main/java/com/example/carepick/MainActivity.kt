package com.example.carepick

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.carepick.ui.favorite.FavoriteFragment
import com.example.carepick.ui.home.HomeFragment // MainActivity가 로드할 Fragment
import com.example.carepick.ui.profile.UserProfileFragment
import com.example.carepick.ui.search.SearchResultFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 메인 액티비티가 ui를 가질 경우 특정 프래그먼트를 불러올 때 화면이 겹치는 문제가 발생한다
        // 따라서 메인 액티비티는 자체 ui를 가지지 않고 바로 HomeFragment를 불러오도록 하였다
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        findViewById<ConstraintLayout>(R.id.nav_home).setOnClickListener {
            loadFragment(HomeFragment())
            updateNavIcons(R.id.nav_home)
        }

        findViewById<ConstraintLayout>(R.id.nav_search).setOnClickListener {
            loadFragment(SearchResultFragment())
            updateNavIcons(R.id.nav_search)
        }

        findViewById<ConstraintLayout>(R.id.nav_recommand).setOnClickListener {
            loadFragment(FavoriteFragment())
            updateNavIcons(R.id.nav_recommand)
        }

        findViewById<ConstraintLayout>(R.id.nav_profile).setOnClickListener {
            loadFragment(UserProfileFragment())
            updateNavIcons(R.id.nav_profile)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun updateNavIcons(activeId: Int) {
        // 모든 아이콘을 비활성화된 상태로 초기화
        findViewById<ImageView>(R.id.nav_home_icon).setImageResource(R.drawable.ic_home_deactivated)
        findViewById<ImageView>(R.id.nav_search_icon).setImageResource(R.drawable.ic_search_deactivated)
        findViewById<ImageView>(R.id.nav_recommand_icon).setImageResource(R.drawable.ic_recommand_deactivated)
        findViewById<ImageView>(R.id.nav_profile_icon).setImageResource(R.drawable.ic_profile_deactivated)

        // -1이면 아무것도 활성화하지 않음
        if (activeId == -1) return

        // 선택된 아이콘만 활성화 상태로 교체
        when (activeId) {
            R.id.nav_home -> findViewById<ImageView>(R.id.nav_home_icon)
                .setImageResource(R.drawable.ic_home_activated)

            R.id.nav_search -> findViewById<ImageView>(R.id.nav_search_icon)
                .setImageResource(R.drawable.ic_search_activated)

            R.id.nav_recommand -> findViewById<ImageView>(R.id.nav_recommand_icon)
                .setImageResource(R.drawable.ic_recommand_activated)

            R.id.nav_profile -> findViewById<ImageView>(R.id.nav_profile_icon)
                .setImageResource(R.drawable.ic_profile_activated)
        }
    }
}