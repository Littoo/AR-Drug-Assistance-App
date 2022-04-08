package com.example.healthcareapp.models.drugs

data class Meta(
    val disclaimer: String,
    val last_updated: String,
    val license: String,
    val results: MetaResults,
    val terms: String
)