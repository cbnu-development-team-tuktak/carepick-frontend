package com.example.carepick.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LatLng(
    val latitude: Double,
    val longitude: Double
) : Parcelable