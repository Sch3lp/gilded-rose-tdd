package com.gildedrose.persistence

import com.gildedrose.domain.StockList
import com.gildedrose.domain.Item
import com.gildedrose.domain.Item.Companion.invoke
import dev.forkhandles.result4k.valueOrNull
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

private const val lastModifiedHeader = "# LastModified:"

fun StockList.saveTo(file: File) {
    file.writer().buffered().use { writer ->
        this.toLines().forEach(writer::appendLine)
    }
}

fun StockList.toLines(): Sequence<String> = sequenceOf("$lastModifiedHeader $lastModified") +
    items.map { it.toLine() }

fun File.loadItems(): StockList? = useLines { lines ->
    lines.toStockList()
}

fun Sequence<String>.toStockList(): StockList? {
    val (header, body) = partition { it.startsWith("#") }
    val items = body.map { line -> line.toItem() }
    return if (items.any { it == null })
        null
    else
        StockList(
            lastModified = lastModifiedFrom(header) ?: Instant.EPOCH,
            items = items.filterNotNull()
        )
}

private fun Item.toLine() = "$name\t${sellByDate ?: ""}\t$quality"

private fun lastModifiedFrom(
    header: List<String>
) = header
    .lastOrNull { it.startsWith(lastModifiedHeader) }
    ?.substring(lastModifiedHeader.length)
    ?.trim()
    ?.toInstant()

private fun String.toInstant() = try {
    Instant.parse(this)
} catch (x: DateTimeParseException) {
    throw IOException("Could not parse LastModified header: " + x.message)
}

private fun String.toItem(): Item? {
    val parts: List<String> = this.split('\t')
    return Item(
        name = parts[0],
        sellByDate = parts[1].toLocalDate(),
        quality = parts[2].toInt()
    ).valueOrNull()
}

private fun String.toLocalDate() = if (this.isBlank()) null else LocalDate.parse(this)