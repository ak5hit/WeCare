package com.noblegas.wecare.models

data class Medicine(
    var userID: String?,
    var quantity: Long,
    var quantityUnit: String?,
    var expiryDate: Long,
    var name: String
) {
    constructor() : this(null, 0, "", 0, "")
}