package ir.staryas.dormmanagement.activity.ui.supervisor

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.activity.MainViewModel
import ir.staryas.dormmanagement.adapter.AdminAdapter
import ir.staryas.dormmanagement.databinding.FragmentSupervisorBinding
import ir.staryas.dormmanagement.model.*
import ir.staryas.dormmanagement.networking.ApiClient
import ir.staryas.dormmanagement.networking.ApiService
import ir.staryas.dormmanagement.util.ViewDialog
import ir.staryas.dormmanagement.util.toast
import kotlinx.android.synthetic.main.fragment_supervisor.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.regex.Pattern

class SupervisorFragment : Fragment() {

    private lateinit var adminList: MutableList<Admin>
    private lateinit var adminAdapter: AdminAdapter
    private lateinit var viewDialog: ViewDialog
    private lateinit var root: View

    private lateinit var supervisorViewModel: SupervisorViewModel
    private lateinit var binding: FragmentSupervisorBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var supervisorSheet: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SupervisorFragment", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        supervisorViewModel =
            ViewModelProviders.of(this).get(SupervisorViewModel::class.java)
        binding = FragmentSupervisorBinding.inflate(inflater, container, false)

        initComponent(binding.root.context)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.run {
            viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        } ?: throw Throwable("invalid activity")
        viewModel.updateActionBarTitle("سرپرست ها")
        root = binding.root



        root.srlSupervisor.setOnRefreshListener {
            loadData()
            root.srlSupervisor.isRefreshing = false
        }

