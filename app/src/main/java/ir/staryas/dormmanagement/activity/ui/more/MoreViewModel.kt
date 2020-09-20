package ir.staryas.dormmanagement.activity.ui.more

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.model.More

class MoreViewModel : ViewModel() {


    private val _moreList = MutableLiveData<ArrayList<More>>().apply {
        value = arrayListOf()
        value?.add(More("ثبت دانشجو", R.color.green_800,R.drawable.ic_add_student,"addStudent"))
        value?.add(More("جستجو دانشجو", R.color.pink_800,R.drawable.ic_student,"searchStudent"))
        value?.add(More("ثبت پرداخت", R.color.cyan_800,R.drawable.ic_payment,"addPayment"))
        value?.add(More("تخصیص اتاق", R.color.yellow_800,R.drawable.ic_student,"registerToRoom"))
        value?.add(More("تنظیمات", R.color.red_800,R.drawable.ic_settings,"settings"))
        value?.add(More("خروج", R.color.deep_purple_800,R.drawable.ic_logout,"logout"))
    }

    val moreList: LiveData<ArrayList<More>> = _moreList



}