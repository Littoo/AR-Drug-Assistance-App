package com.example.healthcareapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenFDAInstance {
    val BASE_URL = "https://api.fda.gov/drug/"

    fun getOpenFDAInstance() : Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}