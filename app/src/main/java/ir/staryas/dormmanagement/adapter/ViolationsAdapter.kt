package ir.staryas.dormmanagement.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.model.Violation

class ViolationsAdapter(val context: Context) : RecyclerView.Adapter<ViolationsAdapter.MyViewHolder>() {

    private var violationList : MutableList<Violation> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_violation,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return violationList.size
    }

    lateinit var mClickListener: ClickListener

    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }

    interface ClickListener {
        fun onClick(pos: Int, aView: View)
    }


    lateinit var mLongClickListener: LongClickListener
    fun setOnLongClickListener(aLongClickListener: LongClickListener){
        mLongClickListener = aLongClickListener
    }

    interface LongClickListener {
        fun onLongClick(pos: Int, aView: View)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.tvViolationTitle.text = violationList[position].violationTitle
        holder.tvViolationDate.text = violationList[position].createdAt!!.replace("-", "/")
    }

    fun setViolationListItem(violationList: MutableList<Violation>){
        this.violationList = violationList
        notifyDataSetChanged()
    }



    inner class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener, View.OnLongClickListener {
        override fun onLongClick(v: View?): Boolean {
            mLongClickListener.onLongClick(adapterPosition, v!!)
            return true
        }

        override fun onClick(v: View) {
            mClickListener.onClick(adapterPosition, v)
        }

        var tvViolationTitle: TextView = itemView!!.findViewById(R.id.tvViolationTitle)
        var tvViolationDate: TextView = itemView!!.findViewById(R.id.tvViolationDate)

        init {
            itemView!!.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
    }


}