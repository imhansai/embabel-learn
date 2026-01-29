package dev.fromnowon.embabellearn.demo

import com.embabel.agent.domain.io.UserInput
import com.embabel.agent.test.unit.FakeOperationContext
import com.embabel.agent.test.unit.FakePromptRunner
import dev.fromnowon.embabellearn.demo.agent.Story
import dev.fromnowon.embabellearn.demo.agent.WriteAndReviewAgent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for the WriteAndReviewAgent class.
 * Tests the agent's ability to craft and review stories based on user input.
 */
internal class WriteAndReviewAgentTest {

    /**
     * Tests the story crafting functionality of the WriteAndReviewAgent.
     * Verifies that the LLM call contains expected content and configuration.
     */
    @Test
    fun testCraftStory() {
        // Create agent with word limits: 200 min, 400 max
        val agent = WriteAndReviewAgent(200, 400)
        val context = FakeOperationContext.create()
        val promptRunner = context.promptRunner() as FakePromptRunner

        context.expectResponse(Story("One upon a time Sir Galahad . . "))

        agent.craftStory(
            UserInput("Tell me a story about a brave knight", Instant.now()),
            context
        )

        // Verify the prompt contains the expected keyword
        Assertions.assertTrue(
            promptRunner.llmInvocations.first().prompt.contains("knight"),
            "Expected prompt to contain 'knight'"
        )

        // Verify the temperature setting for creative output
        val actual = promptRunner.llmInvocations.first().interaction.llm.temperature
        Assertions.assertEquals(
            0.9, checkNotNull(actual), 0.01,
            "Expected temperature to be 0.9: Higher for more creative output"
        )
    }

    @Test
    fun testReview() {
        val agent = WriteAndReviewAgent(200, 400)
        val userInput = UserInput("Tell me a story about a brave knight", Instant.now())
        val story = Story("Once upon a time, Sir Galahad...")
        val context = FakeOperationContext.create()

        context.expectResponse("A thrilling tale of bravery and adventure!")
        agent.reviewStory(userInput, story, context)

        val promptRunner = context.promptRunner() as FakePromptRunner
        val prompt = promptRunner.llmInvocations.first().prompt
        Assertions.assertTrue(prompt.contains("knight"), "Expected review prompt to contain 'knight'")
        Assertions.assertTrue(prompt.contains("review"), "Expected review prompt to contain 'review'")

        // Verify single LLM invocation during review
        Assertions.assertEquals(1, promptRunner.llmInvocations.size)
    }

}