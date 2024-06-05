package com.example.transactions.data.response

import com.google.gson.annotations.SerializedName

data class UserResponse(

	@field:SerializedName("access_token")
	val accessToken: String,

	@field:SerializedName("data")
	val data: UserItems,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("token_type")
	val tokenType: String,

	@field:SerializedName("status")
	val status: Boolean
)

data class UserItems(

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("email")
	val email: String
)
