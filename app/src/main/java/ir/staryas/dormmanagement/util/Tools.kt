package ir.staryas.dormmanagement.util

import android.app.Activity
import android.graphics.PorterDuff
import android.os.Build
import android.view.Menu
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat

class Tools {

    fun setSystemBarLight(act: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view = act.findViewById<View>(android.R.id.content)
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = flags
        }
    }

    fun setSystemBarColor(act: Activity, @ColorRes color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = act.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = act.resources.getColor(color)
        }
    }

    fun changeMenuIconColor(menu: Menu, @ColorInt color: Int) {
        for (i in 0 until menu.size()) {
            val drawable = menu.getItem(i).icon ?: continue
            drawable.mutate()
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

    fun getTimeDifference(persianDate: PersianDate, timestamp:String):String {
        var difference= ""
        val pdformatdate = PersianDateFormat("Y-m-d")

        val pdformattime = PersianDateFormat("H:i:s")
        val datenow = pdformatdate.format(persianDate)
        val timenow = pdformattime.format(persianDate)



        val datetime = timestamp.split(" ")
        val date = datetime[0]
        val time = datetime[1]

        val datesplit1 = date.split("-")
        var yearThen:Int = datesplit1[0].toInt()
        var monthThen:Int = datesplit1[1].toInt()
        var dayThen:Int = datesplit1[2].toInt()

        val datesplit2 = datenow.split("-")
        var yearNow:Int = datesplit2[0].toInt()
        var monthNow:Int = datesplit2[1].toInt()
        var dayNow:Int = datesplit2[2].toInt()

        if(monthNow<monthThen)
        {
            yearNow-=1
            monthNow+=12
        }
        if(dayNow<dayThen){
            monthNow-=1
            dayNow+=30
        }



        if (date == datenow){
            val tnNow = timenow.split(":")
            val tnThen = time.split(":")

            if (tnNow[0].toInt() == tnThen[0].toInt())
            {
                difference = "درحال برگزاری"
            } else if (tnNow[0].toInt() > tnThen[0].toInt())
            {
                val diff:Int = tnNow[0].toInt() - tnThen[0].toInt()
                difference = "برگزار شده"
            } else {
                difference = "برگزار نشده"
            }
        } else {
            val diffDays =((yearNow-yearThen)*365)+((monthNow-monthThen)*30)+(dayNow-dayThen)
            if (diffDays >= 0){
                difference = "برگزار شده"
            } else {
                difference = "برگذار نشده"
            }
        }

        return difference

    }

}