package com.example.healthcareapp.`interface`

import android.net.Uri
import com.example.healthcareapp.models.drugs.OpenFDAResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.net.URL


interface openFDAInterface {
//    api_key=yoZOtBqKPrr6uexaCYVp8ggO0GrbddWM1RAF6wM0
//    @GET("label.json?search=openfda.generic_name.exact:\"VALPROIC+ACID\"")
    @GET("label.json?")
    suspend fun getDrugInfobyGenericName(
        @Query("search") genericName: String
    ) : Response<OpenFDAResponse>

//    companion object {
//        const val BASE_URL = "https://api.fda.gov/drug"
//    }
}