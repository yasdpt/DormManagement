package ir.staryas.dormmanagement.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title
    fun updateActionBarTitle(title: String) = _title.postValue(title)


    private val _isStudent = MutableLiveData<Boolean>()
    val isStudent: LiveData<Boolean>
        get() = _isStudent
    fun updateIsStudentState(isStudent: Boolean) = _isStudent.postValue(isStudent)

    private val _isAtStudent = MutableLiveData<Boolean>()
    val isAtStudent: LiveData<Boolean>
        get() = _isAtStudent
    fun updateIsAtStudentState(isAtStudent: Boolean) = _isAtStudent.postValue(isAtStudent)


    private val _name = MutableLiveData<String>()
    val name: LiveData<String>
        get() = _name
    fun updateName(name: String) = _name.postValue(name)

    private val _roomId = MutableLiveData<Int>()
    val roomId: LiveData<Int>
        get() = _roomId
    fun updateRoomId(roomId: Int) = _roomId.postValue(roomId)


}