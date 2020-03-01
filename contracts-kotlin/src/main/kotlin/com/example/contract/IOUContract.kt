package com.example.contract

import com.example.state.IOUState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

/**
 * A implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [IOUState], which in turn encapsulates an [IOUState].
 *
 * For a new [IOUState] to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [IOUState].
 * - An Create() command with the public keys of both the lender and the borrower.
 *
 * All contracts must sub-class the [Contract] interface.
 */
class IOUContract : Contract {
    companion object {
        @JvmStatic
        val ID = "com.example.contract.IOUContract"
    }

    interface Commands : CommandData {
        class Create : Commands
        class Settle : Commands
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()

        when (command.value) {

            is Commands.Create -> requireThat {

                /*###########################################################
                ###### TODO Add verify as below
                # 1.No input when create an IOU.
                # 2.Only one output state should be created.
                # 3.The lender and the borrower cannot be the same entity.
                # 4.The IOU's value must greater than 0.
                ###########################################################*/

                "No input when create an IOU." using (tx.inputStates.isEmpty())
                "Only one output state should be created." using (tx.outputStates.size == 1)

                val iou:IOUState = tx.outputStates.first() as IOUState
                "The lender and the borrower cannot be same entity." using (iou.lender != iou.borrower)
                "The IOU's value must greater than 0." using (iou.value > 0)


            }

            is Commands.Settle -> requireThat {

                /*###########################################################
                ###### TODO Add verify as below
                # 1.One input when settle an IOU.
                # 2.One output when settle an IOU.
                # 3.The lender and the borrower cannot be the same entity.
                # 4.The IOU's value must be non-negative.
                # 5.Lender input same with Lender output.
                # 6.Borrower input same with Borrower output.
                ###########################################################*/

            }

        }
    }
}
