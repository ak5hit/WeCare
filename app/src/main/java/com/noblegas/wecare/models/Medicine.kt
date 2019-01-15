package com.noblegas.wecare.models

data class Medicine(
    var userID: String = "",
    var quantity: Long = 0,
    var quantityUnit: String = "",
    var expiryDate: Long = 0,
    var name: String = ""
) {
    constructor() : this("", 0, "", 0, "")
}