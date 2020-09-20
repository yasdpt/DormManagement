package ir.staryas.dormmanagement.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.model.RoomStd

class RoomStdAdapter(val context: Context) : RecyclerView.Adapter<RoomStdAdapter.MyViewHolder>() {

    private var studentList : MutableList<RoomStd> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.student_item,parent,false)
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

        holder.tvStudentName.text = studentList[position].tsStudentName
        val stdImage = studentList[position].tsStudentImage
        var imageUrl = "http://staryas.ir/personnoimage.png"
        if (!stdImage.equals("null")){
            imageUrl = "http://staryas.ir/dorm/api/images/$stdImage"
        }
        val requestOption = RequestOptions()
            .placeholder(R.drawable.noimage)
            .centerCrop()
            .transforms(CenterCrop(), RoundedCorners(90))

        Glide.with(context).load(imageUrl)
            .apply(requestOption)
            .into(holder.ivStudent)
    }

    fun setRoomStdListItem(studentList: MutableList<RoomStd>){
        this.studentList = studentList
        notifyDataSetChanged()
    }


    lateinit var mLongClickListener: LongClickListener
    fun setOnLongClickListener(aLongClickListener: LongClickListener){
        mLongClickListener = aLongClickListener
    }

    interface LongClickListener {
        fun onLongClick(pos: Int, aView: View)
    }

    inner class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener, View.OnLongClickListener {
        override fun onLongClick(v: View?): Boolean {
            mLongClickListener.onLongClick(adapterPosition, v!!)
            return true
        }

        override fun onClick(v: View) {
            mClickListener.onClick(adapterPosition, v)
        }

        var tvStudentName: TextView = itemView!!.findViewById(R.id.tvStdFullName)
        var ivStudent: ImageView = itemView!!.findViewById(R.id.ivStudent)

        init {
            itemView!!.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
    }


}