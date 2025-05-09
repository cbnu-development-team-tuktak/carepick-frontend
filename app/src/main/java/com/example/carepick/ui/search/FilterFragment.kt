package com.example.carepick.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.carepick.R

class FilterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 우리가 만든 필터 화면 XML을 inflate 합니다
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로가기 버튼이 있다면 클릭 시 이전 화면으로 돌아가기
        val backButton = view.findViewById<View>(R.id.btn_back) // ← back 버튼 ID가 있는 경우
        backButton?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 이후에 슬라이더나 버튼, 초기화 등 추가 동작 연결하면 됨

        // SeekBar의 thumb 위치에 따라 텍스트가 따라다니도록 구현
        val seekBar = view.findViewById<SeekBar>(R.id.distance_slider)
        val label = view.findViewById<TextView>(R.id.slider_value_label)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (seekBar != null) {
                    label.text = "${progress}km 이내"

                    // 위치 계산
                    val max = seekBar.max
                    val thumbOffset = seekBar.thumbOffset
                    val availableWidth = seekBar.width - seekBar.paddingStart - seekBar.paddingEnd
                    val ratio = progress.toFloat() / max
                    val thumbX = seekBar.paddingStart + ratio * availableWidth

                    // TextView 위치 조정
                    label.x = seekBar.x + thumbX - label.width / 2f
                    label.visibility = View.VISIBLE
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}