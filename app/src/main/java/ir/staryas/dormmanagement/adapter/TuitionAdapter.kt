package ir.staryas.dormmanagement.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.model.Tuition

class TuitionAdapter(val context: Context) : RecyclerView.Adapter<TuitionAdapter.MyViewHolder>() {

    private var tuitionList : MutableList<Tuition> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tuition,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return tuitionList.size
    }

    lateinit var mClickListener: ClickListener

    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }

    interface ClickListener {
        fun onClick(pos: Int, aView: View)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.tvTuitionName.text = "اتاق " + tuitionList[position].tuRoomCapacity + " نفره"
        holder.tvTuitionPrice.text = "میزان شهریه: " + tuitionList[position].tuPrice
    }

    fun setTuitionListItem(tuitionList: MutableList<Tuition>){
        this.tuitionList = tuitionList
        notifyDataSetChanged()
    }



    inner class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener {

        override fun onClick(v: View) {
            mClickListener.onClick(adapterPosition, v)
        }

        var tvTuitionName: TextView = itemView!!.findViewById(R.id.tvTuitionName)
        var tvTuitionPrice: TextView = itemView!!.findViewById(R.id.tvTuitionPrice)
        init {
            itemView!!.setOnClickListener(this)
        }
    }


}