package com.example.carepick

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.carepick.ui.home.HomeFragment // MainActivity가 로드할 Fragment
import com.example.carepick.ui.profile.UserProfileFragment
import com.example.carepick.ui.search.result.SearchResultFragment
import com.example.carepick.ui.selfDiagnosis.SelfDiagnosisFragment

class MainActivity : AppCompatActivity() {

    // ✅ 각 프래그먼트의 인스턴스를 저장할 변수 선언
    private val homeFragment = HomeFragment()
    val searchResultFragment = SearchResultFragment()
    val selfDiagnosisFragment = SelfDiagnosisFragment()
    val userProfileFragment = UserProfileFragment()

    // ✅ 현재 활성화된 프래그먼트를 추적할 변수
    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // ✅ 앱이 처음 시작될 때 모든 프래그먼트를 추가(add)하고, 홈 화면만 보여줌(show)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragment_container, userProfileFragment, "4").hide(userProfileFragment)
                add(R.id.fragment_container, selfDiagnosisFragment, "3").hide(selfDiagnosisFragment)
                add(R.id.fragment_container, searchResultFragment, "2").hide(searchResultFragment)
                // 홈 프래그먼트는 마지막에 추가하고 보여줌
                add(R.id.fragment_container, homeFragment, "1")
            }.commit()
        }

        findViewById<ConstraintLayout>(R.id.nav_home).setOnClickListener {
            switchFragment(homeFragment)
            updateNavIcons(R.id.nav_home)
        }

        findViewById<ConstraintLayout>(R.id.nav_search).setOnClickListener {
            switchFragment(searchResultFragment)
            updateNavIcons(R.id.nav_search)
        }

        findViewById<ConstraintLayout>(R.id.nav_self_diagnosis).setOnClickListener {
            switchFragment(selfDiagnosisFragment)
            updateNavIcons(R.id.nav_self_diagnosis)
        }

        findViewById<ConstraintLayout>(R.id.nav_profile).setOnClickListener {
            switchFragment(userProfileFragment)
            updateNavIcons(R.id.nav_profile)
        }
    }

    // ✅ 새로운 프래그먼트 전환 함수
    private fun switchFragment(fragment: Fragment) {
        // 이미 활성화된 프래그먼트를 다시 누른 경우 아무것도 하지 않음
        if (fragment == activeFragment) return

        supportFragmentManager.beginTransaction()
            .hide(activeFragment) // 현재 활성화된 프래그먼트는 숨기고
            .show(fragment)       // 선택된 프래그먼트는 보여줌
            .commit()
        activeFragment = fragment // 활성 프래그먼트 교체
    }

    /** ✅ HomeFragment에서 탭 전환을 요청할 때 사용할 함수 */
    fun navigateToTab(tabId: Int, args: Bundle? = null) {
        val targetFragment = when (tabId) {
            R.id.nav_home -> homeFragment
            R.id.nav_search -> {
                // 검색어 등 전달된 인자가 있으면 SearchResultFragment에 설정
                searchResultFragment.arguments = args
                searchResultFragment
            }
            R.id.nav_self_diagnosis -> selfDiagnosisFragment
            R.id.nav_profile -> userProfileFragment
            else -> null
        }

        if (targetFragment != null) {
            switchFragment(targetFragment)
            updateNavIcons(tabId)
        }
    }

    /** ✅ 프래그먼트가 자신을 활성 프래그먼트로 등록할 수 있도록 하는 함수 */
    fun updateActiveFragment(fragment: Fragment) {
        // 메인 탭 프래그먼트 중 하나일 경우에만 activeFragment 참조를 업데이트
        if (fragment is HomeFragment || fragment is SearchResultFragment || fragment is SelfDiagnosisFragment || fragment is UserProfileFragment) {
            activeFragment = fragment
        }
    }

    fun updateNavIcons(activeId: Int) {
        // 모든 아이콘을 비활성화된 상태로 초기화
        findViewById<ImageView>(R.id.nav_home_icon).setImageResource(R.drawable.ic_home_deactivated)
        findViewById<ImageView>(R.id.nav_search_icon).setImageResource(R.drawable.ic_search_deactivated)
        findViewById<ImageView>(R.id.nav_self_diagnosis_icon).setImageResource(R.drawable.ic_recommand_deactivated)
        findViewById<ImageView>(R.id.nav_profile_icon).setImageResource(R.drawable.ic_profile_deactivated)

        // -1이면 아무것도 활성화하지 않음
        if (activeId == -1) return

        // 선택된 아이콘만 활성화 상태로 교체
        when (activeId) {
            R.id.nav_home -> findViewById<ImageView>(R.id.nav_home_icon)
                .setImageResource(R.drawable.ic_home_activated)

            R.id.nav_search -> findViewById<ImageView>(R.id.nav_search_icon)
                .setImageResource(R.drawable.ic_search_activated)

            R.id.nav_self_diagnosis -> findViewById<ImageView>(R.id.nav_self_diagnosis_icon)
                .setImageResource(R.drawable.ic_recommand_activated)

            R.id.nav_profile -> findViewById<ImageView>(R.id.nav_profile_icon)
                .setImageResource(R.drawable.ic_profile_activated)
        }
    }
}