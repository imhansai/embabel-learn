package dev.fromnowon.embabellearn.record1

import com.embabel.agent.api.common.Ai
import com.embabel.agent.api.common.streaming.StreamingPromptRunner
import com.embabel.common.ai.model.LlmOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@ShellComponent
class HelloShell {

    /**
     * spring shell 入门
     */
    @ShellMethod(key = ["hello-world"], value = "Say hello to a given name")
    fun helloWorld(@ShellOption(defaultValue = "spring") name: String): String {
        return "Hello world $name"
    }

    @Autowired
    private lateinit var ai: Ai

    /**
     * embabel agent 入门
     */
    @ShellMethod("生成文本")
    fun hello() {
        // 流式输出
        val streamingRunner = ai.withDefaultLlm() as StreamingPromptRunner
        val restaurantStream = streamingRunner.stream()
            .withPrompt("你好，介绍一下自己")
            .generateStream()

        restaurantStream.timeout(150.seconds.toJavaDuration()).doOnSubscribe {
            println("Stream subscription started")
        }.doOnNext {
            print(it)
        }.doOnError {
            it.printStackTrace()
        }.doOnComplete {
            println("Stream completed")
        }.blockLast(6_000.seconds.toJavaDuration())

        // return ai.withDefaultLlm().generateText("你好,介绍一下自己")
    }

    @ShellMethod("讲笑话")
    fun joke(@ShellOption topic1: String, @ShellOption topic2: String, @ShellOption voice: String): Joke {
        return ai
            .withLlm(LlmOptions.withDefaultLlm().withTemperature(.8)) // 高温实现创意
            .createObject(
                """
                    Tell me a joke about $topic1 and $topic2.
                    The voice of the joke should be $voice.
                    The joke should have a leadup and a punchline.
                    
                    """.trimIndent(),
                Joke::class.java
            )
    }

}

data class Joke(val leadup: String, val punchline: String)