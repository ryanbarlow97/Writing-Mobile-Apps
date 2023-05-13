package com.rbarlow.csc306

data class BlogPost(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    var addedOn : Long,
)