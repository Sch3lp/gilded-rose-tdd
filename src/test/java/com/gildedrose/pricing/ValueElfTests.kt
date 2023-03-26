package com.gildedrose.pricing

import com.gildedrose.domain.Item
import com.gildedrose.domain.Price
import com.gildedrose.foundation.IO
import com.gildedrose.foundation.magic
import com.gildedrose.foundation.retry
import com.gildedrose.testing.IOResolver
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.resultFrom
import org.http4k.client.ApacheClient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URI

@ExtendWith(IOResolver::class)
@Suppress("JUnitMalformedDeclaration") // confused by the Fixture receivers
@EnabledIfSystemProperty(named = "run-external-tests", matches = "true")
class ValueElfTests : ValueElfContract(
    Fixture(
        uri = URI.create("http://value-elf.com:8080/prices"),
        handler = ApacheClient(),
        expectedPrice = Price(709)!!
    )
) {
    context(Fixture, IO)
    @Test
    fun `fails sometimes`() {
        val result: List<Result<Price?, Exception>> = (1..500).map {
            resultFrom {
                client.invoke(magic<IO>(), aFoundItem)
            }
        }
        val (successes, failures) = result.partition { it is Success }
        assertTrue(successes.all { it is Success && it.value == expectedPrice })
        val successRatio = successes.size / failures.size.toDouble()
        println("Successes = ${successes.size}, failures = ${failures.size}, ratio = $successRatio")
    }

    context(Fixture, IO)
    @Test
    fun `retry prevents failure`() {
        val retryingClient = retry(1, function = { it: Item -> client(magic<IO>(), it) })
        val result: List<Result<Price?, Exception>> = (1..500).map {
            resultFrom {
                retryingClient.invoke(aFoundItem)
            }
        }
        val (successes, failures) = result.partition { it is Success }
        assertTrue(successes.all { it is Success && it.value == expectedPrice })
        assertTrue(failures.isEmpty())
    }
}

