package com.example.transactions.ui.transactions

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.transactions.R
import com.example.transactions.data.response.TransactionItem
import com.example.transactions.databinding.ItemRowTransactionBinding
import com.example.transactions.ui.addedittransaction.AddEditTransactionActivity
import com.example.transactions.util.formatToIDR

class TransactionAdapter(private val transactionList: List<TransactionItem>): RecyclerView.Adapter<TransactionAdapter.ViewHolder>()  {

    class ViewHolder (val binding: ItemRowTransactionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemRowTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(transactionList[position]) {
                binding.apply {
                    textTitle.text = name
                    textDescription.text = description
                    textAmount.text = amount?.let { formatToIDR(it) }
                    textDate.text = date

                    if (type == "expense") {
                        ivType.setImageResource(R.drawable.ic_expanse)
                        textAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.red_color))
                    } else {
                        ivType.setImageResource(R.drawable.ic_income)
                        textAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.green_color))
                    }

                    root.setOnClickListener {
                        val moveWithObjectIntent = Intent(itemView.context, AddEditTransactionActivity::class.java)
                        moveWithObjectIntent.putExtra("activity", "edit")
                        moveWithObjectIntent.putExtra("EXTRA_TRANSACTION", transactionList[position])
                        itemView.context.startActivity(moveWithObjectIntent)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = transactionList.size
}