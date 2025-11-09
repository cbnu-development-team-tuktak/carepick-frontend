package com.tuktak.carepick.ui.search.result.doctor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tuktak.carepick.R
import com.tuktak.carepick.ui.search.filter.adapter.SpecialtyAdapter

class DoctorFilterFragment : Fragment() {
    // ✅ 의사 전용 ViewModel 사용
    private val filterVM: DoctorFilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {
        // 💡 Tip: 의사 전용 레이아웃(fragment_doctor_filter.xml)을 따로 만들어
        // 운영시간 관련 뷰들을 아예 XML에서 제거하는 것이 가장 깔끔합니다.
        return inflater.inflate(R.layout.fragment_doctor_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<View>(R.id.filterHeader)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updatePadding(top = topInset + 12) // 기존 padding 유지
            insets
        }

        // ✅ 초기화 버튼 리스너 추가
        toolbar.findViewById<TextView>(R.id.btn_reset)?.setOnClickListener {
            resetAllFilters() // 초기화 함수 호출
        }

        // 🩺 진료과 선택
        val specialtyList = listOf(
            "가정의학과", "내과","마취통증의학과", "방사선종양학과", "병리과", "비뇨의학과", "산부인과", "산업의학과", "성형외과", "소아청소년과",
            "신경과", "신경외과", "안과", "영상의학과", "예방의학과", "외과", "응급의학과", "이비인후과", "재활의학과", "정신건강의학과", "정형외과",
            "직업환경의학과", "진단검사의학과", "치과", "피부과", "한방과", "핵의학과", "흉부외과",
            "감염내과", "내분비대사내과", "류마티스내과", "소화기내과", "순환기내과",   "신장내과", "혈액종양내과", "호흡기내과",
        )

        val specialtyRecyclerView = view.findViewById<RecyclerView>(R.id.specialty_recycler_view)
        // 💡 WindowInsets을 사용하여 네비게이션 바 높이만큼 동적으로 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(specialtyRecyclerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            insets
        }
        specialtyRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        specialtyRecyclerView.adapter = SpecialtyAdapter(specialtyList, filterVM.selectedSpecialties)

        // 🔙 뒤로가기 버튼
        view.findViewById<View>(R.id.btn_back)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun resetAllFilters() {
        // 1. ViewModel의 상태 초기화
        filterVM.resetFilters()

        // 2. UI 요소들을 초기 상태로 업데이트
        //    (기존 onViewCreated의 초기화 로직 재활용)

        // 진료과 RecyclerView
        // 어댑터에 ViewModel의 Set이 연결되어 있으므로, 어댑터에 변경 알림만 주면 됨
        val specialtyRecyclerView = view?.findViewById<RecyclerView>(R.id.specialty_recycler_view)
        (specialtyRecyclerView?.adapter as? SpecialtyAdapter)?.notifyDataSetChanged()

        Toast.makeText(requireContext(), "필터가 초기화되었습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val resultBundle = Bundle().apply {
            // ❌ 운영 시간 관련 데이터 넣지 않음
            putStringArrayList("selected_specialties", ArrayList(filterVM.selectedSpecialties))
            filterVM.selectedDistance?.let { putInt("selected_distance", it) }
        }
        // ✅ 의사 전용 결과 키 사용
        parentFragmentManager.setFragmentResult("doctor_filter_request", resultBundle)
    }
}