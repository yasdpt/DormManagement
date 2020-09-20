package ir.staryas.dormmanagement.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.model.UsrMsg
import ir.staryas.dormmanagement.networking.ApiClient
import ir.staryas.dormmanagement.networking.ApiService
import ir.staryas.dormmanagement.util.PrefManage
import ir.staryas.dormmanagement.util.ViewDialog
import ir.staryas.dormmanagement.util.isNetworkAvailable
import ir.staryas.dormmanagement.util.toast
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private val apiClient = ApiClient()
    private lateinit var viewDialog: ViewDialog
    private lateinit var prefManage: PrefManage
    private var rText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initComponent()

    }

    private fun initComponent() {
        viewDialog = ViewDialog(this)
        prefManage = PrefManage(this)

        // button onClickListener for login
        btnLogin.setOnClickListener {
            if (isNetworkAvailable(this)) {
                val username = tieUsername.text.toString().trim()
                val password = tiePassword.text.toString().trim()
                val selectedRadio = radioGroup.checkedRadioButtonId
                val radioBtn = findViewById<RadioButton>(selectedRadio)

                when(radioBtn.text.toString()){
                    "مدیر" -> {
                        rText = "admin"
                    }
                    "سرپرست" -> {
                        rText = "supervisor"
                    }
                }
                try {
                    if(validate(username,password))
                    {
                        btnLogin.isEnabled = false
                        viewDialog.showDialog()
                        login(rText,username,password)
                    }
                } catch (e:Exception){

                }
            } else {
                toast(getString(R.string.no_internet), this)
            }
        }
    }

    private fun login(mode:String,username:String,password:String) {
        val client = apiClient.getClient().create(ApiService::class.java)
        val call = client.login(mode,username,password)

        call.enqueue(object : Callback<UsrMsg> {
            override fun onFailure(call: Call<UsrMsg>, t: Throwable) {
                viewDialog.hideDialog()
                btnLogin.isEnabled = true
                toast(getString(R.string.network_failure),this@LoginActivity)
            }

            override fun onResponse(call: Call<UsrMsg>, response: Response<UsrMsg>) {
                viewDialog.hideDialog()
                btnLogin.isEnabled = true

                // get response values
                val success = response.body()!!.success
                val message = response.body()!!.message

                // check if operation was successful
                if (success == 1) {

                    // save user credentials in sharedPreferences
                    prefManage.setIsUserLoggedIn(true)
                    prefManage.setUserId(response.body()!!.admin!![0].adminId!!.toInt())
                    val fullname = response.body()!!.admin!![0].adminName + " " + response.body()!!.admin!![0].adminFamily
                    prefManage.setFullName(fullname)
                    prefManage.setUsername(response.body()!!.admin!![0].adminUsername!!)
                    prefManage.setUserRole(rText)
                    toast(message!!,this@LoginActivity)

                    // go to next activity if successful
                    val intent = Intent(this@LoginActivity,AdminActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    toast(message!!,this@LoginActivity)
                }
            }

        })
    }

    private fun validate(username: String,password: String):Boolean {
        var valid = true

        val pattern: Pattern = Pattern.compile("^[A-Za-z0-9._-]{2,25}\$")

        if (username.isEmpty() || !pattern.matcher(username).matches())
        {
            tieUsername.error = "لطفا یک نام کاربری معتبر وارد کنید"
            valid = false
        } else {
            tieUsername.error = null
        }

        if (password.isEmpty())
        {
            tiePassword.error = "کلمه عبور خالی است"
            valid = false
        } else {
            tiePassword.error = null
        }

        return valid
    }

}
