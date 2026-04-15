package com.example.hiroad_aws.data

sealed interface QuizItem {
    val id: String
    val question: String
    val module: String
}

data class ChoiceQuestion(
    override val id: String,
    override val question: String,
    override val module: String,
    val options: List<String>,
    val correctIndices: Set<Int>,
) : QuizItem {
    val isMultiSelect: Boolean get() = correctIndices.size > 1
}

data class MatchingQuestion(
    override val id: String,
    override val question: String,
    override val module: String,
    val pairs: List<MatchingPair>,
) : QuizItem

data class MatchingPair(
    val term: String,
    val definition: String,
)
