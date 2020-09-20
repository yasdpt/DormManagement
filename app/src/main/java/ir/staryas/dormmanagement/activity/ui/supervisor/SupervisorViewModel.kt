package ir.staryas.dormmanagement.activity.ui.supervisor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SupervisorViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is supervisor Fragment"
    }
    val text: LiveData<String> = _text
}