package com.example.itemsfinder

import org.json.JSONObject

class Item(val itemName: String, val description: String, val itemType: String) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("itemName", itemName)
        json.put("description", description)
        json.put("itemType", itemType)
        return json
    }
}
