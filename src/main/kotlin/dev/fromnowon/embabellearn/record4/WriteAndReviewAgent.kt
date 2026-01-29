package dev.fromnowon.embabellearn.record4

import com.embabel.agent.api.annotation.*
import com.embabel.agent.api.common.Ai
import com.embabel.agent.domain.io.UserInput
import com.embabel.agent.domain.library.HasContent
import com.embabel.agent.prompt.persona.Persona
import com.embabel.agent.prompt.persona.RoleGoalBackstory
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.core.types.Timestamped
import org.springframework.beans.factory.annotation.Value
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


abstract class Personas {

    companion object {

        val WRITER = RoleGoalBackstory(
            role = "Creative Storyteller",
            goal = "Write engaging and imaginative stories",
            backstory = "Has a PhD in French literature; used to work in a circus"
        )

        val REVIEWER: Persona = Persona(
            "Media Book Review",
            "New York Times Book Reviewer",
            "Professional and insightful",
            "Help guide readers toward good stories"
        )

    }

}


@Agent(description = "Generate a story based on user input and review it")
class WriteAndReviewAgent(
    @Value($$"${storyWordCount:100}") storyWordCount: Int,
    @Value($$"${reviewWordCount:100}") reviewWordCount: Int
) {

    data class Story(
        val text: String,
    )

    data class ReviewedStory(
        val story: Story,
        val review: String,
        val reviewer: Persona,
    ) : HasContent, Timestamped {

        override val timestamp: Instant
            get() = Instant.now()

        override val content: String
            get() = """
            # Story
            ${story.text}

            # Review
            $review

            # Reviewer
            ${reviewer.name}, ${
                timestamp.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))
            }
        """.trimIndent()
    }

    @State
    interface Stage

    data class Properties(
        val storyWordCount: Int,
        val reviewWordCount: Int
    )

    private var properties: Properties = Properties(storyWordCount, reviewWordCount)

    @Action
    fun craftStory(userInput: UserInput, ai: Ai): AssessStory {
        val draft = ai
            .withLlm(LlmOptions.withAutoLlm().withTemperature(.7))
            .withPromptContributor(Personas.WRITER)
            .createObject(
                String.format(
                    """
                        Craft a short story in %d words or less.
                        The story should be engaging and imaginative.
                        Use the user's input as inspiration if possible.

                        # User input
                        %s
                        
                        """.trimIndent(),
                    properties.storyWordCount,
                    userInput.content
                ).trim(), Story::class.java
            )
        return AssessStory(userInput, draft, properties)
    }

    data class HumanFeedback(val comments: String)

    private data class AssessmentOfHumanFeedback(val acceptable: Boolean)

    @State
    data class AssessStory(
        val userInput: UserInput,
        val story: Story,
        val properties: Properties
    ) : Stage {

        @Action
        fun getFeedback(): HumanFeedback {
            return fromForm(
                """
                    Please provide feedback on the story
                    ${story.text}
                    
                    """.trimIndent(),
                HumanFeedback::class.java
            )
        }

        @Action(clearBlackboard = true)
        fun assess(feedback: HumanFeedback, ai: Ai): Stage {
            val assessment = ai.withDefaultLlm().createObject(
                """
                    Based on the following human feedback, determine if the story is acceptable.
                    Return true if the story is acceptable, false otherwise.

                    # Story
                    ${story.text}

                    # Human feedback
                    ${feedback.comments}
                    
                    """.trimIndent(),
                AssessmentOfHumanFeedback::class.java
            )
            return if (assessment.acceptable) {
                Done(userInput, story, properties)
            } else {
                ReviseStory(userInput, story, feedback, properties)
            }
        }
    }

    @State
    data class ReviseStory(
        val userInput: UserInput,
        val story: Story,
        val humanFeedback: HumanFeedback,
        val properties: Properties
    ) : Stage {

        @Action(clearBlackboard = true)
        fun reviseStory(ai: Ai): AssessStory {
            val draft = ai
                .withLlm(LlmOptions.withAutoLlm().withTemperature(.7))
                .withPromptContributor(Personas.WRITER)
                .createObject(
                    """
                            Revise a short story in ${properties.storyWordCount} words or less.
                            Use the user's input as inspiration if possible.

                            # User input
                            ${userInput.content}

                            # Previous story
                            ${story.text}

                            # Revision instructions
                            ${humanFeedback.comments}
                            
                            """.trimIndent(), Story::class.java
                )
            return AssessStory(userInput, draft, properties)
        }
    }

    @State
    data class Done(
        val userInput: UserInput,
        val story: Story,
        val properties: Properties
    ) : Stage {

        @AchievesGoal(
            description = "The story has been crafted and reviewed by a book reviewer",
            export = Export(remote = true, name = "writeAndReviewStory")
        )
        @Action
        fun reviewStory(ai: Ai): ReviewedStory {
            val review = ai
                .withAutoLlm()
                .withPromptContributor(Personas.REVIEWER)
                .generateText(
                    """
                            You will be given a short story to review.
                            Review it in ${properties.reviewWordCount} words or less.
                            Consider whether the story is engaging, imaginative, and well-written.

                            # Story
                            ${story.text}

                            # User input that inspired the story
                            ${userInput.content}
                            
                            """.trimIndent(),
                )
            return ReviewedStory(story, review, Personas.REVIEWER)
        }

    }

}