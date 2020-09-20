package ir.staryas.dormmanagement.util

import android.app.Application

class MyApp : Application(){
    override fun onCreate() {
        super.onCreate()
        // set the apps font when started
        TypefaceUtil.setDefaultFont(this, "SANS_SERIF", "fonts/estedadlight.ttf")
    }
}