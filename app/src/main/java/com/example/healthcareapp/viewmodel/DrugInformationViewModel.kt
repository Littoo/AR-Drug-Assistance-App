package com.example.healthcareapp.viewmodel

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.healthcareapp.`interface`.openFDAInterface
import com.example.healthcareapp.models.drugs.OpenFDAResponse
import com.example.healthcareapp.network.OpenFDAInstance
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl

class DrugInformationViewModel: ViewModel() {

    //Live Data
    var OpenFDAData = MutableLiveData<OpenFDAResponse>()

    private val search_key: String = "openfda.generic_name.exact:"+ "\"VALPROIC+ACID\""

    fun getApiData(){
        /*val retrofitInstance = RetrofitInstance.getRetrofitInstance().create(RetrofitService::class.java)*/
        val openFDAInstance = OpenFDAInstance.getOpenFDAInstance().create(openFDAInterface::class.java)

//        openFDAInstance.getDrugInfobyGenericName(gen_name).enqueue(object : Callback<OpenFDAResponse> {
//            override fun onResponse(
//                call: Call<OpenFDAResponse>,
//                response: Response<OpenFDAResponse>
//            ) {
//                OpenFDAData.value = response.body()
//            }
//
//            override fun onFailure(call: Call<OpenFDAResponse>, t: Throwable) {
//
//            }
//
//        })
        CoroutineScope(Dispatchers.IO).launch {
            val response = openFDAInstance.getDrugInfobyGenericName(search_key.toString())
            Log.i("search_key :", response.body().toString())
            if (response.isSuccessful){
                // Convert raw JSON to pretty JSON using GSON library
                val gson = GsonBuilder().setPrettyPrinting().create()
                val prettyJson = gson.toJson(
                    JsonParser.parseString(
                        response.body()
                            ?.toString() // About this thread blocking annotation : https://github.com/square/retrofit/issues/3255
                    )
                )

                Log.i("Pretty Printed JSON :", prettyJson)
                OpenFDAData.value = response.body()
            } else {

                Log.e("RETROFIT_ERROR", response.toString())

            }
        }

    }
}