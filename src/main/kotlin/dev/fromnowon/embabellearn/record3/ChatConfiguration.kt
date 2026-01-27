package dev.fromnowon.embabellearn.record3

import com.embabel.agent.core.AgentPlatform
import com.embabel.chat.Chatbot
import com.embabel.chat.agent.AgentProcessChatbot
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatConfiguration {

    @Bean
    fun chatbot(agentPlatform: AgentPlatform): Chatbot {
        return AgentProcessChatbot.utilityFromPlatform(
            agentPlatform,
            // Verbosity().showPrompts().showLlmResponses()
        )
    }

}