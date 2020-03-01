package com.example.flow

import co.paralleluniverse.fibers.Suspendable
import com.example.contract.IOUContract
import com.example.state.IOUState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step


object CreateFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val iouValue: Int,
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
        // Flow เก็บเข้า checkpoint แล้วกลับมาทำใหม่ได้
            /*###########################################################
            ###### TODO 1.Select Notary
            ###########################################################*/

            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            /*###########################################################
            ###### TODO 2.Build transaction
            ###########################################################*/
            progressTracker.currentStep = GENERATING_TRANSACTION

            val iouState = IOUState(lender = serviceHub.myInfo.legalIdentities.first(), borrower = otherParty, value = iouValue)
            val txCommand = Command(IOUContract.Commands.Create(), iouState.participants.map{ it.owningKey})
            val txBuilder = TransactionBuilder(notary = notary)
                .addOutputState(iouState)
                .addCommand(txCommand)

            /*###########################################################
            ###### TODO 3.Verify transaction
            ###########################################################*/
            progressTracker.currentStep = VERIFYING_TRANSACTION
            txBuilder.verify(serviceHub)

            /*###########################################################
            ###### TODO 4.Initial sign transaction
            ###########################################################*/
            progressTracker.currentStep = SIGNING_TRANSACTION
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            /*###########################################################
            ###### TODO 5.Collect signature from other node
            ###########################################################*/
            progressTracker.currentStep = GATHERING_SIGS
            val otherPartySession = setOf(initiateFlow(otherParty))
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, otherPartySession))

            /*###########################################################
            ###### TODO 6.Finalise flow and commit transaction to vault of each node
            ###########################################################*/
            progressTracker.currentStep = FINALISING_TRANSACTION
            // Notarise and record the transaction in both parties' vaults.
            //(This line is wrong, don't trust me!!!)
            return subFlow(FinalityFlow(fullySignedTx, otherPartySession))
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    /*###########################################################
                    ###### TODO 7.Flow responder can validate transaction too!!
                    ###########################################################*/
                    // Rest API ยิง check ค่าได้ / ส่วนของ Business logic
                    val output = stx.tx.outputStates.single()
                    "Output state must be an IOU state." using (output is IOUState)
                    val iou = output as IOUState
                    "I won't accept IOUs with a value over 100." using (iou.value <= 100)
                }
            }
            val txId = subFlow(signTransactionFlow).id

            return subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
        }
    }
}
