package com.example.carepick.ui.search

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
import java.util.*

class FilterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔙 뒤로가기 버튼
        val backButton = view.findViewById<View>(R.id.btn_back)
        backButton?.setOnClickListener {
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

        startTimeIcon.setOnClickListener {
            showTimePicker(startTimeText)
        }

        endTimeIcon.setOnClickListener {
            showTimePicker(endTimeText)
        }

        // ⏳ 시간 범위 Spinner 설정
        val spinner = view.findViewById<Spinner>(R.id.time_range_spinner)
        val spinnerIcon = view.findViewById<ImageView>(R.id.spinner_dropdown_icon)
        val timeRanges = listOf("30분", "1시간", "1시간 30분", "2시간", "2시간 30분", "3시간")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeRanges)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinnerIcon.setOnClickListener {
            spinner.performClick()
        }

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

        dayGroupIcon.setOnClickListener {
            dayGroupSpinner.performClick()
        }

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

        // ✅ 🔍 질병 검색 기능
        val searchInput = view.findViewById<EditText>(R.id.search_input)
        val suggestionBox = view.findViewById<LinearLayout>(R.id.suggestion_box)
        val suggestion1 = view.findViewById<TextView>(R.id.suggestion_1)
        val suggestion2 = view.findViewById<TextView>(R.id.suggestion_2)
        val suggestion3 = view.findViewById<TextView>(R.id.suggestion_3)
        val selectedTagsContainer = view.findViewById<LinearLayout>(R.id.selected_tags_container)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    suggestion1.text = "감기"
                    suggestion2.text = "기흉"
                    suggestion3.text = "각막염"
                    suggestion1.visibility = View.VISIBLE
                    suggestion2.visibility = View.VISIBLE
                    suggestion3.visibility = View.VISIBLE
                    suggestionBox.visibility = View.VISIBLE
                } else {
                    suggestion1.visibility = View.GONE
                    suggestion2.visibility = View.GONE
                    suggestion3.visibility = View.GONE
                    suggestionBox.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ 태그 추가 기능
        suggestion1.setOnClickListener { addTag("감기") }
        suggestion2.setOnClickListener { addTag("기흉") }
        suggestion3.setOnClickListener { addTag("각막염") }
    }

    private fun updateDayButtonUI(
        dayButtons: Map<String, TextView>,
        selectedDays: Set<String>
    ) {
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

    private fun addTag(tagText: String) {
        val tagContainer = view?.findViewById<LinearLayout>(R.id.selected_tags_container) ?: return

        val tagView = LayoutInflater.from(context).inflate(R.layout.tag_item, tagContainer, false)
        val textView = tagView.findViewById<TextView>(R.id.tag_text)
        val closeBtn = tagView.findViewById<ImageView>(R.id.tag_close)

        textView.text = tagText
        closeBtn.setOnClickListener {
            tagContainer.removeView(tagView)
        }

        tagContainer.addView(tagView)
    }
}