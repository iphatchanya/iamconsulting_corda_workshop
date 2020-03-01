package com.example.state

import com.example.contract.IOUContract
import com.example.schema.IOUSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * The state object recording IOU agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 *
 * @param value the value of the IOU.
 * @param lender the party issuing the IOU.
 * @param borrower the party receiving and approving the IOU.
 */
@BelongsToContract(IOUContract::class)
data class IOUState(val value: Int,
                    val lender: Party,
                    val borrower: Party,
                    override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState, QueryableState {
    // ประเภท State :
    //  Linear State ต้อง override unique id
    //  Queryable State เก็บค่าไว้ใน vault state เลย query เพื่อให้อ่านง่าย
    override val participants: List<Party> get() = listOf(lender, borrower) // important in state

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is IOUSchemaV1 -> IOUSchemaV1.PersistentIOU(

                    /*###########################################################
                    ###### TODO 1.Map state field with schema field
                    ###########################################################*/
                lender = lender.name.toString(),
                borrower = borrower.name.toString(),
                value = value,
                linearId = linearId.id

            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(IOUSchemaV1)
}
