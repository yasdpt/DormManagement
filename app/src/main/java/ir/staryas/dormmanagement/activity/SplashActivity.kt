package ir.staryas.dormmanagement.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.util.PrefManage
import ir.staryas.dormmanagement.util.isNetworkAvailable
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAY: Long = 2000 //2 seconds
    private lateinit var prefManage:PrefManage
    private var isUserLoggedIn = false
    private var userRole = "admin"

    internal val mRunnable: Runnable = Runnable {
        if (!isFinishing) {
            if (isUserLoggedIn) {
                val intent = Intent(applicationContext, AdminActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        prefManage = PrefManage(this)
        isUserLoggedIn = prefManage.getIsUserLoggedIn()!!
        userRole = prefManage.getUserRole()!!
        //Initialize the Handler
        mDelayHandler = Handler()

        if (isNetworkAvailable(this)) {
            //Navigate with delay
            mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAY)
        } else {
            animation_view.visibility = View.GONE
            tvSplashNoInternet.visibility = View.VISIBLE
            btnSplashTryAgain.visibility = View.VISIBLE
        }

        btnSplashTryAgain.setOnClickListener {
            if (isNetworkAvailable(this)) {
                animation_view.visibility = View.VISIBLE
                tvSplashNoInternet.visibility = View.GONE
                btnSplashTryAgain.visibility = View.GONE
                mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAY)
            } else {
                Toast.makeText(
                    this, R.string.no_internet
                    , Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    public override fun onDestroy() {

        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(mRunnable)
        }

        super.onDestroy()
    }
}
