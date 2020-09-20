package ir.staryas.dormmanagement.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.model.Room

class RoomAdapter(val context: Context) : RecyclerView.Adapter<RoomAdapter.MyViewHolder>() {

    private var roomList : MutableList<Room> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.room_item,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    lateinit var mClickListener: ClickListener
    lateinit var mLongClickListener: LongClickListener

    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }

    fun setOnLongClickListener(aLongClickListener: LongClickListener){
        mLongClickListener = aLongClickListener
    }

    interface ClickListener {
        fun onClick(pos: Int, aView: View)
    }

    interface LongClickListener {
        fun onLongClick(pos: Int, aView: View)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.tvRoomName.text = roomList[position].roomName
        holder.tvRoomFloor.text = "طبقه اتاق: "+roomList[position].roomFloor
        holder.tvRoomCapacity.text = "ظرفیت اتاق: " + roomList[position].roomCapacity
    }

    fun setRoomListItem(roomList: MutableList<Room>){
        this.roomList = roomList
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

        var tvRoomName: TextView = itemView!!.findViewById(R.id.tvRoomName)
        var tvRoomFloor: TextView = itemView!!.findViewById(R.id.tvRoomFloor)
        var tvRoomCapacity: TextView = itemView!!.findViewById(R.id.tvRoomCapacity)

        init {
            itemView!!.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
    }


}