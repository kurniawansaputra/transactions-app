package com.example.transactions.ui.addedittransaction

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.transactions.R
import com.example.transactions.data.response.DefaultResponse
import com.example.transactions.data.response.TransactionItem
import com.example.transactions.data.retrofit.ApiConfig
import com.example.transactions.databinding.ActivityAddEditTransactionBinding
import com.example.transactions.pref.UserPreference
import com.example.transactions.ui.transactions.TransactionsActivity
import com.example.transactions.util.loadImage
import com.example.transactions.util.reduceFileImage
import com.example.transactions.util.setLoading
import com.example.transactions.util.uriToFile
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import kotlin.properties.Delegates

class AddEditTransactionActivity : AppCompatActivity() {
    private var id by Delegates.notNull<Int>()
    private lateinit var name: String
    private lateinit var description: String
    private lateinit var amount: String
    private lateinit var token: String
    private var type: String = "income"
    private lateinit var activity: String
    private var currentImageUri: Uri? = null

    private lateinit var binding: ActivityAddEditTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddEditTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.rbIncome.isChecked = true

        setPref()
        getDetailTransaction()
        setToolbar()
        setListener()
        optionMenu()
    }

    private fun setPref() {
        val user = UserPreference.instance(this).getUser()
        if (user != null) {
            token = user.accessToken
        }

        activity = intent.getStringExtra("activity").toString()
    }

    private fun setToolbar() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            topAppBar.title = if (activity == "add") {
                "Tambah Jurnal"
            } else {
                "Edit Jurnal"
            }
        }
    }

    private fun optionMenu() {
        binding.apply {
            topAppBar.menu.findItem(R.id.menuDelete)?.isVisible = activity == "edit"

            topAppBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menuDelete -> {
                        deleteTransaction()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setListener() {
        binding.apply {
            rgType.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    rbIncome.id -> {
                        type = "income"
                    }

                    rbExpense.id -> {
                        type = "expense"
                    }
                }
            }

            ivPhoto.setOnClickListener {
                startGallery()
            }

            buttonSave.apply {
                text = if (activity == "add") {
                    "Tambah"
                } else {
                    "Perbarui"
                }
                setOnClickListener {
                    name = binding.editName.text.toString()
                    description = binding.editDescription.text.toString()
                    amount = binding.editAmount.text.toString()

                    if (name.isEmpty() || description.isEmpty() || amount.isEmpty()) {
                        Toast.makeText(
                            this@AddEditTransactionActivity,
                            "Harap lengkapi form terlebih dahulu",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    if (activity == "add") {
                        addTransaction()
                    } else {
                        editTransaction()
                    }
                }
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()

        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.ivPhoto.setImageURI(it)
            binding.labelTakePhoto.visibility = View.GONE
        }
    }

    private fun addTransaction() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")

            setLoading(this, true)

            val requestBodyName = name.toRequestBody("text/plain".toMediaType())
            val requestBodyDescription = description.toRequestBody("text/plain".toMediaType())
            val requestBodyAmount = amount.toRequestBody("text/plain".toMediaType())
            val requestBodyType = type.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBodyImage = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                requestImageFile
            )

            lifecycleScope.launch {
                try {
                    val apiService = ApiConfig.getApiService()
                    val successResponse = apiService.addTransaction(
                        "Bearer $token",
                        requestBodyName,
                        requestBodyDescription,
                        requestBodyAmount,
                        requestBodyType,
                        multipartBodyImage
                    )

                    Toast.makeText(
                        this@AddEditTransactionActivity,
                        "${successResponse.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    setLoading(this@AddEditTransactionActivity, false)

                    goToTransactions()

                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, DefaultResponse::class.java)
                    Toast.makeText(
                        this@AddEditTransactionActivity,
                        "${errorResponse.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    setLoading(this@AddEditTransactionActivity, false)
                }
            }
        } ?: Toast.makeText(
            this@AddEditTransactionActivity,
            "Harap lengkapi form terlebih dahulu",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun editTransaction() {
        val imageFile = currentImageUri?.let { uri ->
            uriToFile(uri, this).reduceFileImage()
        }

        Log.d("Image File", "showImage: ${imageFile?.path}")

        setLoading(this, true)

        val requestBodyName = name.toRequestBody("text/plain".toMediaType())
        val requestBodyDescription = description.toRequestBody("text/plain".toMediaType())
        val requestBodyAmount = amount.toRequestBody("text/plain".toMediaType())
        val requestBodyType = type.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile?.asRequestBody("image/jpeg".toMediaType())
        val multipartBodyImage = requestImageFile?.let {
            MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                it
            )
        }

        lifecycleScope.launch {
            try {
                val apiService = ApiConfig.getApiService()
                val successResponse = apiService.updateTransaction(
                    "Bearer $token",
                    id,
                    requestBodyName,
                    requestBodyDescription,
                    requestBodyAmount,
                    requestBodyType,
                    multipartBodyImage
                )

                Toast.makeText(
                    this@AddEditTransactionActivity,
                    "${successResponse.message}",
                    Toast.LENGTH_SHORT
                ).show()
                setLoading(this@AddEditTransactionActivity, false)

                goToTransactions()

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, DefaultResponse::class.java)
                Toast.makeText(
                    this@AddEditTransactionActivity,
                    "${errorResponse.message}",
                    Toast.LENGTH_SHORT
                ).show()
                setLoading(this@AddEditTransactionActivity, false)
            }
        }
    }

    private fun deleteTransaction() {
        setLoading(this, true)
        val client = ApiConfig.getApiService().deleteTransaction("Bearer $token", id)
        client.enqueue(object : retrofit2.Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                setLoading(this@AddEditTransactionActivity, false)
                if (response.isSuccessful) {
                    val status = response.body()?.status

                    if (status == true) {
                        goToTransactions()
                    }
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                setLoading(this@AddEditTransactionActivity, false)
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun getDetailTransaction() {
        if (activity == "edit") {

            val transaction = if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra("EXTRA_TRANSACTION", TransactionItem::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("EXTRA_TRANSACTION")
            }
            if (transaction != null) {
                binding.apply {
                    id = transaction.id.toString().toInt()
                    editName.setText(transaction.name)
                    editDescription.setText(transaction.description)
                    editAmount.setText(transaction.amount.toString())
                    if (transaction.type == "income") {
                        rbIncome.isChecked = true
                    } else {
                        rbExpense.isChecked = true
                    }

                    loadImage(ivPhoto, transaction.image.toString())
                    labelTakePhoto.visibility = View.GONE
                }
            }
        }
    }

    private fun goToTransactions() {
        val intent = Intent(this, TransactionsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    companion object {
        private val TAG = AddEditTransactionActivity::class.java.simpleName
    }
}