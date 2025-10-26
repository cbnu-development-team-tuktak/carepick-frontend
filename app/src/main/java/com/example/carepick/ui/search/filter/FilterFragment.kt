package com.example.carepick.ui.search.filter

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.carepick.R
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.ui.search.FilterViewModel
import com.example.carepick.ui.search.filter.adapter.SpecialtyAdapter
import com.example.carepick.ui.search.result.SearchMode
import java.text.SimpleDateFormat
import java.util.*

class FilterFragment : Fragment() {

    private val filterVM: FilterViewModel by activityViewModels()
    private val logTag = "FilterDebug"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(logTag, "[onViewCreated] Initial filterVM.selectedDays: ${filterVM.selectedDays}")
        Log.d(logTag, "[onViewCreated] Initial ViewModel Hash: ${filterVM.hashCode()}") // ViewModel 인스턴스 확인

        // ✅ 상태바 padding 적용
        val toolbar = view.findViewById<View>(R.id.filterHeader)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updatePadding(top = topInset + 12) // 기존 padding 유지
            insets
        }

        // ✅ 검색 모드를 arguments로부터 가져옵니다.
        val modeString = arguments?.getString("current_search_mode")
        val currentMode = if (modeString == "DOCTOR") SearchMode.DOCTOR else SearchMode.HOSPITAL

        // ✅ 의사 모드일 경우, 운영 시간 섹션을 숨깁니다.
        val distanceSection = view.findViewById<View>(R.id.distance_section)
        val dividerOperationTimeSection = view.findViewById<View>(R.id.divider_operation_time)
        val operationTimeSection = view.findViewById<View>(R.id.operation_time_section)
        val dividerSpecialtySection = view.findViewById<View>(R.id.divider_specialty_section)
        if (currentMode == SearchMode.DOCTOR) {
            distanceSection.visibility = View.GONE
            dividerOperationTimeSection.visibility = View.GONE
            operationTimeSection.visibility = View.GONE
            dividerSpecialtySection.visibility = View.GONE
        } else {
            distanceSection.visibility = View.VISIBLE
            operationTimeSection.visibility = View.VISIBLE
        }

        // ✅ 초기화 버튼 리스너 추가
        toolbar.findViewById<TextView>(R.id.btn_reset)?.setOnClickListener {
            Log.d(logTag, "Reset button clicked, calling resetAllFilters()")
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

        val specialtyAdapter = SpecialtyAdapter(specialtyList, filterVM.selectedSpecialties)
        specialtyRecyclerView.adapter = specialtyAdapter


        // 🔙 뒤로가기 버튼
        view.findViewById<View>(R.id.btn_back)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 📏 거리 범위 SeekBar
        val seekBar = view.findViewById<SeekBar>(R.id.distance_slider)
        val label = view.findViewById<TextView>(R.id.slider_value_label)

        val initialDistance = filterVM.selectedDistance ?: 0
        seekBar.progress = initialDistance

        seekBar.post {
            // 이 시점에는 seekBar.width, seekBar.x 등의 값이 유효할 확률이 높습니다.
            updateSeekBarLabel(seekBar, label, initialDistance)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (seekBar != null) {
                    // ✅ 라벨 업데이트 함수 호출
                    updateSeekBarLabel(seekBar, label, progress)

                    // ✅ ViewModel의 거리 상태 업데이트
                    filterVM.updateDistance(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        fun showTimePicker(targetView: TextView) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val dialog = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                val isAM = selectedHour < 12
                val hourDisplay = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                val amPm = if (isAM) "오전" else "오후"
                val formatted = String.format("%s %02d : %02d", amPm, hourDisplay, selectedMinute)
                targetView.text = formatted
            }, hour, minute, false)

            dialog.show()
        }

        // 🕐 운영시간 설정
        val startTimeText = view.findViewById<TextView>(R.id.start_time_text)
        val startTimeIcon = view.findViewById<ImageView>(R.id.start_time_icon)
        val startTimeContainer = view.findViewById<LinearLayout>(R.id.start_time_container)
        val endTimeContainer = view.findViewById<LinearLayout>(R.id.end_time_container)
        val endTimeText = view.findViewById<TextView>(R.id.end_time_text)
        val endTimeIcon = view.findViewById<ImageView>(R.id.end_time_icon)

        startTimeContainer.setOnClickListener { showTimePicker(startTimeText) }
        endTimeContainer.setOnClickListener { showTimePicker(endTimeText) }

        startTimeText.text = formatApiTimeToDisplayTime(filterVM.startTime)
        endTimeText.text = formatApiTimeToDisplayTime(filterVM.endTime)

        // ⏳ 시간 범위 Spinner 설정
        val spinner = view.findViewById<Spinner>(R.id.time_range_spinner)
        val spinnerContainer = view.findViewById<LinearLayout>(R.id.spinner_container)
        val spinnerIcon = view.findViewById<ImageView>(R.id.spinner_dropdown_icon)
        val timeRanges = listOf("30분", "1시간", "1시간 30분", "2시간", "2시간 30분", "3시간")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeRanges)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinnerIcon.setOnClickListener { spinner.performClick() }

        spinner.setSelection(filterVM.selectedTimeRangeIndex, false)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                filterVM.updateTimeRangeIndex(position)

                val selectedRange = parent.getItemAtPosition(position).toString()
                val startText = startTimeText.text.toString()
                val regex = Regex("오(전|후) (\\d{2}) : (\\d{2})")
                val match = regex.find(startText)
                if (match != null) {
                    val ampm = match.groupValues[1]
                    var hour = match.groupValues[2].toInt()
                    val minute = match.groupValues[3].toInt()
                    if (ampm == "오후" && hour != 12) hour += 12
                    if (ampm == "오전" && hour == 12) hour = 0

                    val duration = when {
                        selectedRange.contains("시간") && selectedRange.contains("분") -> {
                            val parts = selectedRange.split("시간", "분")
                            parts[0].trim().toInt() * 60 + parts[1].trim().toInt()
                        }
                        selectedRange.contains("시간") -> selectedRange.replace("시간", "").trim().toInt() * 60
                        selectedRange.contains("분") -> selectedRange.replace("분", "").trim().toInt()
                        else -> 0
                    }

                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        add(Calendar.MINUTE, duration)
                    }

                    val endHour24 = calendar.get(Calendar.HOUR_OF_DAY)
                    val endMinute = calendar.get(Calendar.MINUTE)
                    val isAM = endHour24 < 12
                    val endHour12 = if (endHour24 % 12 == 0) 12 else endHour24 % 12
                    val endAmPm = if (isAM) "오전" else "오후"
                    endTimeText.text = String.format("%s %02d : %02d", endAmPm, endHour12, endMinute)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // ✅ 요일 선택
        val dayButtons = mapOf(
            "월" to view.findViewById<TextView>(R.id.day_mon),
            "화" to view.findViewById<TextView>(R.id.day_tue),
            "수" to view.findViewById<TextView>(R.id.day_wed),
            "목" to view.findViewById<TextView>(R.id.day_thu),
            "금" to view.findViewById<TextView>(R.id.day_fri),
            "토" to view.findViewById<TextView>(R.id.day_sat),
            "일" to view.findViewById<TextView>(R.id.day_sun)
        )
        updateDayButtonUI(dayButtons, filterVM.selectedDays)

        val dayGroupSpinner = view.findViewById<Spinner>(R.id.day_group_spinner)
        val dayGroupIcon = view.findViewById<ImageView>(R.id.day_group_icon)
        val dayGroupOptions = listOf("선택", "평일", "주말", "매일")
        val groupAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dayGroupOptions)
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dayGroupSpinner.adapter = groupAdapter
        dayGroupIcon.setOnClickListener { dayGroupSpinner.performClick() }

        // --- 스피너 초기 선택 상태 설정 코드 추가 ---
        // ViewModel의 현재 selectedDays 상태를 기반으로 초기 인덱스 계산
        val initialSpinnerIndex = when {
            filterVM.selectedDays.containsAll(listOf("월", "화", "수", "목", "금", "토", "일")) -> 3 // 매일
            filterVM.selectedDays.containsAll(listOf("월", "화", "수", "목", "금")) && filterVM.selectedDays.size == 5 -> 1 // 평일
            filterVM.selectedDays.containsAll(listOf("토", "일")) && filterVM.selectedDays.size == 2 -> 2 // 주말
            else -> 0 // 그 외 (개별 선택 포함)는 "선택"
        }
        dayGroupSpinner.setSelection(initialSpinnerIndex, false)

        dayGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // ✅ ViewModel의 selectedDays를 직접 수정합니다.
                when (dayGroupOptions[position]) {
                    "선택" -> filterVM.selectedDays.clear()
                    "평일" -> filterVM.selectedDays.setAll("월", "화", "수", "목", "금") // setAll 확장 함수 사용
                    "주말" -> filterVM.selectedDays.setAll("토", "일")
                    "매일" -> filterVM.selectedDays.setAll(*dayButtons.keys.toTypedArray())
                }
                Log.d(logTag, "[Spinner] filterVM.selectedDays updated: ${filterVM.selectedDays}")
                // ✅ UI 업데이트 시에도 ViewModel의 selectedDays를 사용합니다.
                updateDayButtonUI(dayButtons, filterVM.selectedDays)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        for ((day, btn) in dayButtons) {
            btn.setOnClickListener {
                // ✅ ViewModel의 selectedDays를 직접 수정합니다.
                if (filterVM.selectedDays.contains(day)) {
                    filterVM.selectedDays.remove(day)
                } else {
                    filterVM.selectedDays.add(day)
                }
                Log.d(logTag, "[Button $day] filterVM.selectedDays updated: ${filterVM.selectedDays}")
                // ✅ UI 업데이트 시에도 ViewModel의 selectedDays를 사용합니다.
                updateDayButtonUI(dayButtons, filterVM.selectedDays)

                // Spinner 상태 동기화 로직 (ViewModel 값 기준)
                dayGroupSpinner.setSelection(
                    when {
                        filterVM.selectedDays.containsAll(listOf("월", "화", "수", "목", "금", "토", "일")) -> 3 // 매일
                        filterVM.selectedDays.containsAll(listOf("월", "화", "수", "목", "금")) && filterVM.selectedDays.size == 5 -> 1 // 평일
                        filterVM.selectedDays.containsAll(listOf("토", "일")) && filterVM.selectedDays.size == 2 -> 2 // 주말
                        else -> 0 // 선택
                    }
                )
            }
        }

    }

    private fun resetAllFilters() {
        Log.d(logTag, "Calling filterVM.resetFilters()")
        // 1. ViewModel의 상태 초기화
        filterVM.resetFilters()

        // 2. UI 요소들을 초기 상태로 업데이트
        //    (기존 onViewCreated의 초기화 로직 재활용)

        // 거리 범위 SeekBar
        val seekBar = view?.findViewById<SeekBar>(R.id.distance_slider)
        val label = view?.findViewById<TextView>(R.id.slider_value_label)
        if (seekBar != null && label != null) {
            val initialDistance = 0 // 초기값 0
            seekBar.progress = initialDistance
            updateSeekBarLabel(seekBar, label, initialDistance)
        }

        // 운영 시간
        val startTimeText = view?.findViewById<TextView>(R.id.start_time_text)
        val endTimeText = view?.findViewById<TextView>(R.id.end_time_text)
        val defaultTimeText = formatApiTimeToDisplayTime(null) // "오전 00 : 00"
        startTimeText?.text = defaultTimeText
        endTimeText?.text = defaultTimeText

        // 시간 범위 Spinner
        val spinner = view?.findViewById<Spinner>(R.id.time_range_spinner)
        spinner?.setSelection(0, false) // 첫 번째 항목("30분") 선택, 리스너 호출 안 함

        // 요일 버튼 및 스피너
        val dayButtons = getDayButtonsMap() // dayButtons Map 가져오기
        if (dayButtons != null) {
            updateDayButtonUI(dayButtons, filterVM.selectedDays) // ViewModel은 이미 clear됨
            val dayGroupSpinner = view?.findViewById<Spinner>(R.id.day_group_spinner)
            dayGroupSpinner?.setSelection(0, false) // "선택" 항목 선택, 리스너 호출 안 함
        }

        // 진료과 RecyclerView
        // 어댑터에 ViewModel의 Set이 연결되어 있으므로, 어댑터에 변경 알림만 주면 됨
        val specialtyRecyclerView = view?.findViewById<RecyclerView>(R.id.specialty_recycler_view)
        (specialtyRecyclerView?.adapter as? SpecialtyAdapter)?.notifyDataSetChanged()

        Toast.makeText(requireContext(), "필터가 초기화되었습니다.", Toast.LENGTH_SHORT).show()
    }

    /** ✅ dayButtons Map을 가져오는 헬퍼 함수 (코드 중복 방지) */
    private fun getDayButtonsMap(): Map<String, TextView>? {
        return view?.let {
            mapOf(
                "월" to it.findViewById<TextView>(R.id.day_mon),
                "화" to it.findViewById<TextView>(R.id.day_tue),
                "수" to it.findViewById<TextView>(R.id.day_wed),
                "목" to it.findViewById<TextView>(R.id.day_thu),
                "금" to it.findViewById<TextView>(R.id.day_fri),
                "토" to it.findViewById<TextView>(R.id.day_sat),
                "일" to it.findViewById<TextView>(R.id.day_sun)
            )
        }
    }

    private fun updateSeekBarLabel(seekBar: SeekBar, label: TextView, progress: Int) {
        label.text = if (progress == 0) "전체" else "${progress}km 이내"
        val max = seekBar.max
        val availableWidth = seekBar.width - seekBar.paddingStart - seekBar.paddingEnd

        // ✅ return@post 를 그냥 return 으로 변경
        if (availableWidth <= 0) return

        val ratio = if (max == 0) 0f else progress.toFloat() / max
        val thumbX = seekBar.paddingStart + ratio * availableWidth
        label.post { // 라벨 너비 계산을 위해 post 유지
            // ✅ 여기의 return@post는 label.post 람다를 가리키므로 그대로 둡니다.
            if (label.width <= 0) return@post
            label.x = seekBar.x + thumbX - label.width / 2f
            label.visibility = View.VISIBLE
        }
    }

    private fun parseDisplayTimeToApiTime(displayTime: String): String? {
        val displayFormat = SimpleDateFormat("a hh : mm", Locale.KOREAN)
        val apiFormat = SimpleDateFormat("HH:mm", Locale.KOREAN)
        return try {
            val date = displayFormat.parse(displayTime)
            if (date != null) apiFormat.format(date) else null
        } catch (e: Exception) {
            null // Parsing failed
        }
    }

    // ✅ Helper to format "HH:mm" to "오전/오후 HH : mm"
    private fun formatApiTimeToDisplayTime(apiTime: String?): String {
        if (apiTime == null) return "오전 00 : 00" // 기본값
        val apiFormat = SimpleDateFormat("HH:mm", Locale.KOREAN)
        val displayFormat = SimpleDateFormat("a hh : mm", Locale.KOREAN)
        return try {
            val date = apiFormat.parse(apiTime)
            if (date != null) displayFormat.format(date) else "오전 00 : 00"
        } catch (e: Exception) {
            "오전 00 : 00" // Formatting failed
        }
    }

    private fun updateDayButtonUI(dayButtons: Map<String, TextView>, selectedDays: Set<String>) {
        for ((day, btn) in dayButtons) {
            btn.setBackgroundResource(
                if (selectedDays.contains(day)) R.drawable.bg_day_selected
                else R.drawable.bg_day_unselected
            )
        }
    }

    private fun <T> MutableSet<T>.setAll(vararg items: T) {
        clear()
        addAll(items)
    }

    override fun onDestroyView() {
        super.onDestroyView()

//        val startTime =
//            parseDisplayTimeToApiTime(view?.findViewById<TextView>(R.id.start_time_text)?.text.toString())
//        val endTime =
//            parseDisplayTimeToApiTime(view?.findViewById<TextView>(R.id.end_time_text)?.text.toString())
//
//        filterVM.startTime = startTime
//        filterVM.endTime = endTime

        val resultBundle = Bundle().apply {
            // ViewModel에 저장된 최신 진료과 목록을 가져와서 전달합니다.
            putStringArrayList("selected_specialties", ArrayList(filterVM.selectedSpecialties))
            putStringArrayList("selected_days", ArrayList(filterVM.selectedDays))
            putString("start_time", filterVM.startTime)
            putString("end_time", filterVM.endTime)
            filterVM.selectedDistance?.let {
                putInt("selected_distance", it)
            }
        }

        // --- 👇 로그 추가 부분 👇 ---
        val logTag = "FilterDebug"
        val days = resultBundle.getStringArrayList("selected_days")
        val start = resultBundle.getString("start_time")
        val end = resultBundle.getString("end_time")

        Log.d(logTag, "Sending filter results:")
        Log.d(logTag, "  Selected Days: $days")
        Log.d(logTag, "  Start Time: $start")
        Log.d(logTag, "  End Time: $end")
        Log.d(logTag, "[onDestroyView] Just updated VM: days=${filterVM.selectedDays}, start=${filterVM.startTime}, end=${filterVM.endTime}, dist=${filterVM.selectedDistance}")
        // ---------------------------

        parentFragmentManager.setFragmentResult("filter_apply_request", resultBundle)
    }
}