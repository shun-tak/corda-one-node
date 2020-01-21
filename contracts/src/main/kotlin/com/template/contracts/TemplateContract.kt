package com.template.contracts

import com.template.states.TemplateState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class TemplateContract : Contract {
    companion object {
        const val ID = "com.template.contracts.TemplateContract"
    }

    interface Commands : CommandData {
        class Create : TypeOnlyCommandData(), Commands
        class Add : TypeOnlyCommandData(), Commands
        class Sub : TypeOnlyCommandData(),Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when(command.value) {
            is Commands.Create -> requireThat {
                "# of inputs should be 0" using (tx.inputs.isEmpty())
                "# of outputs should be 1" using (tx.outputs.size == 1)
                val output = tx.outputsOfType<TemplateState>().single()
                "output value should be 0" using (output.value == 0)
            }

            is Commands.Add -> requireThat {
                "# of inputs should be 1" using (tx.inputs.size == 1)
                "# of outputs should be 1" using (tx.outputs.size == 1)
                val input = tx.inputsOfType<TemplateState>().single()
                val output = tx.outputsOfType<TemplateState>().single()
                "input value should be less than output value" using (input.value < output.value)
                "attributes are same except value" using (input.copy(value = output.value) == output)
            }

            is Commands.Sub -> requireThat {
                "# of inputs should be 1" using (tx.inputs.size == 1)
                "# of outputs should be 1" using (tx.outputs.size == 1)
                val input = tx.inputsOfType<TemplateState>().single()
                val output = tx.outputsOfType<TemplateState>().single()
                "input value should be greater than output value" using (input.value > output.value)
                "attributes are same except value" using (input.copy(value = output.value) == output)
            }
        }
    }
}