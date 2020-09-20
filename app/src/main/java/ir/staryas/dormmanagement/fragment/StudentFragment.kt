package ir.staryas.dormmanagement.fragment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout

import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.activity.MainViewModel
import ir.staryas.dormmanagement.activity.StudentDetailActivity
import ir.staryas.dormmanagement.adapter.RoomStdAdapter
import ir.staryas.dormmanagement.model.*
import ir.staryas.dormmanagement.networking.ApiClient
import ir.staryas.dormmanagement.networking.ApiService
import ir.staryas.dormmanagement.util.*
import kotlinx.android.synthetic.main.dialog_add_payment.*
import kotlinx.android.synthetic.main.dialog_register_to_room.*
import kotlinx.android.synthetic.main.fragment_student.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class StudentFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentStudentInteractionListener? = null
    private lateinit var viewModel: MainViewModel
    private lateinit var root: View
    private lateinit var studentSheet: Dialog

    private lateinit var studentList: MutableList<RoomStd>
    private lateinit var studentAdapter: RoomStdAdapter
    private lateinit var viewDialog: ViewDialog
    private lateinit var prefManage: PrefManage
    private var roomId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            initComponent(activity!!)
            viewModel.updateIsAtStudentState(true)
        } else {
            viewModel.updateIsAtStudentState(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_student, container, false)

        root.srlRoomStudent.setOnRefreshListener {
            loadData()
            root.srlRoomStudent.isRefreshing = false
        }


        initComponent(root.context)

        return root
    }

    private fun initComponent(context: Context){
        activity?.run {
            viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        } ?: throw Throwable("invalid activity")
        viewModel.updateIsStudentState(true)
        viewModel.updateIsAtStudentState(true)
        viewModel.name.observe(activity!!, Observer {
            viewModel.updateActionBarTitle(it)
        })



        viewDialog = ViewDialog(context)
        prefManage = PrefManage(context)
        studentList = mutableListOf()
        studentAdapter = RoomStdAdapter(context)
        val layoutManager = LinearLayoutManager(context)
        root.rvRoomStudent.layoutManager = layoutManager
        root.rvRoomStudent.adapter = studentAdapter

        viewModel.roomId.observe(activity!!, Observer {
            roomId = it
            studentList = mutableListOf()
            loadData()
            viewDialog.hideDialog()
        })

        studentAdapter.setOnItemClickListener(object : RoomStdAdapter.ClickListener {
            override fun onClick(pos: Int, aView: View) {

                viewDialog.showDialog()
                val apiClient = ApiClient()
                val apiService = apiClient.getClient().create(ApiService::class.java)
                val call = apiService.getStudentsById("sid", studentList[pos].tsStudentId!!)
                call.enqueue(object : Callback<StudentMsg> {

                    override fun onResponse(
                        call: Call<StudentMsg>,
                        response: Response<StudentMsg>
                    ) {
                        viewDialog.hideDialog()
                        val res: List<Student>
                        if (response.body()!!.students != null) {
                            res = response.body()!!.students!!
                            if (res.isNotEmpty()) {
                                if (studentList[pos].tsStudentId!! == res[0].studentPId) {
                                    val std = res[0]
                                    val studentObj = StudentObj(std.studentId!!,
                                        std.studentFullName!!,
                                        std.studentImage!!,
                                        std.studentPId!!,
                                        std.studentNatId!!,
                                        std.studentPhone!!,
                                        std.studentDebt!!,
                                        std.studentCredit!!,
                                        std.studentStayTerms!!,
                                        std.createdAt!!,
                                        std.updatedAt!!)
                                    val intent = Intent(context, StudentDetailActivity::class.java)
                                    intent.putExtraJson("myStdObj", studentObj)
                                    context.startActivity(intent)
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
                        viewDialog.hideDialog()
                        btnRTRSearch.isEnabled = true
                        toast(context.getString(R.string.network_failure), context)
                    }
                })

            }
        })

        studentAdapter.setOnLongClickListener(object : RoomStdAdapter.LongClickListener{
            override fun onLongClick(pos: Int, aView: View) {
                val student = studentList[pos]
                showStudentSheet(root, student.tsId!!.toInt(), student.tsStudentId!!, student.tsStudentName!!, student.tsStudentRoom!!.toInt(), student.tsTuitionPrice!!.toInt(), student.tsHasPaidTuition!!.toInt())
            }
        })

    }

    private fun showStudentSheet(view: View, tsId: Int, sId: String, sName: String, roomId: Int, tPrice: Int, tsHasPaidTuition: Int){
        studentSheet = Dialog(view.context)
        studentSheet.setContentView(R.layout.sheet_student_item)
        studentSheet.setCancelable(true)

        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(studentSheet.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        val btnAddSViolation: LinearLayout = studentSheet.findViewById(
            R.id.btnAddSViolation
        )
        val btnDeleteRoomStd: LinearLayout = studentSheet.findViewById(
            R.id.btnDeleteRoomStd
        )

        val tvSheetStudentName: TextView = studentSheet.findViewById(R.id.tvSheetStudentName)

        tvSheetStudentName.text = sName

        btnAddSViolation.setOnClickListener {
            showAddViolationDialog(view.context, sName,sId, roomId)
            hideStudentSheet()
        }

        btnDeleteRoomStd.setOnClickListener {
            val apiClient = ApiClient()
            val apiService = apiClient.getClient().create(ApiService::class.java)
            val call = apiService.deleteTermStudent("delete", tsId, prefManage.getTerm()!!, tPrice, sId, tsHasPaidTuition)

            call.enqueue(object : Callback<Msg>{
                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                    val res = response.body()!!
                    if (res.success == 1){
                        hideStudentSheet()
                        loadData()
                        studentAdapter.notifyDataSetChanged()
                    } else {
                        hideStudentSheet()
                        toast("خطا", view.context)
                    }
                }

                override fun onFailure(call: Call<Msg>, t: Throwable) {
                    hideStudentSheet()
                    toast(view.context.getString(R.string.network_failure), view.context)
                }

            })

            hideStudentSheet()
        }

        studentSheet.show()
        studentSheet.window?.attributes = lp
    }

    private fun hideStudentSheet(){
        studentSheet.dismiss()
    }

    private fun showAddViolationDialog(context: Context, studentName: String, sId: String, roomId: Int) {
        val prefManage = PrefManage(context)

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_add_violation)
        dialog.setCancelable(true)


        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        val etViolationTitle: EditText = dialog.findViewById(R.id.etViolationTitle)
        val tilViolationTitle: TextInputLayout = dialog.findViewById(R.id.tilViolationTitle)
        val etViolationDetail: EditText = dialog.findViewById(R.id.etViolationDetail)
        val tilViolationDetail: TextInputLayout = dialog.findViewById(R.id.tilViolationDetail)
        val etViolationCost: EditText = dialog.findViewById(R.id.etViolationCost)
        val tilViolationCost: TextInputLayout = dialog.findViewById(R.id.tilViolationCost)
        val llViolation: LinearLayout = dialog.findViewById(R.id.llViolation)
        val tvViolationSName:TextView = dialog.findViewById(R.id.tvViolationSName)

        val btnAddViolation: Button = dialog.findViewById(R.id.btnAddViolation)

        var student = Student()




        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        dialog.findViewById<ImageButton>(R.id.btnViolationClose).setOnClickListener {
            dialog.dismiss()
        }


        try {
            val apiClient = ApiClient()
            val apiService = apiClient.getClient().create(ApiService::class.java)
            val call = apiService.getStudentsById("sid", sId)
            call.enqueue(object : Callback<StudentMsg> {

                override fun onResponse(
                    call: Call<StudentMsg>,
                    response: Response<StudentMsg>
                ) {
                    val res: List<Student>
                    if (response.body()!!.students != null) {
                        res = response.body()!!.students!!
                        if (res.isNotEmpty()) {
                            if (sId == res[0].studentPId) {
                                student = res[0]
                                tvViolationSName.text = res[0].studentFullName
                                llViolation.visibility = View.VISIBLE
                                btnAddViolation.isEnabled = true
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
        } catch (e:Exception) {

        }



        btnAddViolation.setOnClickListener {
            val violationTitle = etViolationTitle.text.toString().trim()
            val violationDetail = etViolationDetail.text.toString().trim()
            val violationCost = etViolationCost.text.toString().trim()
            when {
                violationTitle.length < 3 -> tilViolationTitle.error = "عنوان باید حداقل ۳ کاراکتر باشد"
                violationDetail.length < 10 -> tilViolationDetail.error = "توضیحات باید حداقل ۱۰ کاراکتر باشد"
                violationCost.isEmpty() -> tilViolationCost.error = "مبلغ نباید خالی باشد"
                else -> {
                    tilViolationTitle.error = null
                    tilViolationDetail.error = null
                    tilViolationCost.error = null
                    try {
                        val apiClient = ApiClient()
                        val apiService = apiClient.getClient().create(ApiService::class.java)
                        val call = apiService.manageViolations("add",0,0,sId,roomId,prefManage.getUserId()!!,violationTitle,violationDetail, violationCost.toInt(), prefManage.getTerm()!!)

                        call.enqueue(object : Callback<Msg> {
                            override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                                if (response.body()!!.success == 1){
                                    toast(response.body()!!.message!!, context)
                                    dialog.dismiss()
                                    etViolationTitle.setText("")
                                    etViolationDetail.setText("")
                                    etViolationCost.setText("")
                                    tvViolationSName.text = ""
                                    llViolation.visibility = View.GONE
                                    btnAddViolation.isEnabled = false
                                    dialog.dismiss()
                                }
                            }

                            override fun onFailure(call: Call<Msg>, t: Throwable) {
                                toast(context.getString(R.string.network_failure), context)
                            }
                        })

                    } catch (e:Exception) {

                    }
                }
            }


        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun loadData(){
        try {
            viewDialog.showDialog()
            //toast(prefManage.getTerm()!! + " " + roomId, root.context)
            getData()
        } catch (e: Exception){
            Log.d("loadData", e.message!!)
        }
    }

    private fun getData() {
        val apiClient = ApiClient()
        val apiService: ApiService = apiClient.getClient().create(ApiService::class.java)
        val call: Call<RoomStdMsg> = apiService.getRoomStdByTerm("room","ts_id",prefManage.getTerm()!!,roomId, "")

        call.enqueue(object : Callback<RoomStdMsg> {
            override fun onFailure(call: Call<RoomStdMsg>, t: Throwable) {
                viewDialog.hideDialog()
                root.tvSNDA.visibility = View.VISIBLE
                root.rvRoomStudent.visibility = View.GONE
            }

            override fun onResponse(call: Call<RoomStdMsg>, response: Response<RoomStdMsg>) {
                viewDialog.hideDialog()
                root.tvSNDA.visibility = View.GONE
                root.rvRoomStudent.visibility = View.VISIBLE
                if (response.body() != null){
                    if (response.body()?.termStudents!!.isNotEmpty()){
                        studentList = (response.body()!!.termStudents as MutableList<RoomStd>)
                        studentAdapter.setRoomStdListItem(studentList)
                    } else {
                        root.tvSNDA.visibility = View.VISIBLE
                        root.rvRoomStudent.visibility = View.GONE
                    }
                } else {
                    root.tvSNDA.visibility = View.VISIBLE
                    root.rvRoomStudent.visibility = View.GONE
                }
            }

        })
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentStudentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentStudentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentStudentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentStudentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StudentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}
