package com.example.mensajeria.models

data class Chat(
    var id: String = "",
    var name: String = "",
    var users: List<String> = emptyList(),
    var selected:Boolean=false,
)