package com.template.states

import com.template.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

// *********
// * State *
// *********
@BelongsToContract(TemplateContract::class)
data class TemplateState(val value: Int, override val participants: List<AbstractParty> = listOf(),
                         override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {

    fun add(amount: Int) = copy(value = value + amount)

    fun sub(amount: Int) = copy(value = value - amount)
}
