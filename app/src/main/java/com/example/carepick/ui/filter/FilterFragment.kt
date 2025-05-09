package com.example.carepick.ui.filter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.carepick.R
import com.google.android.material.slider.Slider
import android.widget.TextView

class FilterFragment : Fragment(R.layout.fragment_filter) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val slider = view.findViewById<Slider>(R.id.distance_slider)
        slider.setLabelFormatter { value: Float ->
            "${value.toInt()}km 이내"
        }
    }
}