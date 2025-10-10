package com.example.carepick.ui.search.filter

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.ui.search.FilterViewModel
import com.example.carepick.ui.search.filter.adapter.SpecialtyAdapter
import java.util.*

class FilterFragment : Fragment() {

    private val filterVM: FilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 상태바 padding 적용
        val toolbar = view.findViewById<View>(R.id.filterHeader)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updatePadding(top = topInset + 12) // 기존 padding 유지
            insets
        }

        // 🩺 진료과 선택
        val specialtyList = listOf(
            "가정의학과", "감염내과", "내분비대사내과", "류마티스내과", "마취통증의학과", "비뇨의학과", "산부인과",
            "성형외과", "소아청소년과", "소화기내과", "순환기내과", "신경과", "신경외과", "신장내과", "안과", "영상의학과",
            "외과", "응급의학과", "이비인후과", "재활의학과", "정신건강의학과", "정형외과", "치과", "피부과", "혈액종양내과",
            "호흡기내과", "흉부외과"
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
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (seekBar != null) {
                    label.text = "${progress}km 이내"
                    val max = seekBar.max
                    val availableWidth = seekBar.width - seekBar.paddingStart - seekBar.paddingEnd
                    val ratio = progress.toFloat() / max
                    val thumbX = seekBar.paddingStart + ratio * availableWidth
                    label.x = seekBar.x + thumbX - label.width / 2f
                    label.visibility = View.VISIBLE
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 🕐 운영시간 설정
        val startTimeText = view.findViewById<TextView>(R.id.start_time_text)
        val startTimeIcon = view.findViewById<ImageView>(R.id.start_time_icon)
        val endTimeText = view.findViewById<TextView>(R.id.end_time_text)
        val endTimeIcon = view.findViewById<ImageView>(R.id.end_time_icon)

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

        startTimeIcon.setOnClickListener { showTimePicker(startTimeText) }
        endTimeIcon.setOnClickListener { showTimePicker(endTimeText) }

        // ⏳ 시간 범위 Spinner 설정
        val spinner = view.findViewById<Spinner>(R.id.time_range_spinner)
        val spinnerIcon = view.findViewById<ImageView>(R.id.spinner_dropdown_icon)
        val timeRanges = listOf("30분", "1시간", "1시간 30분", "2시간", "2시간 30분", "3시간")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeRanges)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinnerIcon.setOnClickListener { spinner.performClick() }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
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
        val selectedDays = mutableSetOf<String>()
        val dayGroupSpinner = view.findViewById<Spinner>(R.id.day_group_spinner)
        val dayGroupIcon = view.findViewById<ImageView>(R.id.day_group_icon)
        val dayGroupOptions = listOf("선택", "평일", "주말", "매일")
        val groupAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dayGroupOptions)
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dayGroupSpinner.adapter = groupAdapter
        dayGroupIcon.setOnClickListener { dayGroupSpinner.performClick() }

        dayGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (dayGroupOptions[position]) {
                    "선택" -> selectedDays.clear()
                    "평일" -> selectedDays.setAll("월", "화", "수", "목", "금")
                    "주말" -> selectedDays.setAll("토", "일")
                    "매일" -> selectedDays.setAll(*dayButtons.keys.toTypedArray())
                }
                updateDayButtonUI(dayButtons, selectedDays)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        for ((day, btn) in dayButtons) {
            btn.setOnClickListener {
                if (selectedDays.contains(day)) selectedDays.remove(day) else selectedDays.add(day)
                updateDayButtonUI(dayButtons, selectedDays)
                dayGroupSpinner.setSelection(
                    when {
                        selectedDays.containsAll(listOf("월", "화", "수", "목", "금", "토", "일")) -> 3
                        selectedDays.containsAll(listOf("월", "화", "수", "목", "금")) && selectedDays.size == 5 -> 1
                        selectedDays.containsAll(listOf("토", "일")) && selectedDays.size == 2 -> 2
                        else -> 0
                    }
                )
            }
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

        val resultBundle = Bundle().apply {
            // ViewModel에 저장된 최신 진료과 목록을 가져와서 전달합니다.
            putStringArrayList("selected_specialties", ArrayList(filterVM.selectedSpecialties))
        }
        parentFragmentManager.setFragmentResult("filter_apply_request", resultBundle)
    }
}