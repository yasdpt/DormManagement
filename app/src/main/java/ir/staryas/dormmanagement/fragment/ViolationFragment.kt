package ir.staryas.dormmanagement.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.adapter.ViolationsAdapter
import ir.staryas.dormmanagement.model.*
import ir.staryas.dormmanagement.networking.ApiClient
import ir.staryas.dormmanagement.networking.ApiService
import ir.staryas.dormmanagement.util.Communicator
import ir.staryas.dormmanagement.util.PrefManage
import ir.staryas.dormmanagement.util.toast
import kotlinx.android.synthetic.main.dialog_add_payment.*
import kotlinx.android.synthetic.main.fragment_violation.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ViolationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var model: Communicator?=null
    private var param2: String? = null
    private var listener: OnViolationFragmentInteractionListener? = null
    private var studentId = ""
    private lateinit var violationSheet: Dialog

    private lateinit var root: View
    private lateinit var rvViolation: RecyclerView
    private lateinit var violationsAdapter: ViolationsAdapter
    private lateinit var violationsList: MutableList<Violation>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_violation, container, false)
        model= activity?.run {
            ViewModelProviders.of(activity!!).get(Communicator::class.java)
        } ?: throw Exception("invalid activity")

        model!!.userId.observe(activity!!, Observer {
            studentId = it as String
        })

        initComponent(root)

        return root
    }

    private fun initComponent(view: View){
        violationsList = mutableListOf()
        val layoutManager = LinearLayoutManager(view.context)
        rvViolation = view.findViewById(R.id.rvStdViolations)
        violationsAdapter = ViolationsAdapter(view.context)
        rvViolation.layoutManager = layoutManager
        rvViolation.adapter = violationsAdapter

        try {
            loadData(root)
        } catch (e:Exception){
            Log.d("getPayments", e.message!!)
        }

        violationsAdapter.setOnItemClickListener(object : ViolationsAdapter.ClickListener{
            override fun onClick(pos: Int, aView: View) {
                val violation = violationsList[pos]
                showViolationInfoDialog(root.context, violation.violationTitle!!, violation.violationCost!!, violation.createdAt!!, violation.violationDetail!!, violation.violationTerm!!)
            }
        })

        violationsAdapter.setOnLongClickListener(object : ViolationsAdapter.LongClickListener{
            override fun onLongClick(pos: Int, aView: View) {
                val violation = violationsList[pos]
                showViolationSheet(root, violation.studentId!!, violation.roomId!!.toInt(), violation.violationTerm!!,
                    violation.violationId!!.toInt(), violation.violationTitle!!, violation.violationCost!!.toInt(),
                    violation.createdAt!!, violation.violationDetail!!)
            }

        })
    }

    private fun loadData(view: View){
        val apiClient = ApiClient()
        val apiService = apiClient.getClient().create(ApiService::class.java)
        val call = apiService.getViolationByStd("v_id", studentId)
        lateinit var violationMsg: ViolationMsg


        call.enqueue(object : Callback<ViolationMsg> {
            override fun onResponse(call: Call<ViolationMsg>, response: Response<ViolationMsg>) {
                val res = response.body()!!
                if (res.success == 1){
                    if (res.violations!!.isNotEmpty()){
                        violationMsg = res
                        view.tvViolationNoData.visibility = View.GONE
                        rvViolation.visibility = View.VISIBLE
                        violationsList = violationMsg.violations as MutableList<Violation>
                        violationsAdapter.setViolationListItem(violationsList)
                    } else {
                        rvViolation.visibility = View.GONE
                        view.tvViolationNoData.visibility = View.VISIBLE
                    }
                } else {
                    rvViolation.visibility = View.GONE
                    view.tvViolationNoData.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<ViolationMsg>, t: Throwable) {
                rvViolation.visibility = View.GONE
                toast(getString(R.string.network_failure), view.context)
                view.tvViolationNoData.visibility = View.VISIBLE
            }

        })
    }



    private fun showViolationSheet(view: View,sId: String,roomId: Int, vTerm: String,  vId: Int, vTitle: String, vCost: Int, vDate: String, vDetail: String){
        violationSheet = Dialog(view.context)
        violationSheet.setContentView(R.layout.sheet_item_violation)
        violationSheet.setCancelable(true)

        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(violationSheet.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        val btnEditViolation:LinearLayout = violationSheet.findViewById(
            R.id.btnEditViolation
        )
        val btnDeleteViolation:LinearLayout = violationSheet.findViewById(
            R.id.btnDeleteViolation
        )

        val tvSheetViolationName: TextView = violationSheet.findViewById(R.id.tvSheetViolationName)

        tvSheetViolationName.text = vTitle

        btnEditViolation.setOnClickListener {
            showEditViolationDialog(view.context, sId, roomId, vId, vTitle, vTerm, vCost, vDetail)
            hideViolationSheet()
        }

        btnDeleteViolation.setOnClickListener {
            val apiClient = ApiClient()
            val apiService = apiClient.getClient().create(ApiService::class.java)
            val call = apiService.deleteViolation("delete", vId, vCost, sId)

            call.enqueue(object : Callback<Msg>{
                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                    val res = response.body()!!
                    if (res.success == 1){
                        loadData(root)
                        violationsAdapter.notifyDataSetChanged()
                        hideViolationSheet()
                    } else {
                        toast("خطا", view.context)
                        hideViolationSheet()
                    }
                }

                override fun onFailure(call: Call<Msg>, t: Throwable) {
                    toast(view.context.getString(R.string.network_failure), view.context)
                    hideViolationSheet()
                }

            })

            hideViolationSheet()
        }

        violationSheet.show()
        violationSheet.window?.attributes = lp
    }

    private fun hideViolationSheet(){
        violationSheet.dismiss()
    }




    private fun showEditViolationDialog(context: Context, sId: String, roomId: Int, vId: Int, vTitle: String,
                                        vTerm: String, vCost: Int,
                                        vDetail: String) {
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


        etViolationTitle.setText(vTitle)
        etViolationCost.setText(vCost.toString())
        etViolationDetail.setText(vDetail)

        btnAddViolation.setText("ویرایش تخلف")


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
                        val call = apiService.manageViolations("edit",vCost,vId,sId,roomId,prefManage.getUserId()!!,violationTitle,violationDetail, violationCost.toInt(), vTerm)

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
                                    loadData(root)
                                    violationsAdapter.notifyDataSetChanged()
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




    @SuppressLint("SetTextI18n")
    private fun showViolationInfoDialog(context: Context, vTitle: String, vCost: String, vDate: String, vDetail: String, vTerm: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_violation_info)
        dialog.setCancelable(true)


        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT



        val tvVDTitle: TextView = dialog.findViewById(R.id.tvVDTitle)
        val tvVDCost: TextView = dialog.findViewById(R.id.tvVDCost)
        val tvVDDate: TextView = dialog.findViewById(R.id.tvVDDate)
        val tvVDDetail: TextView = dialog.findViewById(R.id.tvVDDetail)
        val tvVDTerm: TextView = dialog.findViewById(R.id.tvVDTerm)

        tvVDTitle.text = tvVDTitle.text.toString() + " " + vTitle
        tvVDCost.text = tvVDCost.text.toString() + " " + vCost
        tvVDDate.text = tvVDDate.text.toString() + " " + vDate
        tvVDDetail.text = tvVDDetail.text.toString() + " " + vDetail
        tvVDTerm.text = tvVDTerm.text.toString() + " " + vTerm


        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        dialog.findViewById<ImageButton>(R.id.btnVDClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onViolationFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnViolationFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface OnViolationFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onViolationFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViolationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}
