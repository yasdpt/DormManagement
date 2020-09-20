package ir.staryas.dormmanagement.activity

import android.app.Dialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.adapter.TuitionAdapter
import ir.staryas.dormmanagement.model.*
import ir.staryas.dormmanagement.networking.ApiClient
import ir.staryas.dormmanagement.networking.ApiService
import ir.staryas.dormmanagement.util.PrefManage
import ir.staryas.dormmanagement.util.ViewDialog
import ir.staryas.dormmanagement.util.isNetworkAvailable
import ir.staryas.dormmanagement.util.toast
import kotlinx.android.synthetic.main.activity_settings.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsActivity : AppCompatActivity() {

    private lateinit var viewDialog: ViewDialog
    private lateinit var prefManage: PrefManage
    var termIdList: ArrayList<Int> = arrayListOf()
    val termList: ArrayList<String> = arrayListOf()
    private lateinit var tuitionList: MutableList<Tuition>
    private lateinit var tuitionAdapter: TuitionAdapter
    private var termId = 0

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initToolbar()

        initComponent()

        btnGoSettings.setOnClickListener {
            prefManage.setTerm(termList[sTermSetting.selectedIndex])
            val intent = Intent(this, AdminActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
            finish()
        }

        btnAddNewTerm.setOnClickListener {
            showAddTermDialog()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.settingsToolbar)
        ViewCompat.setLayoutDirection(toolbar, ViewCompat.LAYOUT_DIRECTION_RTL)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = "تنظیمات"
        supportActionBar!!.elevation = 0.0f
        AdminActivity.tools.setSystemBarColor(this,
            R.color.colorPrimaryDark
        )
        AdminActivity.tools.setSystemBarLight(this)
    }

    private fun initComponent() {
        prefManage = PrefManage(this)
        viewDialog = ViewDialog(this)

        tuitionList = mutableListOf()
        tuitionAdapter = TuitionAdapter(this)
        val layoutManager = LinearLayoutManager(this)
        rvTuition.layoutManager = layoutManager
        rvTuition.adapter = tuitionAdapter


        try {
            viewDialog.showDialog()
            getTermsData()
            getTuitionList()
        } catch (e: Exception) {
            Log.d("spinnerException", e.message!!)
        }


        tuitionAdapter.setOnItemClickListener(object : TuitionAdapter.ClickListener{
            override fun onClick(pos: Int, aView: View) {
                showTuitionEditDialog(tuitionList[pos].tuId!!.toInt(), tuitionList[pos].tuPrice!!.toInt())
            }
        })

    }

    private fun showTuitionEditDialog(tuId: Int, tuPrice: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_edit_tuition)
        dialog.setCancelable(true)


        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        val etTuitionPrice: EditText = dialog.findViewById(R.id.etTuitionPrice)
        val tilTuitionPrice: TextInputLayout = dialog.findViewById(R.id.tilTuitionPrice)

        val btnEditTuition: Button = dialog.findViewById(R.id.btnEditTuition)


        etTuitionPrice.setText(tuPrice.toString())


        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        dialog.findViewById<ImageButton>(R.id.btnTuitionClose).setOnClickListener {
            dialog.dismiss()
        }

        btnEditTuition.setOnClickListener {
            val tuitionPrice = etTuitionPrice.text.toString().trim()
            if (tuitionPrice.isEmpty()){
                tilTuitionPrice.error = "مبلغ نباید خالی باشد"
            } else {
                tilTuitionPrice.error = null
                try {
                    if (isNetworkAvailable(dialog.context)){
                        val apiClient = ApiClient()
                        val apiService = apiClient.getClient().create(ApiService::class.java)
                        val call = apiService.updateTuition(tuId, tuitionPrice.toInt())

                        call.enqueue(object : Callback<Msg> {
                            override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                                val res = response.body()!!
                                if (res.success == 1) {
                                    toast(res.message!!, dialog.context)
                                    when (tuId) {
                                        1 -> prefManage.setT8(tuPrice)
                                        2 -> prefManage.setT10(tuPrice)
                                        3 -> prefManage.setT12(tuPrice)
                                    }
                                    dialog.dismiss()
                                    tuitionList = mutableListOf()
                                    getTuitionList()
                                } else {
                                    toast("عملیات انجام نشد", dialog.context)
                                }
                            }

                            override fun onFailure(call: Call<Msg>, t: Throwable) {
                                toast(getString(R.string.network_failure), dialog.context)
                                Log.d("RTRException", t.message!!)
                            }

                        })
                    } else {
                        toast(getString(R.string.no_internet), dialog.context)
                    }
                } catch (e: Exception) {
                    Log.d("RTRException", e.message!!)
                }
            }
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun showAddTermDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_add_term)
        dialog.setCancelable(true)


        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        val etTermName: EditText = dialog.findViewById(R.id.etTermName)
        val tilTermName: TextInputLayout = dialog.findViewById(R.id.tilTermName)

        val btnAddTerm: Button = dialog.findViewById(R.id.btnAddTerm)




        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        dialog.findViewById<ImageButton>(R.id.btnTermClose).setOnClickListener {
            dialog.dismiss()
        }

        btnAddTerm.setOnClickListener {
            val termName = etTermName.text.toString().trim()
            if (termName.isEmpty() || termName.length > 3){
                tilTermName.error = "لطفا یک نام ترم درست را وارد کنید"
            } else {
                tilTermName.error = null
                try {
                    if (isNetworkAvailable(dialog.context)){
                        val apiClient = ApiClient()
                        val apiService = apiClient.getClient().create(ApiService::class.java)
                        val call = apiService.manageTerms("add", 0, termName)

                        call.enqueue(object : Callback<Msg> {
                            override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                                val res = response.body()!!
                                if (res.success == 1) {
                                    toast(res.message!!, dialog.context)
                                    dialog.dismiss()
                                    termList.clear()
                                    termIdList.clear()
                                    getTermsData()
                                } else {
                                    toast("عملیات انجام نشد", dialog.context)
                                }
                            }

                            override fun onFailure(call: Call<Msg>, t: Throwable) {
                                toast(getString(R.string.network_failure), dialog.context)
                                Log.d("RTRException", t.message!!)
                            }

                        })
                    } else {
                        toast(getString(R.string.no_internet), dialog.context)
                    }
                } catch (e: Exception) {
                    Log.d("RTRException", e.message!!)
                }
            }
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun getTermsData(){
        val apiClient = ApiClient()
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.getTerms("termId")
        call.enqueue(object : Callback<TermMsg> {
            override fun onResponse(call: Call<TermMsg>?, response: Response<TermMsg>?) {

                val resMsg = response!!.body()!!.success

                if (resMsg == 1) {
                    val terms = response.body()!!.terms!!
                    termId = terms[0].termId!!.toInt()
                    for (i in terms.indices) {
                        termList.add(terms[i].termName.toString())
                        termIdList.add(terms[i].termId!!.toInt())
                    }
                    termList.let { sTermSetting.setItems(it) }
                }
            }

            override fun onFailure(call: Call<TermMsg>?, t: Throwable?) {

            }
        })
    }

    private fun getTuitionList() {
        val apiClient = ApiClient()
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.getTuitions("tuId")

        call.enqueue(object : Callback<TuitionMsg>{

            override fun onResponse(call: Call<TuitionMsg>, response: Response<TuitionMsg>) {
                viewDialog.hideDialog()
                val res = response.body()
                if (res!!.tuitions != null){
                    tuitionList = res.tuitions as MutableList<Tuition>
                    tuitionAdapter.setTuitionListItem(tuitionList)
                } else {
                    toast("خطا در دریافت اطلاعات", this@SettingsActivity)
                    finish()
                }
            }

            override fun onFailure(call: Call<TuitionMsg>, t: Throwable) {
                viewDialog.hideDialog()
                toast(getString(R.string.network_failure), this@SettingsActivity)
                finish()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else {
            toast(item.title.toString(), applicationContext)
        }
        return super.onOptionsItemSelected(item)
    }

}
