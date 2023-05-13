package com.rbarlow.csc306

data class BlogComment(
    val id: String,
    val content: String,
    val author: String,
    var addedOn : Long
)