package com.example.test


import com.example.state.IOUState
import net.corda.core.contracts.FungibleState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.schemas.QueryableState
import org.junit.Test
import kotlin.test.assertEquals

class IOUStateTest {

    // ป้องกัน node ล่ม

    // check ทุก field และ type
    @Test
    fun `State contain correct field and type`() {
        //Example
        assertEquals(IOUState::class.java.getDeclaredField("linearId").type, UniqueIdentifier::class.java, "Field 'linearId' is not UniqueIdentifier")
        /*###########################################################
        ###### TODO Add test for Value, Lender and Borrower field
        ###########################################################*/
        assertEquals(IOUState::class.java.getDeclaredField("borrower").type, Party::class.java, "Field 'borrower' is not Party")
        assertEquals(IOUState::class.java.getDeclaredField("lender").type, Party::class.java, "Field 'lender' is not Party")
        assertEquals(IOUState::class.java.getDeclaredField("value").type, Int::class.java, "Field 'value' is not Int")


    }

    // State =  linear state โดย check กับ IOU State
    @Test
    fun `State is Linear State`() {
        assert(LinearState::class.java.isAssignableFrom(IOUState::class.java))
    }

    @Test
    fun `State is Queryable State`() {
        assert(QueryableState::class.java.isAssignableFrom(IOUState::class.java))
    }

    // State = fungible state ใช้ยาก แต่เหมาะกับการทำ currency
    @Test
    fun `State is Fungible State`() {
        assert(FungibleState::class.java.isAssignableFrom(IOUState::class.java))
    }

}