package com.example.transactions.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.transactions.R
import com.example.transactions.data.response.UserResponse
import com.example.transactions.data.retrofit.ApiConfig
import com.example.transactions.databinding.ActivityLoginBinding
import com.example.transactions.pref.UserPreference
import com.example.transactions.ui.main.MainActivity
import com.example.transactions.util.setLoading
import retrofit2.Call
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkIsLogin()
        setListener()
    }

    private fun checkIsLogin() {
        val isLogin = UserPreference.instance(this).isLogin()
        if (isLogin) {
            goToMain()
        }
    }

    private fun setListener() {
        binding.apply {
            buttonSignIn.setOnClickListener {
                email = binding.editEmail.text.toString()
                password = binding.editPassword.text.toString()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Email dan password tidak boleh kososng", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this@LoginActivity, "Email tidak valid", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                login()
            }

            labelSignUp.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }
    }

    private fun login() {
        setLoading(this, true)
        val client = ApiConfig.getApiService().login(email, password)
        client.enqueue(object : retrofit2.Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                setLoading(this@LoginActivity, false)
                if (response.isSuccessful) {
                    val status = response.body()?.status
                    val message = response.body()?.message

                    if (status == true) {
                        val user = response.body()
                        if (user != null) {
                            UserPreference.instance(this@LoginActivity).setUser(user)
                        }

                        goToMain()
                    } else {
                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                setLoading(this@LoginActivity, false)
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
    }
}