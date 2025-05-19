package com.example.carepick.dto.hospital

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HospitalOperatingHours(
    val day: String,
    val startTime: String?,
    val endTime: String?
) : Parcelable