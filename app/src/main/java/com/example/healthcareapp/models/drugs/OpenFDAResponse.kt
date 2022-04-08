package com.example.healthcareapp.models.drugs

data class OpenFDAResponse(
    val meta: Meta,
    val results: List<Results>
)