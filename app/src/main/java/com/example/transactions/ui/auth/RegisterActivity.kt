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
import com.example.transactions.data.response.DefaultResponse
import com.example.transactions.data.retrofit.ApiConfig
import com.example.transactions.databinding.ActivityRegisterBinding
import com.example.transactions.util.setLoading
import retrofit2.Call
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var password: String
    private lateinit var email: String
    private lateinit var name: String

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setToolbar()
        setListener()
    }

    private fun setToolbar() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setListener() {
        binding.apply {
            buttonSignUp.setOnClickListener {
                name = binding.editName.text.toString()
                email = binding.editEmail.text.toString()
                password = binding.editPassword.text.toString()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this@RegisterActivity, "Name, email and password must not be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this@RegisterActivity, "Email is not valid", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                register()
            }
        }
    }

    private fun register() {
        setLoading(this, true)
        val client = ApiConfig.getApiService().register(name, email, password)
        client.enqueue(object : retrofit2.Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                setLoading(this@RegisterActivity, false)
                if (response.isSuccessful) {
                    val status = response.body()?.status
                    val message = response.body()?.message

                    if (status == true) {
                        Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                        goToLogin()
                    } else {
                        Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                setLoading(this@RegisterActivity, false)
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    companion object {
        private val TAG = RegisterActivity::class.java.simpleName
    }
}