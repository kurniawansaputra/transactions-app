package com.example.transactions.data.retrofit

import com.example.transactions.data.response.DefaultResponse
import com.example.transactions.data.response.UserResponse
import com.example.transactions.data.response.TransactionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<UserResponse>

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") review: String
    ): Call<DefaultResponse>

    @POST("logout")
    fun logout(
        @Header("Authorization") token: String
    ): Call<DefaultResponse>

    @GET("transactions")
    fun getTransactions(
        @Header("Authorization") token: String,
    ): Call<TransactionResponse>

    @Multipart
    @POST("transaction-store")
    suspend fun addTransaction(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("type") type: RequestBody,
        @Part file: MultipartBody.Part,
    ): DefaultResponse

    @Multipart
    @POST("transaction-update/{id}")
    suspend fun updateTransaction(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("type") type: RequestBody,
        @Part file: MultipartBody.Part? = null,
    ): DefaultResponse

    @POST("transaction-delete/{id}")
    fun deleteTransaction(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<DefaultResponse>
}