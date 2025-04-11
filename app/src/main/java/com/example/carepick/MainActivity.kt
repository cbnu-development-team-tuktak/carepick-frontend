package com.example.carepick

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.carepick.ui.favorite.FavoriteFragment
import com.example.carepick.ui.home.HomeFragment // MainActivity가 로드할 Fragment
import com.example.carepick.ui.profile.UserProfileFragment
import com.example.carepick.ui.search.SearchResultFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


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
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        findViewById<ConstraintLayout>(R.id.nav_search).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SearchResultFragment())
                .commit()
        }

        findViewById<ConstraintLayout>(R.id.nav_favorite).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FavoriteFragment())
                .commit()
        }

        findViewById<ConstraintLayout>(R.id.nav_profile).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UserProfileFragment())
                .commit()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}