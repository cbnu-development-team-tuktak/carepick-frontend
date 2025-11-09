package com.tuktak.carepick

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.tuktak.carepick.ui.home.HomeFragment // MainActivity가 로드할 Fragment
import com.tuktak.carepick.ui.search.result.doctor.DoctorSearchResultFragment
import com.tuktak.carepick.ui.search.result.hospital.HospitalSearchResultFragment
import com.tuktak.carepick.ui.selfDiagnosis.SelfDiagnosisFragment

class MainActivity : AppCompatActivity() {

    // ✅ 각 프래그먼트의 인스턴스를 저장할 변수 선언
    private val homeFragment = HomeFragment()
    val hospitalFragment = HospitalSearchResultFragment()
    val doctorFragment = DoctorSearchResultFragment()

    val selfDiagnosisFragment = SelfDiagnosisFragment()

    // ✅ 현재 활성화된 프래그먼트를 추적할 변수
    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // ✅ 앱이 처음 시작될 때 모든 프래그먼트를 추가(add)하고, 홈 화면만 보여줌(show)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragment_container, selfDiagnosisFragment, "4").hide(selfDiagnosisFragment)
                add(R.id.fragment_container, hospitalFragment, "3").hide(hospitalFragment)
                add(R.id.fragment_container, doctorFragment, "2").hide(doctorFragment)
                // 홈 프래그먼트는 마지막에 추가하고 보여줌
                add(R.id.fragment_container, homeFragment, "1")
            }.commit()
        }

        findViewById<ConstraintLayout>(R.id.nav_home).setOnClickListener {
            switchFragment(homeFragment)
        }

        findViewById<ConstraintLayout>(R.id.nav_hospital).setOnClickListener {
            switchFragment(hospitalFragment)
        }
        findViewById<ConstraintLayout>(R.id.nav_doctor).setOnClickListener {
            switchFragment(doctorFragment)
        }

        findViewById<ConstraintLayout>(R.id.nav_self_diagnosis).setOnClickListener {
            switchFragment(selfDiagnosisFragment)
        }


//        // ✅ 프래그먼트 스택에 변경이 있을 때마다 감지하는 리스너 추가
//        supportFragmentManager.addOnBackStackChangedListener {
//            updateNavSelection()
//        }
//
//        // 초기 아이콘 상태 설정
//        updateNavSelection()

    }

    // ✅ 새로운 프래그먼트 전환 함수
    private fun switchFragment(fragment: Fragment) {
        if (fragment == activeFragment && supportFragmentManager.backStackEntryCount == 0) return

        // 1. ✅ 백스택을 모두 비워서 상세 페이지 등을 모두 닫습니다.
        // 'inclusive' 플래그는 지정된 트랜잭션까지 포함하여 제거하라는 의미입니다.
        // 여기서는 null을 주어 가장 처음까지의 모든 스택을 비웁니다.
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // 2. ✅ 기존의 hide/show 로직을 실행합니다.
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(fragment)
            .commit()

        activeFragment = fragment

        if (fragment is TabOwner) {
            updateNavIcons(fragment.getNavId())
        }
    }

    // 탭 클릭 처리 헬퍼 함수 (중복 코드 제거용)
//    private fun handleTabClick(fragment: Fragment, navId: Int) {
//        if (activeFragment == fragment && supportFragmentManager.backStackEntryCount > 0) {
//            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
//            updateNavIcons(navId)
//        } else {
//            switchFragment(fragment)
//        }
//    }

    /** ✅ 현재 화면의 프래그먼트를 확인하고, 그에 맞는 탭 아이콘을 활성화하는 중앙 제어 함수 */
    private fun updateNavSelection() {
        // 현재 화면에 보이는 프래그먼트를 찾습니다.
        val visibleFragment = supportFragmentManager.fragments.lastOrNull { it.isVisible }

        // 보이는 프래그먼트가 TabOwner 인터페이스를 구현했다면
        if (visibleFragment is TabOwner) {
            // 해당 프래그먼트가 알려주는 탭 ID로 아이콘을 업데이트합니다.
            updateNavIcons(visibleFragment.getNavId())
        } else {
            // TabOwner가 아닌 프래그먼트(예: 시스템 다이얼로그)가 위에 떠 있다면
            // 어떤 아이콘도 활성화하지 않을 수 있습니다. (선택적)
            // updateNavIcons(-1)
        }
    }

    /** ✅ HomeFragment에서 탭 전환을 요청할 때 사용할 함수 */
    fun navigateToTab(tabId: Int, args: Bundle? = null) {
        val targetFragment = when (tabId) {
            R.id.nav_home -> homeFragment

            // ✅ [변경] 병원 탭으로 이동
            R.id.nav_hospital -> {
                if (args != null) {
                    hospitalFragment.arguments = args
                }
                hospitalFragment
            }

            // ✅ [추가] 의사 탭으로 이동
            R.id.nav_doctor -> {
                if (args != null) {
                    doctorFragment.arguments = args
                }
                doctorFragment
            }

            R.id.nav_self_diagnosis -> selfDiagnosisFragment
            else -> null
        }

        if (targetFragment != null) {
            switchFragment(targetFragment)
        }
    }

    /** ✅ 프래그먼트가 자신을 활성 프래그먼트로 등록할 수 있도록 하는 함수 */
    fun updateActiveFragment(fragment: Fragment) {
        // 메인 탭 프래그먼트 중 하나일 경우에만 activeFragment 참조를 업데이트
        if (fragment is HomeFragment || fragment is HospitalSearchResultFragment || fragment is SelfDiagnosisFragment) {
            activeFragment = fragment
        }
    }

    fun updateNavIcons(activeId: Int) {
        // 모든 아이콘을 비활성화된 상태로 초기화
        findViewById<ImageView>(R.id.nav_home_icon).setImageResource(R.drawable.ic_home_deactivated)
        findViewById<ImageView>(R.id.nav_hospital_icon).setImageResource(R.drawable.ic_search_deactivated)
        findViewById<ImageView>(R.id.nav_doctor_icon).setImageResource(R.drawable.ic_search_deactivated)
        findViewById<ImageView>(R.id.nav_self_diagnosis_icon).setImageResource(R.drawable.ic_recommand_deactivated)
        // -1이면 아무것도 활성화하지 않음
        if (activeId == -1) return

        // 선택된 아이콘만 활성화 상태로 교체
        when (activeId) {
            R.id.nav_home -> findViewById<ImageView>(R.id.nav_home_icon)
                .setImageResource(R.drawable.ic_home_activated)

            R.id.nav_hospital -> findViewById<ImageView>(R.id.nav_hospital_icon)
                .setImageResource(R.drawable.ic_search_activated)

            R.id.nav_doctor -> findViewById<ImageView>(R.id.nav_doctor_icon)
                .setImageResource(R.drawable.ic_search_activated)

            R.id.nav_self_diagnosis -> findViewById<ImageView>(R.id.nav_self_diagnosis_icon)
                .setImageResource(R.drawable.ic_recommand_activated)
        }
    }
}