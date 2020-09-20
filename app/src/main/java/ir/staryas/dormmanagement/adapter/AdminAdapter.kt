package ir.staryas.dormmanagement.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.model.Admin

class AdminAdapter(val context: Context) : RecyclerView.Adapter<AdminAdapter.MyViewHolder>() {

    private var adminList : MutableList<Admin> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.admin_item,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return adminList.size
    }

    lateinit var mClickListener: ClickListener


    lateinit var mLongClickListener: LongClickListener
    fun setOnLongClickListener(aLongClickListener: LongClickListener){
        mLongClickListener = aLongClickListener
    }
    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }

    interface ClickListener {
        fun onClick(pos: Int, aView: View)
    }

    interface LongClickListener {
        fun onLongClick(pos: Int, aView: View)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.tvAdminName.text = adminList[position].adminName + " " + adminList[position].adminFamily
        holder.tvAdminPhone.text = adminList[position].adminPhone
    }

    fun setAdminListItem(adminList: MutableList<Admin>){
        this.adminList = adminList
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

        var tvAdminName: TextView = itemView!!.findViewById(R.id.tvAdminName)
        var tvAdminPhone: TextView = itemView!!.findViewById(R.id.tvAdminPhone)

        init {
            itemView!!.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
    }


}