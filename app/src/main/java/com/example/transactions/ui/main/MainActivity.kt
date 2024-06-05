package com.example.transactions.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.transactions.R
import com.example.transactions.data.response.DefaultResponse
import com.example.transactions.data.response.TransactionResponse
import com.example.transactions.data.retrofit.ApiConfig
import com.example.transactions.databinding.ActivityMainBinding
import com.example.transactions.pref.UserPreference
import com.example.transactions.ui.auth.LoginActivity
import com.example.transactions.ui.transactions.TransactionsActivity
import com.example.transactions.util.formatToIDR
import com.example.transactions.util.setLoading
import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var name: String
    private lateinit var token: String
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setPref()
        setDetail()
        setListener()
        getTransaction()
        optionMenu()
        swipeRefresh()
    }

    private fun setPref() {
        val user = UserPreference.instance(this).getUser()
        if (user != null) {
            token = user.accessToken
            name = user.data.name
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDetail() {
        binding.textName.text = "Hai, $name!"
    }

    private fun getTransaction() {
        val client = ApiConfig.getApiService().getTransactions("Bearer $token")
        client.enqueue(object : retrofit2.Callback<TransactionResponse> {
            override fun onResponse(call: Call<TransactionResponse>, response: Response<TransactionResponse>) {
                binding.swipeRefresh.isRefreshing = false
                if (response.isSuccessful) {
                    val status = response.body()?.status

                    if (status == true) {
                        binding.textBalance.text = formatToIDR(response.body()?.balance ?: 0)
                        binding.textIncome.text = formatToIDR(response.body()?.totalIncome ?: 0)
                        binding.textExpense.text = formatToIDR(response.body()?.totalExpense ?: 0)
                    }
                }
            }

            override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
                binding.swipeRefresh.isRefreshing = false
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun setListener() {
        binding.apply {
            materialCardViewTransactions.setOnClickListener {
                val intent = Intent(this@MainActivity, TransactionsActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun optionMenu() {
        binding.apply {
            topAppBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menuLogout -> {
                        logout()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun logout() {
        setLoading(this, true)
        val client = ApiConfig.getApiService().logout("Bearer $token")
        client.enqueue(object : retrofit2.Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                setLoading(this@MainActivity, false)
                if (response.isSuccessful) {
                    val status = response.body()?.status

                    if (status == true) {
                        UserPreference.instance(this@MainActivity).deleteAll()
                        goToLogin()
                    }
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                setLoading(this@MainActivity, false)
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun swipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            getTransaction()
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}