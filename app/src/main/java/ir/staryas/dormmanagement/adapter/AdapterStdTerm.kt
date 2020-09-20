package ir.staryas.dormmanagement.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.model.RoomStd

class AdapterStdTerm(val context: Context) : RecyclerView.Adapter<AdapterStdTerm.MyViewHolder>() {

    private var studentList : MutableList<RoomStd> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.std_term_item,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return studentList.size
    }

    lateinit var mClickListener: ClickListener

    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }

    interface ClickListener {
        fun onClick(pos: Int, aView: View)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.tvStdTermName.text = "نام ترم: " + studentList[position].tsTerm
        holder.tvStdHPT.text = if(studentList[position].tsHasPaidTuition == "1") "شهریه پرداخت شده" else "شهریه پرداخت نشده"
    }

    fun setRoomStdListItem(studentList: MutableList<RoomStd>){
        this.studentList = studentList
        notifyDataSetChanged()
    }



    inner class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener {

        override fun onClick(v: View) {
            mClickListener.onClick(adapterPosition, v)
        }

        var tvStdTermName: TextView = itemView!!.findViewById(R.id.tvStdTermName)
        var tvStdHPT: TextView = itemView!!.findViewById(R.id.tvStdHPT)

        init {
            itemView!!.setOnClickListener(this)
        }
    }


}