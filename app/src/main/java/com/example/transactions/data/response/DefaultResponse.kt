package com.example.transactions.data.response

import com.google.gson.annotations.SerializedName

data class DefaultResponse(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)
