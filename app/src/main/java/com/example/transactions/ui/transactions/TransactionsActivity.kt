package com.example.transactions.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.transactions.R
import com.example.transactions.data.response.TransactionItem
import com.example.transactions.data.response.TransactionResponse
import com.example.transactions.data.retrofit.ApiConfig
import com.example.transactions.databinding.ActivityTransactionsBinding
import com.example.transactions.pref.UserPreference
import com.example.transactions.ui.addedittransaction.AddEditTransactionActivity
import retrofit2.Call
import retrofit2.Response

class TransactionsActivity : AppCompatActivity() {
    private lateinit var token: String

    private lateinit var binding: ActivityTransactionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setPref()
        setToolbar()
        setListener()
        getTransaction()
    }

    private fun setPref() {
        val user = UserPreference.instance(this).getUser()
        if (user != null) {
            token = user.accessToken
        }
    }

    private fun setToolbar() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setListener() {
        binding.fabAddTransaction.setOnClickListener {
            val intent = Intent(this, AddEditTransactionActivity::class.java)
            intent.putExtra("activity", "add")
            startActivity(intent)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getTransaction() {
        setProgressBar(true)
        val client = ApiConfig.getApiService().getTransactions("Bearer $token")
        client.enqueue(object : retrofit2.Callback<TransactionResponse> {
            override fun onResponse(call: Call<TransactionResponse>, response: Response<TransactionResponse>) {
                setProgressBar(false)
                if (response.isSuccessful) {
                    val status = response.body()?.status
                    val data = response.body()?.data

                    if (status == true) {
                        val transactionAdapter = TransactionAdapter(data as ArrayList<TransactionItem>)
                        binding.rvTransaction.adapter = transactionAdapter
                        binding.rvTransaction.setHasFixedSize(true)
                    }
                }
            }

            override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
                setProgressBar(false)
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun setProgressBar(isVisible: Boolean) {
        binding.progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    companion object {
        private val TAG = TransactionsActivity::class.java.simpleName
    }
}