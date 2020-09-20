package ir.staryas.dormmanagement.util

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Communicator : ViewModel(){

    val userId = MutableLiveData<Any>()

    fun setUserId(msg:String){
        userId.value = msg
    }
}