        root.fabSupervisor.setOnClickListener {
            showAddSupervisorDialog(root.context,"add",0,"","","","","","")
        }

    }

    private fun initComponent(context: Context){
        viewDialog = ViewDialog(context)
        adminList = mutableListOf()
        adminAdapter = AdminAdapter(context)
        val layoutManager = LinearLayoutManager(context)
        binding.root.rvSuperVisor.layoutManager = layoutManager
        binding.root.rvSuperVisor.adapter = adminAdapter
        loadData()

        adminAdapter.setOnItemClickListener(object : AdminAdapter.ClickListener{
            override fun onClick(pos: Int, aView: View) {
                val si = adminList[pos]
                showSupervisorInfoDialog(si.adminName!!, si.adminFamily!!, si.adminUsername!!, si.adminEmail!!, si.adminPhone!!)
            }
        })

        adminAdapter.setOnLongClickListener(object : AdminAdapter.LongClickListener{
            override fun onLongClick(pos: Int, aView: View) {
                val supervisor = adminList[pos]
                showSupervisorSheet(binding.root, supervisor.adminId!!.toInt(), supervisor.adminName!!, supervisor.adminFamily!!, supervisor.adminUsername!!, "", supervisor.adminEmail!!, supervisor.adminPhone!!)
            }

        })
    }

    @SuppressLint("SetTextI18n")
    private fun showSupervisorSheet(view: View, adminId: Int, aName: String, aFamily: String, aUsername: String, aPassword: String, aEmail:String, aPhone:String){
        supervisorSheet = Dialog(view.context)
        supervisorSheet.setContentView(R.layout.sheet_supervisor_item)
        supervisorSheet.setCancelable(true)

        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(supervisorSheet.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        val btnEditSupervisor:LinearLayout = supervisorSheet.findViewById(
            R.id.btnEditSupervisor
        )
        val btnDeleteSupervisor:LinearLayout = supervisorSheet.findViewById(
            R.id.btnDeleteSupervisor
        )

        val tvSheetSupervisorName: TextView = supervisorSheet.findViewById(R.id.tvSheetSupervisorName)

        tvSheetSupervisorName.text = "$aName $aFamily"

        btnEditSupervisor.setOnClickListener {
            showAddSupervisorDialog(view.context, "edit", adminId, aName, aFamily, aUsername, aPassword, aEmail, aPhone)
            hideSupervisorSheet()
        }

        btnDeleteSupervisor.setOnClickListener {
            val apiClient = ApiClient()
            val apiService = apiClient.getClient().create(ApiService::class.java)
            val call = apiService.deleteAdmin("delete", adminId)

            call.enqueue(object : Callback<Msg>{
                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                    val res = response.body()!!
                    if (res.success == 1){
                        hideSupervisorSheet()
                        loadData()
                        adminAdapter.notifyDataSetChanged()
                    } else {
                        hideSupervisorSheet()
                        toast("خطا", view.context)
                    }
                }

                override fun onFailure(call: Call<Msg>, t: Throwable) {
                    hideSupervisorSheet()
                    toast(view.context.getString(R.string.network_failure), view.context)
                }

            })

            hideSupervisorSheet()
        }

        supervisorSheet.show()
        supervisorSheet.window?.attributes = lp
    }

    private fun hideSupervisorSheet(){
        supervisorSheet.dismiss()
    }

    private fun loadData(){
        try {
            viewDialog.showDialog()
            getData()
        } catch (e:Exception){

        }
    }

    private fun getData(){
        val apiClient = ApiClient()
        val apiService: ApiService = apiClient.getClient().create(ApiService::class.java)
        val call: Call<AdminMsg> = apiService.getAdmins("aid")

        call.enqueue(object : Callback<AdminMsg>{
            override fun onFailure(call: Call<AdminMsg>, t: Throwable) {
                viewDialog.hideDialog()
                binding.root.tvSNDA.visibility = View.VISIBLE
                binding.root.rvSuperVisor.visibility = View.GONE
            }

            override fun onResponse(call: Call<AdminMsg>, response: Response<AdminMsg>) {
                viewDialog.hideDialog()
                binding.root.tvSNDA.visibility = View.GONE
                binding.root.rvSuperVisor.visibility = View.VISIBLE
                if (response.body() != null){
                    if (response.body()?.admins!!.isNotEmpty()){
                        adminList = (response.body()!!.admins as MutableList<Admin>)
                        adminAdapter.setAdminListItem(adminList)
                    } else {
                        binding.root.tvSNDA.visibility = View.VISIBLE
                        binding.root.rvSuperVisor.visibility = View.GONE
                    }
                } else {
                    binding.root.tvSNDA.visibility = View.VISIBLE
                    binding.root.rvSuperVisor.visibility = View.GONE
                }
            }

        })

    }

    private fun showAddSupervisorDialog(context: Context, mode:String, aId:Int, aName: String, aFamily: String, aUsername: String, aPassword: String, aEmail:String, aPhone:String){
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_add_supervisor)
        dialog.setCancelable(true)


        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        val etAdminName: EditText = dialog.findViewById(R.id.etAdminName)
        val tilAdminName: TextInputLayout = dialog.findViewById(R.id.tilAdminName)
        val etAdminFamily: EditText = dialog.findViewById(R.id.etAdminFamily)
        val tilAdminFamily: TextInputLayout = dialog.findViewById(R.id.tilAdminFamily)
        val etAdminUsername: EditText = dialog.findViewById(R.id.etAdminUsername)
        val tilAdminUsername: TextInputLayout = dialog.findViewById(R.id.tilAdminPassword)
        val etAdminPassword: EditText = dialog.findViewById(R.id.etAdminPassword)
        val tilAdminPassword: TextInputLayout = dialog.findViewById(R.id.tilAdminName)
        val etAdminEmail: EditText = dialog.findViewById(R.id.etAdminEmail)
        val tilAdminEmail: TextInputLayout = dialog.findViewById(R.id.tilAdminEmail)
        val etAdminPhone: EditText = dialog.findViewById(R.id.etAdminPhone)
        val tilAdminPhone: TextInputLayout = dialog.findViewById(R.id.tilAdminPhone)

        val btnAddSupervisor: Button = dialog.findViewById(R.id.btnAddSupervisor)



        if (mode == "edit"){
            etAdminName.setText(aName)
            etAdminFamily.setText(aFamily)
            etAdminUsername.setText(aUsername)
            etAdminPassword.setText(aPassword)
            etAdminEmail.setText(aEmail)
            etAdminPhone.setText(aPhone)
            btnAddSupervisor.text = "ویرایش سرپرست"
        }


        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        dialog.findViewById<ImageButton>(R.id.btnRoomClose).setOnClickListener {
            dialog.dismiss()
        }

        btnAddSupervisor.setOnClickListener {
            val adminName = etAdminName.text.toString().trim()
            val adminFamily = etAdminFamily.text.toString().trim()
            val adminUsername = etAdminUsername.text.toString().trim()
            val adminPassword = etAdminPassword.text.toString().trim()
            val adminEmail = etAdminEmail.text.toString().trim()
            val adminPhone = etAdminPhone.text.toString().trim()
            val pattern: Pattern = Pattern.compile("^[A-Za-z0-9._-]{2,25}\$")
            when {
                adminName.length < 3 -> tilAdminName.error = "عنوان باید حداقل ۳ کاراکتر باشد"
                adminFamily.length < 3  -> tilAdminFamily.error = "طبقه نباید خالی باشد"
                adminUsername.length < 3 || !pattern.matcher(adminUsername).matches() -> tilAdminUsername.error = "ظرفیت نباید خالی باشد"
                adminPassword.length < 6  -> tilAdminPassword.error = "رمز عبور نباید کمتر از ۶ کاراکتر باشد"
                adminEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(adminEmail).matches() -> tilAdminEmail.error = "لطفا یک ایمیل معتبر وارد کنید"
                adminPhone.length < 10 -> tilAdminPhone.error = "لطفا یک شماره تلفن معتبر وارد کنید"
                else -> {
                    tilAdminName.error = null
                    tilAdminFamily.error = null
                    tilAdminUsername.error = null
                    tilAdminPassword.error = null
                    tilAdminEmail.error = null
                    tilAdminPhone.error = null
                    try {
                        viewDialog.showDialog()
                        if (mode == "add"){
                            val apiClient = ApiClient()
                            val apiService = apiClient.getClient().create(ApiService::class.java)
                            val call = apiService.manageAdmins("add", 0, "supervisor", adminName, adminFamily, adminUsername,
                                adminPassword, adminEmail, adminPhone)

                            call.enqueue(object : Callback<Msg>{
                                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                                    viewDialog.hideDialog()
                                    val res = response.body()!!
                                    if (res.success == 1){
                                        dialog.dismiss()
                                        loadData()
                                        viewDialog.hideDialog()
                                    } else {
                                        toast("خطا", context)
                                    }
                                }

                                override fun onFailure(call: Call<Msg>, t: Throwable) {
                                    viewDialog.hideDialog()
                                    toast(context.getString(R.string.network_failure), context)
                                }

                            })
                        } else if (mode == "edit") {
                            val apiClient = ApiClient()
                            val apiService = apiClient.getClient().create(ApiService::class.java)
                            val call = apiService.manageAdmins("edit", aId, "supervisor", adminName, adminFamily, adminUsername,
                                adminPassword, adminEmail, adminPhone)

                            call.enqueue(object : Callback<Msg>{
                                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                                    viewDialog.hideDialog()
                                    val res = response.body()!!
                                    if (res.success == 1){
                                        dialog.dismiss()
                                        loadData()
                                        viewDialog.hideDialog()
                                    } else {
                                        toast("خطا", context)
                                    }
                                }

                                override fun onFailure(call: Call<Msg>, t: Throwable) {
                                    viewDialog.hideDialog()
                                    toast(context.getString(R.string.network_failure), context)
                                }

                            })
                        }
                    } catch (e:Exception) {

                    }
                }
            }


        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun showSupervisorInfoDialog(siName: String, siFamily: String, siUsername: String, siEmail: String, siPhone: String){
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_supervisor_info)
        dialog.setCancelable(true)


        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT



        val tvSIName: TextView = dialog.findViewById(R.id.tvSIName)
        val tvSIFamily: TextView = dialog.findViewById(R.id.tvSIFamily)
        val tvSIUsername: TextView = dialog.findViewById(R.id.tvSIUsername)
        val tvSIEmail: TextView = dialog.findViewById(R.id.tvSIEmail)
        val tvSIPhone: TextView = dialog.findViewById(R.id.tvSIPhone)

        tvSIName.text = tvSIName.text.toString() + " " + siName
        tvSIFamily.text = tvSIFamily.text.toString() + " " + siFamily
        tvSIUsername.text = tvSIUsername.text.toString() + " " + siUsername
        tvSIEmail.text = tvSIEmail.text.toString() + " " + siEmail
        tvSIPhone.text = tvSIPhone.text.toString() + " " + siPhone


        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        dialog.findViewById<ImageButton>(R.id.btnSIClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

}