package ir.staryas.dormmanagement.activity.ui.more

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputLayout
import com.jaredrummler.materialspinner.MaterialSpinner
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.activity.*
import ir.staryas.dormmanagement.databinding.FragmentMoreBinding
import ir.staryas.dormmanagement.model.*
import ir.staryas.dormmanagement.networking.ApiClient
import ir.staryas.dormmanagement.networking.ApiService
import ir.staryas.dormmanagement.util.*
import kotlinx.android.synthetic.main.more_item.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MoreFragment : Fragment() {

    private lateinit var moreViewModel: MoreViewModel
    private var adapter: MoreAdapter? = null
    private lateinit var binding: FragmentMoreBinding
    private lateinit var prefManage: PrefManage

    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MoreFragment", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMoreBinding.inflate(inflater, container, false)



        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initComponent()
    }

    private fun initComponent(){

        prefManage = PrefManage(activity!!)

        activity?.run {
            viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        } ?: throw Throwable("invalid activity")

        //viewModel.updateIsData(Pair(first = false, second = false))
        //viewModel.updateIsStudentState(false)

        viewModel.updateActionBarTitle("بیشتر")
        moreViewModel = ViewModelProviders.of(this).get(MoreViewModel::class.java)
        val gvMore: GridView = binding.run { root.findViewById(R.id.gvFoods) }

        moreViewModel.moreList.observe(this, Observer {
            adapter = MoreAdapter(binding.root.context, it, activity!!)
            gvMore.adapter = adapter
        })

    }




}

