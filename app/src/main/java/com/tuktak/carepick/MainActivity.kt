package com.tuktak.carepick

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.tuktak.carepick.ui.home.HomeFragment // MainActivity가 로드할 Fragment
import com.tuktak.carepick.ui.hospital.HospitalDetailFragment
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

    private var currentTabId: Int = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // ✅ 저장된 상태가 있다면, 마지막 탭 ID를 복구합니다.
        if (savedInstanceState != null) {
            currentTabId = savedInstanceState.getInt("CURRENT_TAB_ID", R.id.nav_home)
        }

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
            handleTabClick(homeFragment, R.id.nav_home)
        }
        findViewById<ConstraintLayout>(R.id.nav_hospital).setOnClickListener {
            handleTabClick(hospitalFragment, R.id.nav_hospital)
        }
        findViewById<ConstraintLayout>(R.id.nav_doctor).setOnClickListener {
            handleTabClick(doctorFragment, R.id.nav_doctor)
        }
        findViewById<ConstraintLayout>(R.id.nav_self_diagnosis).setOnClickListener {
            handleTabClick(selfDiagnosisFragment, R.id.nav_self_diagnosis)
        }
    }

    // ✅ [수정] 탭 클릭 처리 헬퍼 함수
    private fun handleTabClick(fragment: Fragment, navId: Int) {
        // 1. 현재 탭을 다시 누른 경우
        if (activeFragment == fragment) {
            // 상세 페이지(백스택)가 열려 있으면 모두 닫고 목록으로 복귀
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }
        // 2. 다른 탭을 누른 경우
        else {
            // ✅ 다른 탭으로 이동할 때도 현재 탭의 상세 페이지(백스택)를 모두 닫습니다.
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            // 탭을 전환합니다.
            switchFragment(fragment)
            currentTabId = navId
        }

        // 아이콘을 강제로 업데이트합니다.
        updateNavIcons(navId)
    }

    // ✅ 시스템에 의해 액티비티가 종료될 때 현재 탭 ID를 저장합니다.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("CURRENT_TAB_ID", currentTabId)
    }

    // ✅ 새로운 프래그먼트 전환 함수
    private fun switchFragment(fragment: Fragment) {

        val transaction = supportFragmentManager.beginTransaction()

        supportFragmentManager.fragments.filter { it.isVisible }.forEach {
            transaction.hide(it)
        }

        // 2. ✅ 목표 프래그먼트(fragment)를 '보여줍니다(show)'.
        transaction.show(fragment)

        transaction.commit()

        activeFragment = fragment // activeFragment는 베이스 탭을 가리키도록 유지
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
            currentTabId = tabId

            // 1. 해당 탭으로 먼저 전환 (백스택을 비우지 않음!)
            switchFragment(targetFragment)

            // 2. ✅ 아이콘을 여기서 명시적으로 업데이트
            updateNavIcons(tabId)

            // 3. ✅ Bundle을 확인하여 추가 액션(상세 페이지 열기) 수행
            if (args != null) {
                if (tabId == R.id.nav_hospital && args.containsKey("hospitalId")) {
                    val hospitalDetailFragment = HospitalDetailFragment()
                    hospitalDetailFragment.arguments = args

                    supportFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, hospitalDetailFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    fun updateNavIcons(activeId: Int) {
        // 모든 아이콘을 비활성화된 상태로 초기화
        findViewById<ImageView>(R.id.nav_home_icon).setImageResource(R.drawable.ic_home_deactivated)
        findViewById<ImageView>(R.id.nav_hospital_icon).setImageResource(R.drawable.ic_hospital_deactivated)
        findViewById<ImageView>(R.id.nav_doctor_icon).setImageResource(R.drawable.ic_doctor_deactivated)
        findViewById<ImageView>(R.id.nav_self_diagnosis_icon).setImageResource(R.drawable.ic_recommand_deactivated)
        // -1이면 아무것도 활성화하지 않음
        if (activeId == -1) return

        // 선택된 아이콘만 활성화 상태로 교체
        when (activeId) {
            R.id.nav_home -> findViewById<ImageView>(R.id.nav_home_icon)
                .setImageResource(R.drawable.ic_home_activated)

            R.id.nav_hospital -> findViewById<ImageView>(R.id.nav_hospital_icon)
                .setImageResource(R.drawable.ic_hospital_activated)

            R.id.nav_doctor -> findViewById<ImageView>(R.id.nav_doctor_icon)
                .setImageResource(R.drawable.ic_doctor_activated)

            R.id.nav_self_diagnosis -> findViewById<ImageView>(R.id.nav_self_diagnosis_icon)
                .setImageResource(R.drawable.ic_recommand_activated)
        }
    }

    override fun onResume() {
        super.onResume()

        // 1. 현재 FragmentManager가 관리 중인 프래그먼트들 중에서
        //    실제로 사용자 눈에 보이고 있는(isVisible) TabOwner 프래그먼트를 찾습니다.
        val visibleFragment = supportFragmentManager.fragments.firstOrNull { it.isVisible && it is TabOwner }

        if (visibleFragment != null) {
            // 2. 찾았다면, 그 프래그먼트를 activeFragment로 재설정하여 동기화를 맞춥니다.
            activeFragment = visibleFragment

            // 3. 그 프래그먼트의 ID를 가져와서 currentTabId도 업데이트합니다.
            val realTabId = (visibleFragment as TabOwner).getNavId()
            currentTabId = realTabId

            // 4. 마지막으로 네비게이션 바 아이콘을 강제로 업데이트합니다.
            updateNavIcons(realTabId)
        } else {
            // 만약 보이는 프래그먼트를 못 찾았다면(예외 상황), 저장해뒀던 currentTabId를 믿어봅니다.
            updateNavIcons(currentTabId)
        }
    }
}