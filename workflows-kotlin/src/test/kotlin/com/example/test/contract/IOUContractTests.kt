package com.example.test.contract

import com.example.contract.IOUContract
import com.example.state.IOUState
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class IOUContractTests {
    private val ledgerServices = MockServices(listOf("com.example.contract", "com.example.flow"))
    private val megaCorp = TestIdentity(CordaX500Name("MegaCorp", "London", "GB"))
    private val miniCorp = TestIdentity(CordaX500Name("MiniCorp", "New York", "US"))
    private val iouValue = 1

    @Test
    fun `transaction must include Create command`() {
        ledgerServices.ledger {
            transaction {
                output(IOUContract.ID, IOUState(iouValue, miniCorp.party, megaCorp.party))
                fails() // validate ทันที เพราะมีแค่ output state เลย fail แน่ๆ
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), IOUContract.Commands.Create())
                verifies()
            }
        }
    }

    // มี input + output เลย fail
    @Test
    fun `transaction must have no inputs`() {
        ledgerServices.ledger {
            transaction {
                input(IOUContract.ID, IOUState(iouValue, miniCorp.party, megaCorp.party))
                output(IOUContract.ID, IOUState(iouValue, miniCorp.party, megaCorp.party))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), IOUContract.Commands.Create())
                failsWith("No input when create an IOU.") // failwith = ต้องการ exception ที่เป็น message ที่กำหนดเท่านั้น
            }
        }
    }

    // มี output เข้ามา 2 ตัว
    @Test
    fun `transaction must have one output`() {
        ledgerServices.ledger {
            transaction {
                output(IOUContract.ID, IOUState(iouValue, miniCorp.party, megaCorp.party))
                output(IOUContract.ID, IOUState(iouValue, miniCorp.party, megaCorp.party))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), IOUContract.Commands.Create())
//                verifies()
                failsWith("Only one output state should be created.")


            }
        }
    }

    // lender กับ borrower เป็น miniCorp เหมือนกัน
    @Test
    fun `lender is not borrower`() {
        ledgerServices.ledger {
            transaction {
                output(IOUContract.ID, IOUState(iouValue, miniCorp.party,miniCorp.party))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), IOUContract.Commands.Create())
                failsWith("The lender and the borrower cannot be same entity.")
            }
        }
    }

    // value = -1
    @Test
    fun `value must greater than 0`() {
        ledgerServices.ledger {
            transaction {
                output(IOUContract.ID, IOUState(-1, miniCorp.party, megaCorp.party))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), IOUContract.Commands.Create())
                failsWith("The IOU's value must greater than 0.")
            }
        }
    }
}