class MoreAdapter(context: Context, private var moreList: ArrayList<More>,
                  var activity: FragmentActivity
) : BaseAdapter() {
    var context: Context? = context
    var prefManage = PrefManage(context)


    override fun getCount(): Int {
        return moreList.size
    }

    override fun getItem(position: Int): Any {
        return moreList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val more = this.moreList[position]
        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val foodView = inflater.inflate(R.layout.more_item, null)
        foodView.ivImage.setImageResource(more.image!!)
        foodView.tvTitle.text = more.title!!
        foodView.lyt_parent.setBackgroundResource(more.backColor!!)
        foodView.lyt_parent.setOnClickListener {
            when (more.action) {
                "addStudent" -> {
                    //foodView.tvTitle.text = "yas"
                    val intent = Intent(context, AddStudentActivity::class.java)
                    intent.putExtra("mode", "add")
                    context!!.startActivity(intent)
                }
                "searchStudent" -> {
                    showSearchStudentDialog(context!!)
                }
                "addPayment" -> {
                    showAddPaymentDialog(context!!)
                }
                "registerToRoom" -> {
                    showRTRDialog(context!!)
                }
                "settings" -> {
                    val intent = Intent(context, SettingsActivity::class.java)
                    context!!.startActivity(intent)
                }
                "logout" -> {
                    prefManage.setUserId(0)
                    prefManage.setIsUserLoggedIn(false)
                    prefManage.setUserRole("")
                    prefManage.setUsername("")
                    val intent = Intent(context, LoginActivity::class.java)
                    activity.finish()
                    context!!.startActivity(intent)
                }
            }
        }
        return foodView
    }


    private fun showSearchStudentDialog(context: Context) {
        val prefManage = PrefManage(context)

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_search_student)
        dialog.setCancelable(true)
        var studentPId = ""


        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        val etStdSSearch: EditText = dialog.findViewById(R.id.etStdSSearch)
        val tilStdSSearch: TextInputLayout = dialog.findViewById(R.id.tilStdSSearch)
        val llStdS: LinearLayout = dialog.findViewById(R.id.llStdS)
        val tvStdSName: TextView = dialog.findViewById(R.id.tvStdSName)

        val btnStdSSearch: Button = dialog.findViewById(R.id.btnStdSSearch)
        val btnStdSDeleteS: ImageButton = dialog.findViewById(R.id.btnStdSDeleteS)
        val btnSeeStdProfile: Button = dialog.findViewById(R.id.btnSeeStdProfile)
        lateinit var studentObj: StudentObj

        btnSeeStdProfile.isEnabled = false


        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        dialog.findViewById<ImageButton>(R.id.btnStdSClose).setOnClickListener {
            dialog.dismiss()
        }

        btnSeeStdProfile.setOnClickListener {
            val intent = Intent(context, StudentDetailActivity::class.java)
            intent.putExtraJson("myStdObj", studentObj)
            context.startActivity(intent)
            dialog.dismiss()
        }

        btnStdSSearch.setOnClickListener {
            val studentId = etStdSSearch.text.toString().trim()
            if (studentId.isEmpty() || studentId.length < 5) {
                tilStdSSearch.error = "شماره دانشجویی باید حداقل ۵ کاراکتر باشد"
            } else {
                tilStdSSearch.error = null
                try {
                    if (isNetworkAvailable(context)) {
                        btnStdSSearch.isEnabled = false

                        val apiClient = ApiClient()
                        val apiService = apiClient.getClient().create(ApiService::class.java)
                        val call = apiService.getStudentsById("sid", studentId)
                        call.enqueue(object : Callback<StudentMsg> {

                            override fun onResponse(
                                call: Call<StudentMsg>,
                                response: Response<StudentMsg>
                            ) {
                                btnStdSSearch.isEnabled = true
                                val res: List<Student>
                                if (response.body()!!.students != null) {
                                    res = response.body()!!.students!!
                                    if (res.isNotEmpty()) {
                                        if (studentId == res[0].studentPId) {
                                            val std = res[0]
                                            studentObj = StudentObj(
                                                std.studentId!!,
                                                std.studentFullName!!,
                                                std.studentImage!!,
                                                std.studentPId!!,
                                                std.studentNatId!!,
                                                std.studentPhone!!,
                                                std.studentDebt!!,
                                                std.studentCredit!!,
                                                std.studentStayTerms!!,
                                                std.createdAt!!,
                                                std.updatedAt!!
                                            )
                                            studentPId = etStdSSearch.text.toString().trim()
                                            etStdSSearch.setText("")
                                            tilStdSSearch.visibility = View.GONE
                                            btnStdSSearch.visibility = View.GONE
                                            llStdS.visibility = View.VISIBLE
                                            tvStdSName.text = res[0].studentFullName
                                            btnSeeStdProfile.isEnabled = true
                                        } else {
                                            toast("دانشجو مورد نظر پیدا نشد", context)
                                        }
                                    }
                                }
                            }

                            override fun onFailure(call: Call<StudentMsg>, t: Throwable) {
                                toast(context.getString(R.string.network_failure), context)
                            }


                        })
                    } else {
                        toast(context.getString(R.string.no_internet), context)
                    }
                } catch (e: Exception) {

                }
            }
        }

        btnStdSDeleteS.setOnClickListener {
            tvStdSName.text = ""
            llStdS.visibility = View.GONE
            tilStdSSearch.visibility = View.VISIBLE
            btnStdSSearch.visibility = View.VISIBLE
            btnSeeStdProfile.isEnabled = false
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }


    private fun showAddPaymentDialog(context: Context) {
        val prefManage = PrefManage(context)

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_add_payment)
        dialog.setCancelable(true)
        var studentPId = ""
        var tsId = 0


        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        val etPaymentSearch: EditText = dialog.findViewById(R.id.etPaymentSearch)
        val tilPaymentSearch: TextInputLayout = dialog.findViewById(R.id.tilPaymentSearch)
        val etPaymentTitle: EditText = dialog.findViewById(R.id.etPaymentTitle)
        val tilPaymentTitle: TextInputLayout = dialog.findViewById(R.id.tilPaymentTitle)
        val etPaymentPrice: EditText = dialog.findViewById(R.id.etPaymentPrice)
        val tilPaymentPrice: TextInputLayout = dialog.findViewById(R.id.tilPaymentPrice)
        val llPayment: LinearLayout = dialog.findViewById(R.id.llPayment)
        val tvStudentName: TextView = dialog.findViewById(R.id.tvPaymentSName)

        val btnPaymentSearch: Button = dialog.findViewById(R.id.btnPaymentSearch)
        val btnPaymentDeleteS: ImageButton = dialog.findViewById(R.id.btnPaymentDeleteS)
        val btnAddPayment: Button = dialog.findViewById(R.id.btnAddPayment)


        val tsList: ArrayList<RoomStd> = arrayListOf()
        val tsNameList: ArrayList<String> = arrayListOf()

        val chbPay: AppCompatCheckBox = dialog.findViewById(R.id.chbPay)
        val spinnerPayment: MaterialSpinner = dialog.findViewById(R.id.spinnerPayment)

        chbPay.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                try {
                    spinnerPayment.visibility = View.VISIBLE
                    val apiClient = ApiClient()
                    val service = apiClient.getClient().create(ApiService::class.java)
                    val call = service.getRoomStdByTerm(
                        "student",
                        "ts_id",
                        prefManage.getTerm()!!,
                        0,
                        studentPId
                    )
                    call.enqueue(object : Callback<RoomStdMsg> {
                        override fun onResponse(
                            call: Call<RoomStdMsg>?,
                            response: Response<RoomStdMsg>?
                        ) {

                            val resMsg = response!!.body()!!.success

                            if (resMsg == 1) {
                                if (response.body()!!.termStudents!!.isNotEmpty()) {
                                    val tss = response.body()!!.termStudents!!
                                    tsId = tss[0].tsId!!.toInt()
                                    for (i in tss.indices) {
                                        /*tsList.add(tss[i].tsTerm.toString())
                                        tsPriceList.add(tss[i].tsTuitionPrice!!.toInt())*/
                                        tsNameList.add(tss[i].tsTerm!!)
                                        tsList.add(tss[i])
                                    }
                                    tsNameList.let { spinnerPayment.setItems(it) }
                                    val tsPrice = tsList[0].tsTuitionPrice
                                    val tsTerm = tsList[0].tsTerm
                                    etPaymentPrice.setText(tsPrice.toString())
                                    etPaymentTitle.setText("پرداخت شهریه ترم " + tsTerm)
                                    etPaymentPrice.isEnabled = false
                                    etPaymentTitle.isEnabled = false
                                } else {
                                    chbPay.isChecked = false
                                    toast("دانشجو در اتاقی ثبت نشده است", dialog.context)
                                }
                            }
                        }

                        override fun onFailure(call: Call<RoomStdMsg>?, t: Throwable?) {

                        }
                    })
                } catch (e: Exception) {
                    Log.d("spinnerException", e.message!!)
                }
            } else {
                spinnerPayment.visibility = View.GONE
                etPaymentPrice.setText("")
                etPaymentTitle.setText("")
                etPaymentPrice.isEnabled = true
                etPaymentTitle.isEnabled = true
            }
        }



        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        dialog.findViewById<ImageButton>(R.id.btnPaymentClose).setOnClickListener {
            dialog.dismiss()
        }

        btnAddPayment.setOnClickListener {

            //toast(studentPId, context)
            if (chbPay.isChecked) {
                //toast("isChecked", dialog.context)
                val tsPrice = tsList[spinnerPayment.selectedIndex].tsTuitionPrice
                val tsTerm = tsList[spinnerPayment.selectedIndex].tsTerm
                etPaymentPrice.setText(tsPrice.toString())
                etPaymentTitle.setText("پرداخت شهریه ترم " + tsTerm)
                etPaymentPrice.isEnabled = false
                etPaymentTitle.isEnabled = false
                val paymentTitle = etPaymentTitle.text.toString().trim()
                val paymentPrice = etPaymentPrice.text.toString().trim()

                try {
                    val apiClient = ApiClient()
                    val apiService = apiClient.getClient().create(ApiService::class.java)
                    val call = apiService.managePayments(
                        "tuition",
                        0,
                        0,
                        studentPId,
                        prefManage.getUserId()!!,
                        paymentTitle,
                        paymentPrice.toInt(),
                        tsNameList[spinnerPayment.selectedIndex]
                    )


                    call.enqueue(object : Callback<Msg> {
                        override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                            if (response.body()!!.success == 1) {
                                toast(response.body()!!.message!!, context)
                                etPaymentPrice.setText("")
                                etPaymentSearch.setText("")
                                etPaymentTitle.setText("")
                                tvStudentName.text = ""
                                llPayment.visibility = View.GONE
                                chbPay.visibility = View.GONE
                                tilPaymentSearch.visibility = View.VISIBLE
                                btnPaymentSearch.visibility = View.VISIBLE
                                btnAddPayment.isEnabled = false


                                val callTerm = apiService.manageTermStudents(
                                    "edit",
                                    tsTerm!!,
                                    tsList[spinnerPayment.selectedIndex].tsId!!.toInt(),
                                    tsTerm,
                                    studentPId,
                                    tsList[spinnerPayment.selectedIndex].tsStudentRoom!!.toInt(),
                                    0, 1
                                )

                                callTerm.enqueue(object : Callback<Msg> {
                                    override fun onFailure(call: Call<Msg>, t: Throwable) {
                                        toast(context.getString(R.string.network_failure), context)
                                    }

                                    override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                                        //toast(response.body()!!.success.toString(), dialog.context )

                                    }

                                })


                                dialog.dismiss()
                            } else {
                                toast(response.body()!!.message!!, context)
                            }
                        }

                        override fun onFailure(call: Call<Msg>, t: Throwable) {
                            toast(context.getString(R.string.network_failure), context)
                        }
                    })




                } catch (e: Exception) {
                    toast(e.localizedMessage!!, dialog.context)
                }

            } else {
                //toast("notChecked", dialog.context)
                etPaymentPrice.isEnabled = true
                etPaymentPrice.isEnabled = true
                val paymentTitle = etPaymentTitle.text.toString().trim()
                val paymentPrice = etPaymentPrice.text.toString().trim()
                when {
                    paymentTitle.length < 3 -> tilPaymentTitle.error =
                        "عنوان باید حداقل ۳ کاراکتر باشد"
                    paymentPrice.isEmpty() -> tilPaymentPrice.error = "مبلغ نباید خالی باشد"
                    else -> {
                        tilPaymentPrice.error = null
                        tilPaymentTitle.error = null
                        try {
                            val apiClient = ApiClient()
                            val apiService = apiClient.getClient().create(ApiService::class.java)
                            val call = apiService.managePayments(
                                "add",
                                0,
                                0,
                                studentPId,
                                prefManage.getUserId()!!,
                                paymentTitle,
                                paymentPrice.toInt(),
                                tsNameList[spinnerPayment.selectedIndex]
                            )

                            call.enqueue(object : Callback<Msg> {
                                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                                    if (response.body()!!.success == 1) {
                                        toast(response.body()!!.message!!, context)
                                        dialog.dismiss()
                                        etPaymentPrice.setText("")
                                        etPaymentSearch.setText("")
                                        etPaymentTitle.setText("")
                                        tvStudentName.text = ""
                                        llPayment.visibility = View.GONE
                                        chbPay.visibility = View.GONE
                                        tilPaymentSearch.visibility = View.VISIBLE
                                        btnPaymentSearch.visibility = View.VISIBLE
                                        btnAddPayment.isEnabled = false
                                    } else {
                                        toast(response.body()!!.message!!, context)
                                    }
                                }

                                override fun onFailure(call: Call<Msg>, t: Throwable) {
                                    toast(context.getString(R.string.network_failure), context)
                                }
                            })
                        } catch (e: Exception) {

                        }
                    }
                }
            }


        }

        btnPaymentSearch.setOnClickListener {
            val studentId = etPaymentSearch.text.toString().trim()
            if (studentId.isEmpty() || studentId.length < 5) {
                tilPaymentSearch.error = "شماره دانشجویی باید حداقل ۵ کاراکتر باشد"
            } else {
                tilPaymentSearch.error = null
                try {
                    if (isNetworkAvailable(context)) {
                        btnPaymentSearch.isEnabled = false

                        val apiClient = ApiClient()
                        val apiService = apiClient.getClient().create(ApiService::class.java)
                        val call = apiService.getStudentsById("sid", studentId)
                        call.enqueue(object : Callback<StudentMsg> {

                            override fun onResponse(
                                call: Call<StudentMsg>,
                                response: Response<StudentMsg>
                            ) {
                                btnPaymentSearch.isEnabled = true
                                val res: List<Student>
                                if (response.body()!!.students != null) {
                                    res = response.body()!!.students!!
                                    if (res.isNotEmpty()) {
                                        if (studentId == res[0].studentPId) {
                                            studentPId = etPaymentSearch.text.toString().trim()
                                            etPaymentSearch.setText("")
                                            tilPaymentSearch.visibility = View.GONE
                                            btnPaymentSearch.visibility = View.GONE
                                            chbPay.visibility = View.VISIBLE
                                            llPayment.visibility = View.VISIBLE
                                            tvStudentName.text = res[0].studentFullName
                                            btnAddPayment.isEnabled = true
                                        } else {
                                            toast("دانشجوی مورد نظر پیدا نشد", context)
                                        }
                                    } else {
                                        toast("دانشجوی مورد نظر پیدا نشد", context)
                                    }
                                } else {
                                    toast("دانشجوی مورد نظر پیدا نشد", context)
                                }
                            }

                            override fun onFailure(call: Call<StudentMsg>, t: Throwable) {
                                btnPaymentSearch.isEnabled = true
                                toast(context.getString(R.string.network_failure), context)
                            }


                        })

                    } else {
                        toast(context.getString(R.string.no_internet), context)
                    }
                } catch (e: Exception) {
                    toast("error:" + e.message.toString(), context)
                }
            }
        }

        btnPaymentDeleteS.setOnClickListener {
            chbPay.isChecked = false
            chbPay.visibility = View.GONE
            spinnerPayment.visibility = View.GONE
            btnPaymentSearch.isEnabled = true
            tvStudentName.text = ""
            llPayment.visibility = View.GONE
            chbPay.visibility = View.GONE
            tilPaymentSearch.visibility = View.VISIBLE
            btnPaymentSearch.visibility = View.VISIBLE
            btnAddPayment.isEnabled = false
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun showRTRDialog(context: Context) {
        val prefManage = PrefManage(context)

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_register_to_room)
        dialog.setCancelable(true)
        var studentPId = ""


        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        val etRTRSearch: EditText = dialog.findViewById(R.id.etRTRSearch)
        val tilRTRSearch: TextInputLayout = dialog.findViewById(R.id.tilRTRSearch)
        val llRTR: LinearLayout = dialog.findViewById(R.id.llRTR)
        val tvRTRSName: TextView = dialog.findViewById(R.id.tvRTRSName)
        var roomIdList: ArrayList<Int> = arrayListOf()
        val roomList: ArrayList<String> = arrayListOf()
        val roomCapacityList: ArrayList<Int> = arrayListOf()
        var roomId = 0

        val btnRTRSearch: Button = dialog.findViewById(R.id.btnRTRSearch)
        val btnRTRDeleteS: ImageButton = dialog.findViewById(R.id.btnRTRDeleteS)
        val btnRTR: Button = dialog.findViewById(R.id.btnRTR)

        val spinnerRTR: MaterialSpinner = dialog.findViewById(R.id.spinnerRTR)

        try {
            //roomIdList = getRooms(spinnerRTR)
            val apiClient = ApiClient()
            val service = apiClient.getClient().create(ApiService::class.java)
            val call = service.getRooms("rid")
            call.enqueue(object : Callback<RoomMsg> {
                override fun onResponse(call: Call<RoomMsg>?, response: Response<RoomMsg>?) {

                    val resMsg = response!!.body()!!.success

                    if (resMsg == 1) {
                        val rooms = response.body()!!.rooms!!
                        roomId = rooms[0].roomId!!.toInt()
                        for (i in rooms.indices) {
                            roomList.add(rooms[i].roomName.toString())
                            roomIdList.add(rooms[i].roomId!!.toInt())
                            roomCapacityList.add(rooms[i].roomCapacity!!.toInt())
                        }
                        roomList.let { spinnerRTR.setItems(it) }
                    }
                }

                override fun onFailure(call: Call<RoomMsg>?, t: Throwable?) {

                }
            })
        } catch (e: Exception) {
            Log.d("spinnerException", e.message!!)
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        dialog.findViewById<ImageButton>(R.id.btnRTRClose).setOnClickListener {
            dialog.dismiss()
        }

        btnRTR.setOnClickListener {
            try {
                val roomCapacity = roomCapacityList[spinnerRTR.selectedIndex]
                var tsTuitionPrice = 0
                when (roomCapacity) {
                    8 -> tsTuitionPrice = prefManage.getT8()!!
                    10 -> tsTuitionPrice = prefManage.getT10()!!
                    12 -> tsTuitionPrice = prefManage.getT12()!!
                }

                val apiClient = ApiClient()
                val apiService = apiClient.getClient().create(ApiService::class.java)
                val call = apiService.manageTermStudents(
                    "add",
                    "",
                    0,
                    prefManage.getTerm()!!,
                    studentPId,
                    roomIdList[spinnerRTR.selectedIndex],
                    tsTuitionPrice,
                    0
                )

                call.enqueue(object : Callback<Msg> {
                    override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                        val res = response.body()!!
                        if (res.success == 1) {
                            toast(res.message!!, context)
                            dialog.dismiss()
                        } else {
                            toast(res.message!!, context)
                        }
                    }

                    override fun onFailure(call: Call<Msg>, t: Throwable) {
                        toast(context.getString(R.string.network_failure), context)
                        Log.d("RTRException", t.message!!)
                    }

                })

            } catch (e: Exception) {
                Log.d("RTRException", e.message!!)
            }
        }

        btnRTRSearch.setOnClickListener {
            val studentId = etRTRSearch.text.toString().trim()
            if (studentId.isEmpty() || studentId.length < 5) {
                tilRTRSearch.error = "شماره دانشجویی باید حداقل ۵ کاراکتر باشد"
            } else {
                tilRTRSearch.error = null
                try {
                    if (isNetworkAvailable(context)) {
                        btnRTRSearch.isEnabled = false

                        val apiClient = ApiClient()
                        val apiService = apiClient.getClient().create(ApiService::class.java)
                        val call = apiService.getStudentsById("sid", studentId)
                        call.enqueue(object : Callback<StudentMsg> {

                            override fun onResponse(
                                call: Call<StudentMsg>,
                                response: Response<StudentMsg>
                            ) {
                                btnRTRSearch.isEnabled = true
                                val res: List<Student>
                                if (response.body()!!.students != null) {
                                    res = response.body()!!.students!!
                                    if (res.isNotEmpty()) {
                                        if (studentId == res[0].studentPId) {
                                            studentPId = etRTRSearch.text.toString().trim()
                                            etRTRSearch.setText("")
                                            tilRTRSearch.visibility = View.GONE
                                            btnRTRSearch.visibility = View.GONE
                                            llRTR.visibility = View.VISIBLE
                                            tvRTRSName.text = res[0].studentFullName
                                            btnRTR.isEnabled = true
                                        } else {
                                            toast("دانشجو مورد نظر پیدا نشد", context)
                                        }
                                    } else {
                                        toast("دانشجوی مورد نظر پیدا نشد", context)
                                    }
                                } else {
                                    toast("دانشجوی مورد نظر پیدا نشد", context)
                                }
                            }

                            override fun onFailure(call: Call<StudentMsg>, t: Throwable) {
                                btnRTRSearch.isEnabled = true
                                toast(context.getString(R.string.network_failure), context)
                            }


                        })

                    } else {
                        toast(context.getString(R.string.no_internet), context)
                    }
                } catch (e: Exception) {
                    Log.d("RTRSearchException", e.message!!)
                }
            }
        }

        btnRTRDeleteS.setOnClickListener {
            btnRTRSearch.isEnabled = true
            tvRTRSName.text = ""
            llRTR.visibility = View.GONE
            tilRTRSearch.visibility = View.VISIBLE
            btnRTRSearch.visibility = View.VISIBLE
            btnRTR.isEnabled = false
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }


}