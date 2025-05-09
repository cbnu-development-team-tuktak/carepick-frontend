package com.example.carepick.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.carepick.R

class LocationSettingFragment : Fragment() {

    private lateinit var gpsTab: TextView
    private lateinit var areaTab: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gpsTab = view.findViewById(R.id.btnGps)
        areaTab = view.findViewById(R.id.btnArea)

        gpsTab.setOnClickListener {
            selectTab(isGps = true)
        }

        areaTab.setOnClickListener {
            selectTab(isGps = false)
        }

        selectTab(isGps = false) // 기본: 읍면동 설정
    }

    private fun selectTab(isGps: Boolean) {
        if (isGps) {
            gpsTab.setBackgroundResource(R.drawable.bg_tab_selected)
            areaTab.setBackgroundResource(R.drawable.bg_tab_unselected)
        } else {
            gpsTab.setBackgroundResource(R.drawable.bg_tab_unselected)
            areaTab.setBackgroundResource(R.drawable.bg_tab_selected)
        }
    }
}