package dev.fromnowon.embabellearn.record3

import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.EmbabelComponent
import com.embabel.agent.api.common.ActionContext
import com.embabel.chat.Conversation
import com.embabel.chat.UserMessage

@EmbabelComponent
class ChatActions {

    @Action(canRerun = true, trigger = UserMessage::class)
    fun respond(
        conversation: Conversation,
        context: ActionContext
    ) {
        val assistantMessage = context.ai()
            .withDefaultLlm()
            .respond(conversation.messages)
        context.sendMessage(conversation.addMessage(assistantMessage))
    }

}