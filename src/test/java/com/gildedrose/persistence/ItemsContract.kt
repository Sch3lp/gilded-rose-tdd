package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.foundation.IO
import com.gildedrose.item
import com.gildedrose.oct29
import com.gildedrose.testing.IOResolver
import dev.forkhandles.result4k.Success
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import kotlin.test.assertEquals

@ExtendWith(IOResolver::class)
abstract class ItemsContract<TX : TXContext>(
    val items: Items<TX>
) {
    private val initialStockList = StockList(
        lastModified = Instant.parse("2022-02-09T23:59:59Z"),
        items = listOf(
            item("banana", oct29.minusDays(1), 42),
            item("kumquat", oct29.plusDays(1), 101)
        )
    )

    context(IO)
    @Test
    fun `returns empty stocklist before any save`() {
        items.inTransaction {
            assertEquals(
                Success(
                    StockList(
                        lastModified = Instant.EPOCH,
                        items = emptyList()
                    )
                ),
                items.load()
            )
        }
    }

    context(IO)
    @Test
    fun `returns last saved stocklist`() {
        items.inTransaction {
            items.save(initialStockList)
            assertEquals(
                Success(initialStockList),
                items.load()
            )

            val modifiedStockList = initialStockList.copy(
                lastModified = initialStockList.lastModified.plusSeconds(3600),
                items = initialStockList.items.drop(1)
            )
            items.save(modifiedStockList)
            assertEquals(
                Success(modifiedStockList),
                items.load()
            )
        }
    }
}
