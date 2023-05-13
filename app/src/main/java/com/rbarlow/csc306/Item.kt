package com.rbarlow.csc306

data class Item(
    val id: String,
    var name: String,
    var description: String,
    var image: String,
    var addedOn : Long,
    var addedBy : String,
    var views : Int,
    var approved : Boolean,
)
