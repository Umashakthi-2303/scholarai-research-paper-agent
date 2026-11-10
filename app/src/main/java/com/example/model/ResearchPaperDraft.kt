package com.example.model

import java.util.UUID

data class ResearchPaperDraft(
    val id: String = UUID.randomUUID().toString(),
    val topic: String,
    val academicTitle: String,
    val abstractText: String,
    val keywords: List<String>,
    val introduction: String,
    val literatureReviewGuidance: String,
    val researchObjectives: List<String>,
    val methodologySuggestions: String,
    val expectedResultsAndDiscussion: String,
    val conclusion: String,
    val academicIntegrityNotice: String = "Academic Integrity Guarantee: Citations, data sources, and empirical findings are not fabricated. Use this structured draft to guide your original empirical investigation and literature search.",
    val academicLevel: String = "Undergraduate",
    val discipline: String = "Interdisciplinary",
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalWordCount: Int
        get() {
            val allText = "$academicTitle $abstractText $introduction $literatureReviewGuidance ${researchObjectives.joinToString(" ")} $methodologySuggestions $expectedResultsAndDiscussion $conclusion"
            return allText.split("\\s+".toRegex()).count { it.isNotBlank() }
        }

    val estimatedReadingTimeMinutes: Int
        get() = maxOf(1, totalWordCount / 200)

    fun toFormattedPlainText(): String {
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(createdAt))
        val sb = StringBuilder()
        sb.append("========================================================================\n")
        sb.append("                       ACADEMIC RESEARCH PAPER DRAFT                    \n")
        sb.append("========================================================================\n\n")
        sb.append("TITLE: $academicTitle\n\n")
        sb.append("TOPIC: $topic\n")
        sb.append("ACADEMIC LEVEL: $academicLevel | DISCIPLINE: $discipline\n")
        sb.append("GENERATED DATE: $dateStr\n")
        sb.append("ESTIMATED WORDS: $totalWordCount words (~$estimatedReadingTimeMinutes min read)\n\n")
        sb.append("------------------------------------------------------------------------\n")
        sb.append("                                ABSTRACT                                \n")
        sb.append("------------------------------------------------------------------------\n")
        sb.append("$abstractText\n\n")
        sb.append("KEYWORDS: ${keywords.joinToString("; ")}\n\n")
        sb.append("------------------------------------------------------------------------\n")
        sb.append("1. INTRODUCTION                                                         \n")
        sb.append("------------------------------------------------------------------------\n")
        sb.append("$introduction\n\n")
        sb.append("------------------------------------------------------------------------\n")
        sb.append("2. LITERATURE REVIEW GUIDANCE & THEORETICAL FRAMEWORK                   \n")
        sb.append("------------------------------------------------------------------------\n")
        sb.append("$literatureReviewGuidance\n\n")
        sb.append("------------------------------------------------------------------------\n")
        sb.append("3. RESEARCH OBJECTIVES & QUESTIONS                                      \n")
        sb.append("------------------------------------------------------------------------\n")
        researchObjectives.forEachIndexed { index, obj ->
            sb.append("  [RO-${index + 1}] $obj\n")
        }
        sb.append("\n")
        sb.append("------------------------------------------------------------------------\n")
        sb.append("4. METHODOLOGY SUGGESTIONS & RESEARCH DESIGN                            \n")
        sb.append("------------------------------------------------------------------------\n")
        sb.append("$methodologySuggestions\n\n")
        sb.append("------------------------------------------------------------------------\n")
        sb.append("5. EXPECTED RESULTS & DISCUSSION FRAMEWORK                              \n")
        sb.append("------------------------------------------------------------------------\n")
        sb.append("$expectedResultsAndDiscussion\n\n")
        sb.append("------------------------------------------------------------------------\n")
        sb.append("6. CONCLUSION & FUTURE DIRECTIONS                                       \n")
        sb.append("------------------------------------------------------------------------\n")
        sb.append("$conclusion\n\n")
        sb.append("========================================================================\n")
        sb.append("ACADEMIC INTEGRITY NOTICE:\n")
        sb.append("$academicIntegrityNotice\n")
        sb.append("========================================================================\n")
        return sb.toString()
    }
}
