package com.example.transactions.pref

import android.content.Context
import com.example.transactions.data.response.UserResponse
import com.orhanobut.hawk.Hawk

class UserPreference {
    fun setUser(user: UserResponse){
        Hawk.put(USER_KEY, user)
    }

    fun getUser(): UserResponse? {
        return Hawk.get(USER_KEY)
    }

    fun isLogin(): Boolean {
        return Hawk.contains(USER_KEY)
    }

    fun deleteAll() {
        Hawk.delete(USER_KEY)
    }

    companion object {
        private const val USER_KEY = "user_key"
        private val userPreference = UserPreference()

        fun instance(context: Context?): UserPreference {
            Hawk.init(context).build()
            return userPreference
        }
    }
}