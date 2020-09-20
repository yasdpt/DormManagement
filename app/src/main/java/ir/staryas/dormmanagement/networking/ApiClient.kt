package ir.staryas.dormmanagement.networking

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ApiClient {
    private val baseUrl:String = "http://staryas.ir/dorm/api/"

    fun getClient():Retrofit{
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


}