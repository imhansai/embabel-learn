package dev.fromnowon.embabellearn.demo

import com.embabel.agent.api.common.Ai
import com.embabel.agent.api.invocation.AgentInvocation
import com.embabel.agent.core.AgentPlatform
import com.embabel.agent.domain.io.UserInput
import dev.fromnowon.embabellearn.demo.agent.ReviewedStory
import dev.fromnowon.embabellearn.demo.injected.InjectedDemo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod

@ShellComponent
class DemoShell(
    private val injectedDemo: InjectedDemo,
    @field:Autowired private val agentPlatform: AgentPlatform,
) {

    @Autowired
    private lateinit var ai: Ai

    @ShellMethod("Demo")
    fun demo(): String {
        // Illustrate calling an agent programmatically,
        // as most often occurs in real applications.
        val reviewedStory = AgentInvocation.Companion
            .create(agentPlatform, ReviewedStory::class.java)
            .invoke(UserInput("Tell me a story about caterpillars"))
        return reviewedStory.content
    }

    @ShellMethod("Invent an animal")
    fun animal(): String {
        return injectedDemo.inventAnimal().toString()
    }

}