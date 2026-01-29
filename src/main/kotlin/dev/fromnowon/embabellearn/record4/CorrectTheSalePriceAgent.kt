package dev.fromnowon.embabellearn.record4

import com.embabel.agent.api.annotation.*
import com.embabel.agent.api.common.Ai
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.createObject
import com.embabel.agent.domain.io.UserInput
import java.math.BigDecimal


@Agent(description = "纠正销售价格")
class CorrectTheSalePriceAgent {

    @Action
    fun extractInformation(userInput: UserInput, context: OperationContext): PendingCorrectPriceInfoList {
        val correctPriceInfoList = context.ai()
            .withDefaultLlm()
            .createObject(
                """
                        # 用户输入:
                        ${userInput.content}
                        
                        从用户输入中提取asin、国家(例如US、IT、CA等)、正确的价格(数值) 列表
                    """.trimIndent(),
                CorrectPriceInfoList::class.java
            )

        return PendingCorrectPriceInfoList(userInput, correctPriceInfoList)
    }

    @State
    interface Stage

    data class HumanFeedback(val comments: String)

    private data class AssessmentOfHumanFeedback(val noAdjustment: Boolean)

    @State
    data class PendingCorrectPriceInfoList(
        val userInput: UserInput,
        val priceInfoList: CorrectPriceInfoList
    ) : Stage {

        @Action
        fun getFeedback(): HumanFeedback {
            return fromForm(
                """
                    审查、调整数据
                    $priceInfoList
                    """.trimIndent(), HumanFeedback::class.java
            )
        }

        @Action(clearBlackboard = true)
        fun assess(feedback: HumanFeedback, ai: Ai): Stage {
            val assessment = ai.withDefaultLlm().createObject<AssessmentOfHumanFeedback>(
                """
                根据以下人工反馈，判断内容是否需要调整。如果内容不用调整，则返回 true，否则返回 false。
                
                # 内容
                $priceInfoList
                
                # 人工反馈
                ${feedback.comments}
                
            """.trimIndent()
            )

            return if (assessment.noAdjustment) {
                Done(userInput, priceInfoList)
            } else {
                Revise(userInput, priceInfoList, feedback)
            }
        }

    }

    @State
    data class Done(
        val userInput: UserInput,
        val priceInfoList: CorrectPriceInfoList
    ) : Stage {

        @AchievesGoal(description = "打印最终的数据")
        @Action
        fun printResult(): String {
            return priceInfoList.list.joinToString()
        }

    }

    @State
    data class Revise(
        val userInput: UserInput,
        val priceInfoList: CorrectPriceInfoList,
        val humanFeedback: HumanFeedback
    ) : Stage {

        @Action(clearBlackboard = true)
        fun revise(ai: Ai): PendingCorrectPriceInfoList {
            return ai.withDefaultLlm().createObject<PendingCorrectPriceInfoList>(
                """
                    根据用户反馈修正数据
                    
                    # 用户输入
                    ${userInput.content}
                    
                    # 之前的数据
                    $priceInfoList
                    
                    # 用户反馈
                    ${humanFeedback.comments}
                """.trimIndent()
            )
        }

    }

}

data class CorrectPriceInfo(

    val asin: String,

    val country: String,

    val price: BigDecimal

)

data class CorrectPriceInfoList(

    val list: List<CorrectPriceInfo>

)