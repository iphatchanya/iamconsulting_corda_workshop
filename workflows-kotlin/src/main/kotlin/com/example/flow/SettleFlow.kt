package com.example.flow

import co.paralleluniverse.fibers.Suspendable
import com.example.contract.IOUContract
import com.example.state.IOUState
import net.corda.core.contracts.Command
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step


object SettleFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val paybackValue: Int,
                    val linearId: String,
                    val otherParty: Party) : FlowLogic<SignedTransaction>() {
        /**
         * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
         * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
         */
        companion object {
            object GENERATING_TRANSACTION : Step("Generating transaction based on new IOU.")
            object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
            object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
            object GATHERING_SIGS : Step("Gathering the counterparty's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    GENERATING_TRANSACTION,
                    VERIFYING_TRANSACTION,
                    SIGNING_TRANSACTION,
                    GATHERING_SIGS,
                    FINALISING_TRANSACTION
            )
        }

        override val progressTracker = tracker()

        /**
         * The flow logic is encapsulated within the call() method.
         */
        @Suspendable
        override fun call(): SignedTransaction {

            /*###########################################################
            ###### TODO 1.VaultQuery (more info https://docs.corda.net/api-vault-query.html)
            ###########################################################*/
//            val queryCriteria =
//            val inputState =

            val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(linearId)))
            val inputState = serviceHub.vaultService.queryBy<IOUState>(queryCriteria).states.single()

            /*###########################################################
            ###### TODO 2.Select Notary from inputState
            ###########################################################*/

//            val notary =
            val notary = serviceHub.networkMapCache.notaryIdentities.first()


            /*###########################################################
            ###### TODO 3.Build transaction
            ###########################################################*/
            progressTracker.currentStep = GENERATING_TRANSACTION

//            val iouState =
//            val txCommand =
//            val txBuilder =

            val iouState = IOUState(lender = otherParty, borrower = serviceHub.myInfo.legalIdentities.first(), value = paybackValue)
            val txCommand = Command(IOUContract.Commands.Create(), iouState.participants.map{ it.owningKey})
            val txBuilder = TransactionBuilder(notary = notary)
                    .addOutputState(iouState)
                    .addCommand(txCommand)

            /*###########################################################
            ###### TODO 4.Verify transaction
            ###########################################################*/
            progressTracker.currentStep = VERIFYING_TRANSACTION
//            txBuilder.?
            txBuilder.verify(serviceHub)

            /*###########################################################
            ###### TODO 5.Initial sign transaction
            ###########################################################*/
            progressTracker.currentStep = SIGNING_TRANSACTION
//            val partSignedTx =
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)


            /*###########################################################
            ###### TODO 6.Collect signature from other node
            ###########################################################*/
            progressTracker.currentStep = GATHERING_SIGS
//            val otherPartySession =
//            val fullySignedTx =
            val otherPartySession = setOf(initiateFlow(otherParty))
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, otherPartySession))

            /*###########################################################
            ###### TODO 7.Finalise flow and commit transaction to vault of each node
            ###########################################################*/
            progressTracker.currentStep = FINALISING_TRANSACTION
            // Notarise and record the transaction in both parties' vaults.
            //(This line is wrong, don't trust me!!!)
//            return subFlow(CollectSignaturesFlow(serviceHub.signInitialTransaction(TransactionBuilder(otherParty)), setOf(initiateFlow(otherParty)), GATHERING_SIGS.childProgressTracker()))
            return subFlow(FinalityFlow(fullySignedTx, otherPartySession))
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    //You can ignore it (just for this bootcamp) :)
                }
            }
            val txId = subFlow(signTransactionFlow).id

            return subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
        }
    }
}
