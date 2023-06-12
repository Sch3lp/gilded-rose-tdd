package com.gildedrose.rendering

import kotlinx.html.*

fun HTML.stockListView(viewModel: StockListHtmxViewModel) {
    body {
        h1 { +viewModel.now }
        form(method = FormMethod.post, action = "/delete-items") {
            if (viewModel.isDeletingEnabled) {
                input(type = InputType.submit) { +"Delete" }
            }
            table {
                tr {
                    if (viewModel.isDeletingEnabled) {
                        th { }
                    }
                    th { +"ID" }
                    th { +"Name" }
                    th { +"Sell By Date" }
                    th { +"Sell By Days" }
                    th { +"Quality" }
                    th { +"Price" }
                }
                viewModel.items.map { item ->
                    tr {
                        if (viewModel.isDeletingEnabled) {
                            td {
                                input(type = InputType.checkBox, name = item["id"])
                            }
                            td { + item["id"]!! }
                            td { + item["name"]!! }
                            td { + item["sellByDate"]!! }
                            td { + item["sellByDays"]!! } //todo: text-align:right
                            td { + item["quality"]!! } //todo: text-align:right
                            td { + item["price"]!! } //todo: text-align:right
                        }
                    }
                }
            }
        }
    }
}
