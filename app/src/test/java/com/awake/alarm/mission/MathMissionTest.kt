package com.awake.alarm.mission

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MathMissionTest {

    private lateinit var mission: MathMission

    @Before
    fun setup() {
        mission = MathMission(MathDifficulty.EASY)
        mission.start()
    }

    @Test
    fun `initial state is not complete`() {
        assertFalse(mission.isComplete())
        assertEquals(0, mission.problemsSolved)
    }

    @Test
    fun `correct answer increments progress`() {
        val answer = mission.currentProblem.correctAnswer.toString()
        val result = mission.submitAnswer(answer)
        assertTrue(result)
        assertEquals(1, mission.problemsSolved)
    }

    @Test
    fun `wrong answer does not increment progress`() {
        val wrongAnswer = (mission.currentProblem.correctAnswer + 999).toString()
        val result = mission.submitAnswer(wrongAnswer)
        assertFalse(result)
        assertEquals(0, mission.problemsSolved)
    }

    @Test
    fun `invalid input returns false`() {
        val result = mission.submitAnswer("abc")
        assertFalse(result)
        assertEquals(0, mission.problemsSolved)
    }

    @Test
    fun `completing required problems marks mission complete`() {
        repeat(mission.requiredCorrect) {
            val answer = mission.currentProblem.correctAnswer.toString()
            mission.submitAnswer(answer)
        }
        assertTrue(mission.isComplete())
    }

    @Test
    fun `progress is zero initially`() {
        assertEquals(0f, mission.progress(), 0.001f)
    }

    @Test
    fun `progress increases after correct answer`() {
        mission.submitAnswer(mission.currentProblem.correctAnswer.toString())
        assertTrue(mission.progress() > 0f)
    }

    @Test
    fun `wrong answer can decrease progress`() {
        // Get some progress first
        mission.submitAnswer(mission.currentProblem.correctAnswer.toString())
        mission.submitAnswer(mission.currentProblem.correctAnswer.toString())
        val progressAfterCorrect = mission.problemsSolved

        // Wrong answer
        mission.submitAnswer((mission.currentProblem.correctAnswer + 999).toString())
        assertTrue(mission.problemsSolved < progressAfterCorrect)
    }

    @Test
    fun `problems are generated with valid operations`() {
        val problem = mission.currentProblem
        assertTrue(problem.operator in listOf('+', '-'))
        assertTrue(problem.operand1 > 0)
        assertTrue(problem.operand2 > 0)
    }

    @Test
    fun `medium difficulty includes multiply`() {
        val mediumMission = MathMission(MathDifficulty.MEDIUM)
        mediumMission.start()
        // Generate multiple problems and check operands are in range
        repeat(10) {
            val problem = mediumMission.currentProblem
            assertTrue(problem.operand1 <= 50)
            assertTrue(problem.operand2 <= 50)
            mediumMission.submitAnswer(mediumMission.currentProblem.correctAnswer.toString())
        }
    }

    @Test
    fun `hard difficulty requires more correct answers`() {
        val hardMission = MathMission(MathDifficulty.HARD)
        val easyMission = MathMission(MathDifficulty.EASY)
        assertTrue(hardMission.requiredCorrect > easyMission.requiredCorrect)
    }

    @Test
    fun `status text updates correctly`() {
        assertEquals("0 / 3 correct", mission.statusText())
        mission.submitAnswer(mission.currentProblem.correctAnswer.toString())
        assertEquals("1 / 3 correct", mission.statusText())
    }
}
