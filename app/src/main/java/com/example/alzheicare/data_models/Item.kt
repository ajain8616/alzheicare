package com.example.alzheicare.data_models

data class Item(
    val itemName: String = "",
    val description: String = "",
    val itemType: String = "",
    val documentId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    var isChecked: Boolean = false,
    val itemId: String = ""
)