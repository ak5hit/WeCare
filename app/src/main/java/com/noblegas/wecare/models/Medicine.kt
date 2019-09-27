package com.noblegas.wecare.models

data class Medicine(
    var name: String = "",
    var quantity: Long = 0,
    var quantityUnit: String = "",
    var expiryDate: Long = 0,

    var uploadTime: Long = 0,
    var uploaderID: String = "",
    var uploaderName: String = "",
    var uploaderImageURL: String = "",
    var imageUrls: HashMap<String, String> = HashMap()
) {
    constructor() : this(
        "", 0, "", 0, 0,
        "", "", "", HashMap()
    )
}