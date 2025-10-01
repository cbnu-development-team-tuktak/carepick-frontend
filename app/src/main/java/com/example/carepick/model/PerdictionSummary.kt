package com.example.carepick.model

data class TopItem(
    val name: String,
    val score: Double
)

data class PredictionSummary(
    val diseases: List<TopItem>,
    val specialties: List<TopItem>
)

/**
 * 백엔드 message 예:
 *  "입력 문장: ...\n예측된 질병 Top-3:\n- 알레르기 비염 (0.1106)\n- 감기 (0.069)\n- 비염 (0.0626)\n"
 *  "입력 문장: ...\n예측된 진료과 Top-3\n- 이비인후과 (0.6613)\n- 외과 (0.0911)\n- 소아청소년과 (0.0723)\n"
 *
 * -> '- '로 시작하는 라인에서 "이름 (점수)" 파싱
 */
fun parseTopKFromMessage(message: String): List<TopItem> {
    val result = mutableListOf<TopItem>()
    val regex = Regex("""^-+\s*(.+?)\s*\(([-+]?([0-9]*[.])?[0-9]+)\)\s*$""")
    message.lines().forEach { raw ->
        val line = raw.trim()
        val m = regex.find(line) ?: return@forEach
        val name = m.groupValues[1].trim()
        val scoreStr = m.groupValues[2].trim()
        val score = scoreStr.toDoubleOrNull() ?: return@forEach
        result += TopItem(name = name, score = score)
    }
    return result
}