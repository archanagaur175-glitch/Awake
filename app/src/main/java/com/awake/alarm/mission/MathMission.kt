package com.awake.alarm.mission

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awake.alarm.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

enum class MathDifficulty(val operations: List<Char>, val maxOperand: Int, val requiredCorrect: Int) {
    EASY(listOf('+', '-'), 20, 3),
    MEDIUM(listOf('+', '-'), 50, 5),
    HARD(listOf('+', '-', '*'), 100, 7),
    EXTREME(listOf('+', '-', '*'), 500, 10)
}

data class MathProblem(
    val operand1: Int,
    val operand2: Int,
    val operator: Char,
    val correctAnswer: Int
)

class MathMission(private val difficulty: MathDifficulty = MathDifficulty.MEDIUM) : WakeMission {

    private var _complete = false
    private var _problemsSolved = 0
    private var _currentProblem = generateProblem()
    private var _lastAnswerCorrect: Boolean? = null
    private var _started = false

    val problemsSolved: Int get() = _problemsSolved
    val currentProblem: MathProblem get() = _currentProblem
    val lastAnswerCorrect: Boolean? get() = _lastAnswerCorrect
    val requiredCorrect: Int get() = difficulty.requiredCorrect

    private fun generateProblem(): MathProblem {
        val op = difficulty.operations.random()
        val max = difficulty.maxOperand
        val a = Random.nextInt(1, max)
        val b = Random.nextInt(1, max)

        return when (op) {
            '+' -> MathProblem(a, b, op, a + b)
            '-' -> {
                val (big, small) = if (a >= b) a to b else b to a
                MathProblem(big, small, op, big - small)
            }
            '*' -> MathProblem(a, b, op, a * b)
            else -> MathProblem(a, b, op, a + b)
        }
    }

    override fun start() {
        _started = true
        _complete = false
        _problemsSolved = 0
        _currentProblem = generateProblem()
        _lastAnswerCorrect = null
    }

    override fun isComplete(): Boolean = _complete

    override fun onDismiss() { /* no-op for math */ }

    override fun progress(): Float = _problemsSolved.toFloat() / difficulty.requiredCorrect

    override fun statusText(): String =
        "$_problemsSolved / ${difficulty.requiredCorrect} correct"

    fun submitAnswer(answer: String): Boolean {
        val userAnswer = answer.toIntOrNull() ?: return false
        val correct = userAnswer == _currentProblem.correctAnswer

        _lastAnswerCorrect = correct

        if (correct) {
            _problemsSolved++
            if (_problemsSolved >= difficulty.requiredCorrect) {
                _complete = true
            } else {
                _currentProblem = generateProblem()
            }
        } else {
            // Wrong answer resets progress by 1 (minimum 0)
            _problemsSolved = (_problemsSolved - 1).coerceAtLeast(0)
            _currentProblem = generateProblem()
        }

        return correct
    }

    @Composable
    override fun render() {
        val haptic = LocalHapticFeedback.current
        var userInput by remember { mutableStateOf("") }
        var showFeedback by remember { mutableStateOf(false) }
        var wasCorrect by remember { mutableStateOf(false) }

        val progressAnim by animateFloatAsState(
            targetValue = progress(),
            animationSpec = tween(500, easing = FastOutSlowInEasing),
            label = "progress"
        )

        val feedbackScale by animateFloatAsState(
            targetValue = if (showFeedback) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
            label = "feedback"
        )

        LaunchedEffect(_lastAnswerCorrect) {
            if (_lastAnswerCorrect != null) {
                wasCorrect = _lastAnswerCorrect == true
                showFeedback = true
                delay(800)
                showFeedback = false
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "Solve to dismiss",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DarkSurfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressAnim)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MathAccent)
                )
            }

            Text(
                text = statusText(),
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Problem display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${currentProblem.operand1} ${currentProblem.operator} ${currentProblem.operand2}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "=",
                        fontSize = 32.sp,
                        color = TextTertiary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Answer input
            OutlinedTextField(
                value = userInput,
                onValueChange = { newUserInput ->
                    // Only allow digits and optional negative sign
                    if (newUserInput.isEmpty() || newUserInput.matches(Regex("^-?\\d*$"))) {
                        userInput = newUserInput
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (userInput.isNotBlank()) {
                            val correct = submitAnswer(userInput)
                            haptic.performHapticFeedback(
                                if (correct) HapticFeedbackType.LongPress
                                else HapticFeedbackType.TextHandleMove
                            )
                            userInput = ""
                        }
                    }
                ),
                placeholder = {
                    Text("Your answer", color = TextTertiary)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MathAccent,
                    unfocusedBorderColor = TextTertiary.copy(alpha = 0.3f),
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit button
            Button(
                onClick = {
                    if (userInput.isNotBlank()) {
                        val correct = submitAnswer(userInput)
                        haptic.performHapticFeedback(
                            if (correct) HapticFeedbackType.LongPress
                            else HapticFeedbackType.TextHandleMove
                        )
                        userInput = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MathAccent
                ),
                enabled = userInput.isNotBlank() && !_complete
            ) {
                Text(
                    text = if (_complete) "Complete!" else "Submit",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Feedback overlay
            AnimatedVisibility(
                visible = showFeedback,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(feedbackScale)
                        .clip(CircleShape)
                        .background(if (wasCorrect) SuccessGreen else AlarmActive),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (wasCorrect) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}
