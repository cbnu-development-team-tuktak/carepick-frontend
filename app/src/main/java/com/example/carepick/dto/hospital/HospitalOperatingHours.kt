package com.example.carepick.dto.hospital

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HospitalOperatingHours(
    val first: String,
    val second: String
) : Parcelable