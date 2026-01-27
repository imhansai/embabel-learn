package dev.fromnowon.embabellearn.record2

import com.embabel.agent.api.annotation.LlmTool
import com.embabel.agent.api.tool.Tool

class MathService {

    @LlmTool(description = "Adds two numbers together")
    fun add(
        @LlmTool.Param(description = "First number") a: Int,
        @LlmTool.Param(description = "Second number") b: Int,
    ): Int = a + b

    @LlmTool(description = "Multiplies two numbers")
    fun multiply(
        @LlmTool.Param(description = "First number") a: Int,
        @LlmTool.Param(description = "Second number") b: Int,
    ): Int = a * b
}

// Create tools from all annotated methods on an instance
val mathTools = Tool.fromInstance(MathService())
