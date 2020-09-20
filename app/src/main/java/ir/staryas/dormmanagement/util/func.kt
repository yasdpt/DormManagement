package ir.staryas.dormmanagement.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.*


fun toast(msg:String,context: Context){
    Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
    return if (connectivityManager is ConnectivityManager) {
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        networkInfo?.isConnected ?: false
    } else false
}
