package com.example.test

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.NotarySpec
import net.corda.testing.node.User

/**
 * This file is exclusively for being able to run your nodes through an IDE.
 * Do not use in a production environment.
 */
fun main(args: Array<String>) {
    val user = User("user1", "test", permissions = setOf("ALL"))

    driver(DriverParameters(isDebug = true,
            startNodesInProcess = true,
            extraCordappPackagesToScan = listOf("com.example"),
            inMemoryDB = false,
            waitForAllNodesToFinish = true)) {

        startNode(
                providedName = CordaX500Name("PartyA", "London", "GB"),
                rpcUsers = listOf(user),
                customOverrides = mapOf("h2port" to "12345")
        //jdbc:h2:tcp://127.0.0.1:12345/node
        ).getOrThrow()

        startNode(
                providedName = CordaX500Name("PartyB", "New York", "US"),
                rpcUsers = listOf(user),
                customOverrides = mapOf("h2port" to "12346")
        //jdbc:h2:tcp://127.0.0.1:12346/node
        ).getOrThrow()

        startNode(
                providedName = CordaX500Name("PartyC", "Paris", "FR"),
                rpcUsers = listOf(user),
                customOverrides = mapOf("h2port" to "12347")
        //jdbc:h2:tcp://127.0.0.1:12347/node
        ).getOrThrow()
    }

}
