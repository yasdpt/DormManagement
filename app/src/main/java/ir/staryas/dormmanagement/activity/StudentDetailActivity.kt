package ir.staryas.dormmanagement.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.activity.AdminActivity.Companion.tools
import ir.staryas.dormmanagement.adapter.MyPagerAdapter
import ir.staryas.dormmanagement.fragment.PaymentFragment
import ir.staryas.dormmanagement.fragment.TermsFragment
import ir.staryas.dormmanagement.fragment.ViolationFragment
import ir.staryas.dormmanagement.model.Msg
import ir.staryas.dormmanagement.model.StudentObj
import ir.staryas.dormmanagement.networking.ApiClient
import ir.staryas.dormmanagement.networking.ApiService
import ir.staryas.dormmanagement.util.*
import kotlinx.android.synthetic.main.activity_student_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class StudentDetailActivity : AppCompatActivity(), PaymentFragment.OnPaymentFragmentInteractionListener, ViolationFragment.OnViolationFragmentInteractionListener, TermsFragment.OnTermsFragmentInteractionListener {
    override fun onPaymentFragmentInteraction(uri: Uri) {

    }

    override fun onViolationFragmentInteraction(uri: Uri) {

    }

    override fun onTermsFragmentInteraction(uri: Uri) {

    }

    private lateinit var std: StudentObj

    private var model: Communicator?=null
    private lateinit var prefManage: PrefManage

    private lateinit var menuItem:MenuItem

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)

        initToolbar()
        initComponent()

        val fragmentAdapter = MyPagerAdapter(supportFragmentManager)
        vpStudent.adapter = fragmentAdapter

        tlStudent.setupWithViewPager(vpStudent)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.detailToolbar)
        ViewCompat.setLayoutDirection(toolbar, ViewCompat.LAYOUT_DIRECTION_RTL)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = "پروفایل دانشجو"
        supportActionBar!!.elevation = 0.0f
        AdminActivity.tools.setSystemBarColor(this,
            R.color.colorPrimaryDark
        )
        AdminActivity.tools.setSystemBarLight(this)
    }

    @SuppressLint("SetTextI18n")
    private fun initComponent() {
        std = intent.getJsonExtra("myStdObj", StudentObj::class.java)!!
        prefManage = PrefManage(this)


        val stdImage = std.studentImage
        var imageUrl = "http://staryas.ir/personnoimage.png"
        if (stdImage != "null"){
            imageUrl = "http://staryas.ir/dorm/api/images/$stdImage"
        }





        val requestOption = RequestOptions()
            .placeholder(R.drawable.noimage)
            .centerCrop()
            .transforms(CenterCrop(), RoundedCorners(150))

        Glide.with(this).load(imageUrl)
            .apply(requestOption)
            .into(ivStdImage)

        tvStdFullName.text = tvStdFullName.text.toString()+std.studentFullName
        tvStdPId.text = tvStdPId.text.toString()+std.studentPId
        tvStdNatId.text = tvStdNatId.text.toString()+std.studentNatId
        tvStdPhone.text = tvStdPhone.text.toString()+std.studentPhone
        val formatter = DecimalFormat("###,###,###")
        val debt = std.studentDebt.replace(",","").toDouble()
        val formattedDebt:String = formatter.format(debt)
        val credit = std.studentCredit.replace(",","").toDouble()
        val formattedCredit:String = formatter.format(credit)
        tvStdDebt.text = tvStdDebt.text.toString()+formattedDebt + " تومان"
        tvStdCredit.text = tvStdCredit.text.toString()+formattedCredit + " تومان"
        val pDate = std.createdAt.split(" ")
        tvStdDate.text = tvStdDate.text.toString()+pDate[0].replace("-","/")

        val viewModel = ViewModelProviders.of(this).get(Communicator::class.java)
        viewModel.setUserId(std.studentPId)


        btnClearCredit.setOnClickListener {
            clearCredit(std.studentPId)
        }
    }

    private fun clearCredit(sId: String) {
        val apiClient = ApiClient()
        val apiService = apiClient.getClient().create(ApiService::class.java)
        val call = apiService.clearCredit(sId)

        call.enqueue(object : Callback<Msg>{
            override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                if (response.body() != null){
                    if (response.body()!!.success == 1){
                        toast(response.body()!!.message!!, this@StudentDetailActivity)
                        tvStdCredit.setText("طلبکاری:0 تومان")
                    }
                }
            }

            override fun onFailure(call: Call<Msg>, t: Throwable) {
                toast(getString(R.string.network_failure), this@StudentDetailActivity)
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_student, menu)
        tools.changeMenuIconColor(menu, resources.getColor(R.color.white))
        menuItem = menu.findItem(R.id.action_edit_student)
        if (prefManage.getUserRole() == "supervisor"){
            menuItem.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }else if (item.itemId == R.id.action_edit_student) {
            val intent = Intent(this, AddStudentActivity::class.java)
            intent.putExtra("mode", "edit")
            intent.putExtraJson("studentObj", std)
            startActivity(intent)
        } else {
            toast(item.title.toString(), applicationContext)
        }
        return super.onOptionsItemSelected(item)
    }

}
