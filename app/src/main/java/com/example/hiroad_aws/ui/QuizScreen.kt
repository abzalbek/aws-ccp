package com.example.hiroad_aws.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hiroad_aws.data.ChoiceQuestion
import com.example.hiroad_aws.data.MatchingQuestion
import com.example.hiroad_aws.data.QuizItem
import com.example.hiroad_aws.data.QuizModules
import com.example.hiroad_aws.data.QuizRepository
import com.example.hiroad_aws.ui.theme.HiRoad_AWSTheme

private fun canSubmitChoice(correctIndices: Set<Int>, selected: Set<Int>): Boolean =
    selected.size == correctIndices.size && selected.isNotEmpty()

private fun canSubmitMatching(selections: List<Int?>): Boolean =
    selections.size > 0 && selections.all { it != null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    modifier: Modifier = Modifier,
    /** `null` means include every question. */
    moduleFilter: String? = null,
    onExitToHome: () -> Unit = {},
) {
    val context = LocalContext.current
    var questions by remember { mutableStateOf<List<QuizItem>?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var roundOrder by remember { mutableStateOf<List<QuizItem>>(emptyList()) }
    var roundIndex by remember { mutableIntStateOf(0) }
    var selectedIndices by remember { mutableStateOf(emptySet<Int>()) }
    var revealed by remember { mutableStateOf(false) }

    val current: QuizItem? =
        roundOrder.getOrNull(roundIndex)

    val subtitle = when {
        moduleFilter.isNullOrBlank() -> QuizModules.ALL_QUESTIONS
        else -> moduleFilter
    }

    fun filteredBank(loaded: List<QuizItem>): List<QuizItem> =
        if (moduleFilter.isNullOrBlank()) loaded
        else loaded.filter { it.module == moduleFilter }

    LaunchedEffect(Unit) {
        try {
            val list = QuizRepository.loadQuestions(context)
            questions = list
        } catch (e: Exception) {
            loadError = e.message ?: e.javaClass.simpleName
        }
    }

    LaunchedEffect(questions, moduleFilter) {
        val loaded = questions ?: return@LaunchedEffect
        val bank = filteredBank(loaded)
        if (bank.isEmpty()) {
            roundOrder = emptyList()
            roundIndex = 0
            revealed = false
            selectedIndices = emptySet()
            return@LaunchedEffect
        }
        roundOrder = bank.shuffled()
        roundIndex = 0
        revealed = false
        selectedIndices = emptySet()
    }

    BackHandler(enabled = questions != null && loadError == null) {
        if (roundIndex > 0 && roundOrder.isNotEmpty()) {
            roundIndex--
            revealed = false
            selectedIndices = emptySet()
        } else {
            onExitToHome()
        }
    }

    val quizBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        titleContentColor = MaterialTheme.colorScheme.inverseOnSurface,
        scrolledContainerColor = MaterialTheme.colorScheme.inverseSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.primary,
        actionIconContentColor = MaterialTheme.colorScheme.primary,
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onExitToHome) {
                            Text(
                                text = "Home",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    colors = quizBarColors,
                )
                HorizontalDivider(
                    thickness = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (roundOrder.isNotEmpty()) {
            val progress = (roundIndex + 1).toFloat() / roundOrder.size.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                strokeCap = StrokeCap.Round,
            )
            Text(
                text = "Question ${roundIndex + 1} of ${roundOrder.size}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
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
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            current == null -> {
                val loaded = questions ?: emptyList()
                val bank = filteredBank(loaded)
                when {
                    loaded.isEmpty() -> {
                        Text("No questions in the bank yet. Add entries to questions.json.")
                    }
                    bank.isEmpty() -> {
                        Text(
                            "No questions for this module yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    else -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
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
                            val loaded = questions ?: return@ChoiceQuestionContent
                            val bank = filteredBank(loaded)
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
                            val loaded = questions ?: return@MatchingQuestionContent
                            val bank = filteredBank(loaded)
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
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
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
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
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
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                } else {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        enabled = !revealed,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
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
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("Check answer")
        }
        OutlinedButton(
            onClick = onNext,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
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
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
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
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
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
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(8.dp),
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
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("Check answer")
        }
        OutlinedButton(
            onClick = onNext,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
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
