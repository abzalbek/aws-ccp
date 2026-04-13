package com.example.hiroad_aws.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hiroad_aws.data.ChoiceQuestion
import com.example.hiroad_aws.data.MatchingQuestion
import com.example.hiroad_aws.data.QuizItem
import com.example.hiroad_aws.data.QuizRepository
import com.example.hiroad_aws.ui.theme.HiRoad_AWSTheme

private fun canSubmitChoice(correctIndices: Set<Int>, selected: Set<Int>): Boolean =
    selected.size == correctIndices.size && selected.isNotEmpty()

private fun canSubmitMatching(selections: List<Int?>): Boolean =
    selections.size > 0 && selections.all { it != null }

@Composable
fun QuizScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var questions by remember { mutableStateOf<List<QuizItem>?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var roundOrder by remember { mutableStateOf<List<QuizItem>>(emptyList()) }
    var roundIndex by remember { mutableIntStateOf(0) }
    var selectedIndices by remember { mutableStateOf(emptySet<Int>()) }
    var revealed by remember { mutableStateOf(false) }

    val current: QuizItem? =
        roundOrder.getOrNull(roundIndex)

    LaunchedEffect(Unit) {
        try {
            val list = QuizRepository.loadQuestions(context)
            questions = list
            val shuffled = list.shuffled()
            roundOrder = shuffled
            roundIndex = 0
        } catch (e: Exception) {
            loadError = e.message ?: e.javaClass.simpleName
        }
    }

    BackHandler(
        enabled = questions != null && loadError == null && roundOrder.isNotEmpty(),
    ) {
        if (roundIndex > 0) {
            roundIndex--
            revealed = false
            selectedIndices = emptySet()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "AWS Certified Cloud Practitioner",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (roundOrder.isNotEmpty()) {
            Text(
                text = "Question ${roundIndex + 1} of ${roundOrder.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when {
            loadError != null -> {
                Text(
                    text = "Could not load questions: $loadError",
                    color = MaterialTheme.colorScheme.error,
                )
            }
            questions == null -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            current == null -> {
                Text("No questions in the bank yet. Add entries to questions.json.")
            }
            else -> {
                val currentItem = requireNotNull(current)
                when (currentItem) {
                    is ChoiceQuestion -> ChoiceQuestionContent(
                        q = currentItem,
                        roundIndex = roundIndex,
                        roundOrder = roundOrder,
                        selectedIndices = selectedIndices,
                        onSelectedIndicesChange = { selectedIndices = it },
                        revealed = revealed,
                        onReveal = { revealed = true },
                        onNext = {
                            val bank = questions ?: return@ChoiceQuestionContent
                            if (bank.isEmpty()) return@ChoiceQuestionContent
                            revealed = false
                            selectedIndices = emptySet()
                            val next = roundIndex + 1
                            if (next >= roundOrder.size) {
                                roundOrder = bank.shuffled()
                                roundIndex = 0
                            } else {
                                roundIndex = next
                            }
                        },
                    )
                    is MatchingQuestion -> MatchingQuestionContent(
                        mq = currentItem,
                        roundIndex = roundIndex,
                        roundOrder = roundOrder,
                        revealed = revealed,
                        onReveal = { revealed = true },
                        onNext = {
                            val bank = questions ?: return@MatchingQuestionContent
                            if (bank.isEmpty()) return@MatchingQuestionContent
                            revealed = false
                            val next = roundIndex + 1
                            if (next >= roundOrder.size) {
                                roundOrder = bank.shuffled()
                                roundIndex = 0
                            } else {
                                roundIndex = next
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoiceQuestionContent(
    q: ChoiceQuestion,
    roundIndex: Int,
    roundOrder: List<QuizItem>,
    selectedIndices: Set<Int>,
    onSelectedIndicesChange: (Set<Int>) -> Unit,
    revealed: Boolean,
    onReveal: () -> Unit,
    onNext: () -> Unit,
) {
    val optionPermutation = remember(q.id, roundIndex, roundOrder) {
        q.options.indices.shuffled()
    }
    val displayCorrectIndices = optionPermutation.indices
        .filter { optionPermutation[it] in q.correctIndices }
        .toSet()
    val isMultiSelect = displayCorrectIndices.size > 1

    Text(
        text = q.question,
        style = MaterialTheme.typography.bodyLarge,
    )
    Spacer(modifier = Modifier.height(4.dp))

    optionPermutation.forEachIndexed { displayPos, originalIndex ->
        val option = q.options[originalIndex]
        val isSelected = displayPos in selectedIndices
        val showResult = revealed
        val isCorrect = displayPos in displayCorrectIndices
        val containerColor = when {
            !showResult -> MaterialTheme.colorScheme.surfaceContainerHighest
            isCorrect -> MaterialTheme.colorScheme.primaryContainer
            isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        }
        val role = if (isMultiSelect) Role.Checkbox else Role.RadioButton
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = isSelected,
                    enabled = !revealed,
                    role = role,
                ) {
                    if (isMultiSelect) {
                        onSelectedIndicesChange(
                            if (displayPos in selectedIndices) selectedIndices - displayPos
                            else selectedIndices + displayPos,
                        )
                    } else {
                        onSelectedIndicesChange(setOf(displayPos))
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isMultiSelect) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                        enabled = !revealed,
                    )
                } else {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        enabled = !revealed,
                    )
                }
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                if (showResult) {
                    when {
                        isCorrect -> Text(
                            text = "Correct",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                        isSelected && !isCorrect -> Text(
                            text = "Incorrect",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onReveal,
            enabled = canSubmitChoice(displayCorrectIndices, selectedIndices) && !revealed,
            modifier = Modifier.weight(1f),
        ) {
            Text("Check answer")
        }
        OutlinedButton(
            onClick = onNext,
            modifier = Modifier.weight(1f),
        ) {
            Text("Next")
        }
    }
}

@Composable
private fun MatchingQuestionContent(
    mq: MatchingQuestion,
    roundIndex: Int,
    roundOrder: List<QuizItem>,
    revealed: Boolean,
    onReveal: () -> Unit,
    onNext: () -> Unit,
) {
    val definitionOrder = remember(mq.id, roundIndex, roundOrder) {
        mq.pairs.indices.shuffled()
    }
    val matchSelections = remember(mq.id, roundIndex, roundOrder) {
        mutableStateListOf<Int?>().apply { repeat(mq.pairs.size) { add(null) } }
    }

    Text(
        text = mq.question,
        style = MaterialTheme.typography.bodyLarge,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "For each service, choose the matching definition.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))

    mq.pairs.forEachIndexed { termIndex, pair ->
        var expanded by remember(mq.id, roundIndex, termIndex, roundOrder) { mutableStateOf(false) }
        val selectedDefIndex = matchSelections[termIndex]
        val rowCorrect = selectedDefIndex != null && selectedDefIndex == termIndex
        val showResult = revealed
        val containerColor = when {
            !showResult -> MaterialTheme.colorScheme.surfaceContainerHighest
            rowCorrect -> MaterialTheme.colorScheme.primaryContainer
            selectedDefIndex != null && !rowCorrect -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = pair.term,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Box {
                    OutlinedButton(
                        onClick = { if (!revealed) expanded = true },
                        enabled = !revealed,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = when (selectedDefIndex) {
                                null -> "Select definition…"
                                else -> mq.pairs[selectedDefIndex].definition
                            },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    DropdownMenu(
                        expanded = expanded && !revealed,
                        onDismissRequest = { expanded = false },
                    ) {
                        definitionOrder.forEach { defIndex ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        mq.pairs[defIndex].definition,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                },
                                onClick = {
                                    matchSelections[termIndex] = defIndex
                                    expanded = false
                                },
                            )
                        }
                    }
                }
                if (showResult) {
                    Text(
                        text = if (rowCorrect) "Correct" else "Incorrect",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (rowCorrect) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onReveal,
            enabled = canSubmitMatching(matchSelections.toList()) && !revealed,
            modifier = Modifier.weight(1f),
        ) {
            Text("Check answer")
        }
        OutlinedButton(
            onClick = onNext,
            modifier = Modifier.weight(1f),
        ) {
            Text("Next")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizScreenPreview() {
    HiRoad_AWSTheme {
        QuizScreen()
    }
}
