package com.example.carepick.utils

fun String.cleanHospitalName(): String {
    return this.replace(Regex("""^["'(【\[].*?["')】\]]\s*"""), "")
}
