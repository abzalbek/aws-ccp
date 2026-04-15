package com.example.hiroad_aws.data

import android.content.Context
import org.json.JSONObject

object QuizRepository {

    private const val ASSET_FILE = "questions.json"

    fun loadQuestions(context: Context): List<QuizItem> {
        val json = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val array = root.getJSONArray("questions")
        return buildList {
            for (i in 0 until array.length()) {
                add(parseQuestion(array.getJSONObject(i)))
            }
        }
    }

    private fun parseQuestion(o: JSONObject): QuizItem {
        val id = o.getString("id")
        val question = o.getString("question")
        val module = o.optString("module", "")
        val isMatching = o.optString("type", "") == "matching" || o.has("matchingPairs")
        if (isMatching) {
            val arr = o.getJSONArray("matchingPairs")
            val pairs = List(arr.length()) { idx ->
                val p = arr.getJSONObject(idx)
                MatchingPair(
                    term = p.getString("term"),
                    definition = p.getString("definition"),
                )
            }
            require(pairs.isNotEmpty()) { "matchingPairs must not be empty" }
            return MatchingQuestion(id = id, question = question, module = module, pairs = pairs)
        }
        val opts = o.getJSONArray("options")
        val options = List(opts.length()) { idx -> opts.getString(idx) }
        val correct = parseCorrectIndices(o)
        require(correct.isNotEmpty()) { "Question must have correctIndex or correctIndices" }
        return ChoiceQuestion(
            id = id,
            question = question,
            module = module,
            options = options,
            correctIndices = correct,
        )
    }

    private fun parseCorrectIndices(o: JSONObject): Set<Int> {
        return when {
            o.has("correctIndices") -> {
                val arr = o.getJSONArray("correctIndices")
                buildSet {
                    for (j in 0 until arr.length()) add(arr.getInt(j))
                }
            }
            o.has("correctIndex") -> setOf(o.getInt("correctIndex"))
            else -> emptySet()
        }
    }
}
