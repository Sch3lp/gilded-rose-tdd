package com.gildedrose.rendering

import com.gildedrose.Features
import com.gildedrose.domain.Item
import com.gildedrose.domain.StockList
import com.gildedrose.http.ResponseErrors.withError
import com.gildedrose.persistence.StockListLoadingError
import dev.forkhandles.result4k.*
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import org.http4k.core.*
import org.http4k.template.HandlebarsTemplates
import org.http4k.template.ViewModel
import org.http4k.template.viewModel
import java.io.StringWriter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.*

private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.UK)
private val handlebars = HandlebarsTemplates().HotReload("src/main/java")
private val view = Body.viewModel(handlebars, ContentType.TEXT_HTML).toLens()

fun renderWithHtmx(
    stockListResult: Result4k<StockList, StockListLoadingError>,
    now: Instant,
    zoneId: ZoneId,
    features: Features
): Response {
    val today = LocalDate.ofInstant(now, zoneId)
    return stockListResult.map { stockList ->
        val stockListModel = StockListHtmxViewModel(
            now = dateFormat.format(today),
            items = stockList.map { item ->
                val priceString = when (val price = item.price) {
                    null -> ""
                    is Success -> price.value?.toString().orEmpty()
                    is Failure -> "error"
                }
                item.toMap(today, priceString)
            },
            isDeletingEnabled = features.isDeletingEnabled
        )
        val htmlWriter = StringWriter()
        htmlWriter.appendHTML().html {
            stockListView(stockListModel)
        }
        Response(Status.OK).body(htmlWriter.toString())
    }.recover { error ->
        Response(Status.INTERNAL_SERVER_ERROR)
            .withError(error)
            .body("Something went wrong, we're really sorry.")
    }
}

data class StockListHtmxViewModel(
    val now: String,
    val items: List<Map<String, String>>,
    val isDeletingEnabled: Boolean
) : ViewModel


private fun Item.toMap(now: LocalDate, priceString: String): Map<String, String> = mapOf(
    "id" to id.toString(),
    "name" to name.value,
    "sellByDate" to if (sellByDate == null) "" else dateFormat.format(sellByDate),
    "sellByDays" to this.daysUntilSellBy(now).toString(),
    "quality" to this.quality.toString(),
    "price" to priceString
)

private fun Item.daysUntilSellBy(now: LocalDate): Long =
    if (sellByDate == null) 0 else
        ChronoUnit.DAYS.between(now, this.sellByDate)
