package dev.fromnowon.embabellearn.record3

import com.embabel.chat.ChatSession
import com.embabel.chat.Chatbot
import com.embabel.chat.UserMessage
import com.embabel.chat.support.console.ConsoleOutputChannel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod

@ShellComponent
class ChatShell {

    @Autowired
    private lateinit var chatbot: Chatbot

    @ShellMethod("会话")
    fun conversation() {
        val session: ChatSession = chatbot.createSession(
            user = null,
            outputChannel = ConsoleOutputChannel()
        )

        var userInput = readln()
        while (userInput != "/bye") {
            session.onUserMessage(UserMessage(userInput))
            userInput = readln()
        }
    }

}