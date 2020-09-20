package ir.staryas.dormmanagement.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.model.Payment
import java.text.DecimalFormat

class PaymentsAdapter(val context: Context) : RecyclerView.Adapter<PaymentsAdapter.MyViewHolder>() {

    private var paymentList : MutableList<Payment> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return paymentList.size
    }

    lateinit var mClickListener: ClickListener

    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }

    interface ClickListener {
        fun onClick(pos: Int, aView: View)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val formatter = DecimalFormat("###,###,###")
        val prcstr = paymentList[position].paymentPrice!!.replace(",","").toDouble()
        val formattedPrice:String = formatter.format(prcstr) + " تومان"
        holder.tvPaymentAmount.text = formattedPrice

        holder.tvPaymentDate.text = paymentList[position].createdAt!!.replace("-", "/")

    }

    fun setPaymentListItem(paymentList: MutableList<Payment>){
        this.paymentList = paymentList
        notifyDataSetChanged()
    }



    inner class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener {

        override fun onClick(v: View) {
            mClickListener.onClick(adapterPosition, v)
        }

        var tvPaymentAmount: TextView = itemView!!.findViewById(R.id.tvPaymentAmount)
        var tvPaymentDate: TextView = itemView!!.findViewById(R.id.tvPaymentDate)

        init {
            itemView!!.setOnClickListener(this)
        }
    }


}