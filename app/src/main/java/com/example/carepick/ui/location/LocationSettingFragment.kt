package com.example.carepick.ui.location

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.carepick.R

class LocationSettingFragment : Fragment() {

    private val regionList = listOf(
        "지역", // 초기 표시 텍스트
        "서울", "경기", "인천", "강원", "충남", "대전", "충북", "세종",
        "부산", "울산", "대구", "경북", "경남", "전남", "광주", "전북", "제주", "전국"
    )

    private val districtList = listOf(
        "시/군/구", // 초기 표시 텍스트
        "강남구", "서초구", "송파구", "종로구", "용산구",
        "마포구", "성동구", "노원구", "중구", "동작구"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기 버튼
        view.findViewById<View>(R.id.btn_close).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val btnGps = view.findViewById<TextView>(R.id.btn_gps)
        val btnAddress = view.findViewById<TextView>(R.id.btn_address)
        val areaSelector = view.findViewById<LinearLayout>(R.id.area_selector)

        val containerProvince = view.findViewById<CardView>(R.id.container_province)
        val containerProvinceInner = view.findViewById<ConstraintLayout>(R.id.container_province_inner)
        val flowProvince = view.findViewById<Flow>(R.id.flow_province)

        val containerDistrict = view.findViewById<CardView>(R.id.container_district)
        val containerDistrictInner = view.findViewById<ConstraintLayout>(R.id.container_district_inner)
        val flowDistrict = view.findViewById<Flow>(R.id.flow_district)

        val spinnerProvince = view.findViewById<Spinner>(R.id.spinner_province)
        val spinnerCity = view.findViewById<Spinner>(R.id.spinner_city)

        var isGpsSelected = true
        val provinceButtons = mutableListOf<TextView>()

        // ✅ Spinner 기본 어댑터 (회색 없이 기본 스타일)
        val regionAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            regionList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerProvince.adapter = regionAdapter

        val cityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            districtList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerCity.adapter = cityAdapter

        // ✅ Spinner 터치 시 드롭다운 대신 커스텀 박스 열기
        spinnerProvince.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                containerProvince.visibility =
                    if (containerProvince.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                true
            } else false
        }

        spinnerCity.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                containerDistrict.visibility =
                    if (containerDistrict.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                true
            } else false
        }

        // 🟢 탭 전환 UI
        fun updateTabUI() {
            if (isGpsSelected) {
                btnGps.setBackgroundResource(R.drawable.bg_tab_left_selected)
                btnAddress.setBackgroundResource(R.drawable.bg_tab_right_unselected)
                areaSelector.visibility = View.GONE
                containerProvince.visibility = View.GONE
                containerDistrict.visibility = View.GONE
            } else {
                btnGps.setBackgroundResource(R.drawable.bg_tab_left_unselected)
                btnAddress.setBackgroundResource(R.drawable.bg_tab_right_selected)
                areaSelector.visibility = View.VISIBLE
                containerProvince.visibility = View.GONE
                containerDistrict.visibility = View.GONE
            }
        }

        btnGps.setOnClickListener {
            if (!isGpsSelected) {
                isGpsSelected = true
                updateTabUI()
            }
        }

        btnAddress.setOnClickListener {
            if (isGpsSelected) {
                isGpsSelected = false
                updateTabUI()
            }
        }

        updateTabUI()

        // 🏙️ 지역 버튼 추가
        val regionIds = mutableListOf<Int>()
        for ((index, region) in regionList.withIndex()) {
            if (index == 0) continue // '지역'은 선택 버튼에서 제외
            val btn = createSelectableButton(region)
            btn.id = View.generateViewId()
            containerProvinceInner.addView(btn)
            regionIds.add(btn.id)
            provinceButtons.add(btn)

            btn.setOnClickListener {
                provinceButtons.forEach {
                    it.setBackgroundResource(R.drawable.chip_unselected)
                    it.tag = false
                }
                btn.setBackgroundResource(R.drawable.chip_selected)
                btn.tag = true

                spinnerProvince.setSelection(index)
                containerProvince.visibility = View.GONE
                containerDistrict.visibility = View.VISIBLE
            }
        }
        flowProvince.referencedIds = regionIds.toIntArray()

        // 🏘️ 시군구 버튼 추가
        val districtIds = mutableListOf<Int>()
        for ((index, district) in districtList.withIndex()) {
            if (index == 0) continue
            val btn = createSelectableButton(district)
            btn.id = View.generateViewId()
            containerDistrictInner.addView(btn)
            districtIds.add(btn.id)

            btn.setOnClickListener {
                spinnerCity.setSelection(index)
                containerDistrict.visibility = View.GONE
            }
        }
        flowDistrict.referencedIds = districtIds.toIntArray()
    }

    private fun createSelectableButton(text: String): TextView {
        val textView = TextView(requireContext())
        textView.text = text
        textView.setPadding(32, 20, 32, 20)
        textView.setBackgroundResource(R.drawable.chip_unselected)
        textView.setTextColor(Color.BLACK)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)

        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(12, 12, 12, 12)
        textView.layoutParams = params

        return textView
    }
}