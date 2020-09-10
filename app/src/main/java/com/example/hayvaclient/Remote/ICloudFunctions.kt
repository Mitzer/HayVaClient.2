package com.example.hayvaclient.Remote

import retrofit2.http.GET
import retrofit2.http.Query
import io.reactivex.Observable
import okhttp3.ResponseBody

interface ICloudFunctions {
    @GET("")
    fun getCustomToken(@Query("access_token") accessToken:String): Observable<ResponseBody>
}