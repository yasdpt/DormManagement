package ir.staryas.dormmanagement.activity.ui.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ir.staryas.dormmanagement.model.Room
import ir.staryas.dormmanagement.model.RoomMsg
import ir.staryas.dormmanagement.networking.ApiClient
import ir.staryas.dormmanagement.networking.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoomViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is room Fragment"
    }
    val text: LiveData<String> = _text


    private val _roomList = MutableLiveData<MutableList<Room>>().apply {
        value = getData()
    }

    val roomList: LiveData<MutableList<Room>> = _roomList

    private fun getData() : MutableList<Room>{
        val apiClient = ApiClient()
        val apiService: ApiService = apiClient.getClient().create(ApiService::class.java)
        val call: Call<RoomMsg> = apiService.getRooms("rid")
        var roomList = mutableListOf<Room>()
        call.enqueue(object : Callback<RoomMsg> {
            override fun onFailure(call: Call<RoomMsg>, t: Throwable) {

            }

            override fun onResponse(call: Call<RoomMsg>, response: Response<RoomMsg>) {

                if (response.body()?.rooms!!.isNotEmpty()){
                    roomList = (response.body()!!.rooms as MutableList<Room>)
                    //roomAdapter.setRoomListItem(roomList)
                }
            }

        })
        return roomList
    }
}