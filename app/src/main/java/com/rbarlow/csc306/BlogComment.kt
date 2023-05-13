package com.rbarlow.csc306

data class BlogComment(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val time: Long
)