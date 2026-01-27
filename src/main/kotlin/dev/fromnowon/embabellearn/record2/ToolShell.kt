package dev.fromnowon.embabellearn.record2

import com.embabel.agent.api.common.Ai
import com.embabel.agent.tools.math.MathTools
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod

@ShellComponent
class ToolShell {

    @Autowired
    private lateinit var mathToolGroup: MathTools

    @Autowired
    private lateinit var ai: Ai

    @ShellMethod("数学计算")
    fun tool(): String {
        return ai
            .withDefaultLlm()
            // .withToolGroup(mathToolGroup)
            // .withToolGroup(CoreToolGroups.MATH)
            .withTools(mathTools)
            .generateText("2 + 5 * 3 等于多少")
    }

}