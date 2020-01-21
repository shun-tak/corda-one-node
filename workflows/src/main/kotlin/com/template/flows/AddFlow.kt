package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.TemplateContract
import com.template.states.TemplateState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class AddFlow(private val amount: Int, private val linearId: UniqueIdentifier) : FlowLogic<SignedTransaction>() {
    companion object {
        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction based on new IOU.")
        object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
//        object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
//            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
//        }

        object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
//                GATHERING_SIGS,
                FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        progressTracker.currentStep = GENERATING_TRANSACTION
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val input = serviceHub.vaultService.queryBy<TemplateState>(queryCriteria).states.single()

        val txBuilder = TransactionBuilder(input.state.notary)
                .addCommand(Command(TemplateContract.Commands.Add(), ourIdentity.owningKey))
                .addInputState(input)
                .addOutputState(input.state.data.add(amount), TemplateContract.ID)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val tx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = FINALISING_TRANSACTION
        return subFlow(FinalityFlow(tx, listOf(), FINALISING_TRANSACTION.childProgressTracker()))
    }
}
