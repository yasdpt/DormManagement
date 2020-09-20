package ir.staryas.dormmanagement.activity.ui.room

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.plusAssign
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import com.jaredrummler.materialspinner.MaterialSpinner
import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.activity.MainViewModel
import ir.staryas.dormmanagement.adapter.RoomAdapter
import ir.staryas.dormmanagement.databinding.FragmentRoomBinding
import ir.staryas.dormmanagement.model.*
import ir.staryas.dormmanagement.navigation.KeepStateNavigator
import ir.staryas.dormmanagement.networking.ApiClient
import ir.staryas.dormmanagement.networking.ApiService
import ir.staryas.dormmanagement.util.*
import kotlinx.android.synthetic.main.fragment_room.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class RoomFragment : Fragment() {

    private lateinit var roomList: MutableList<Room>
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var viewDialog: ViewDialog
    private lateinit var prefManage: PrefManage
    private lateinit var root: View

    private lateinit var roomViewModel: RoomViewModel
    private lateinit var binding: FragmentRoomBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var roomSheet: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("RoomFragment", "onCreate")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            prefManage = PrefManage(activity!!)
            viewModel.updateActionBarTitle("اتاق ها" + " - " + "نیمسال تحصیلی " + prefManage.getTerm()!!)
            viewModel.updateIsStudentState(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        roomViewModel =
            ViewModelProviders.of(this).get(RoomViewModel::class.java)
        binding = FragmentRoomBinding.inflate(inflater, container, false)


        initComponent(binding.root.context)


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.run {
            viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        } ?: throw Throwable("invalid activity")
        prefManage = PrefManage(activity!!)
        viewModel.updateActionBarTitle("اتاق ها" + " - " + "نیمسال تحصیلی " + prefManage.getTerm()!!)
        viewModel.updateIsStudentState(false)
        root = binding.root

        if (prefManage.getUserRole() == "supervisor"){
            root.fabRoom.hide()
        }



        root.srlRoom.setOnRefreshListener {
            loadData()
            root.srlRoom.isRefreshing = false
        }

        root.fabRoom.setOnClickListener {
            showAddRoomDialog(root.context,"add", 0, "", 0, 0)
        }

    }

    private fun initComponent(context: Context){
        viewDialog = ViewDialog(context)
        prefManage = PrefManage(context)
        roomList = mutableListOf()
        roomAdapter = RoomAdapter(context)
        val layoutManager = LinearLayoutManager(context)
        binding.root.rvRoom.layoutManager = layoutManager
        binding.root.rvRoom.adapter = roomAdapter
        loadData()


        roomAdapter.setOnItemClickListener(object : RoomAdapter.ClickListener{
            override fun onClick(pos: Int, aView: View) {
                val navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
                val navHostFragment = activity!!.supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!
                val navigator = KeepStateNavigator(
                    activity!!,
                    navHostFragment.childFragmentManager,
                    R.id.nav_host_fragment
                )
                navController.navigatorProvider += navigator
                viewModel.updateName(roomList[pos].roomName!!)
                viewModel.updateRoomId(roomList[pos].roomId!!.toInt())
                navController.navigate(R.id.action_navigation_home_to_navigation_student)
            }
        })

        roomAdapter.setOnLongClickListener(object : RoomAdapter.LongClickListener{
            override fun onLongClick(pos: Int, aView: View) {
                showRoomSheet(binding.root, roomList[pos].roomId!!.toInt(), roomList[pos].roomName!!, roomList[pos].roomFloor!!.toInt(), roomList[pos].roomCapacity!!.toInt())
            }

        })
    }

    private fun showRoomSheet(view: View, roomI: Int, roomN: String, roomF: Int, roomC: Int){
        roomSheet = Dialog(view.context)
        roomSheet.setContentView(R.layout.sheet_room_item)
        roomSheet.setCancelable(true)

        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(roomSheet.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        val btnEditRoom:LinearLayout = roomSheet.findViewById(
            R.id.btnEditRoom
        )
        val btnDeleteRoom:LinearLayout = roomSheet.findViewById(
            R.id.btnDeleteRoom
        )

        val tvSheetRoomName: TextView = roomSheet.findViewById(R.id.tvSheetRoomName)

        tvSheetRoomName.text = roomN

        btnEditRoom.setOnClickListener {
            showAddRoomDialog(view.context, "edit",roomI, roomN, roomF, roomC)
            hideRoomSheet()
        }

        btnDeleteRoom.setOnClickListener {
            val apiClient = ApiClient()
            val apiService = apiClient.getClient().create(ApiService::class.java)
            val call = apiService.deleteRoom("delete", roomI)

            call.enqueue(object : Callback<Msg>{
                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                    val res = response.body()!!
                    if (res.success == 1){
                        hideRoomSheet()
                        loadData()
                        roomAdapter.notifyDataSetChanged()
                    } else {
                        hideRoomSheet()
                        toast("خطا", view.context)
                    }
                }

                override fun onFailure(call: Call<Msg>, t: Throwable) {
                    hideRoomSheet()
                    toast(view.context.getString(R.string.network_failure), view.context)
                }

            })

            hideRoomSheet()
        }

        roomSheet.show()
        roomSheet.window?.attributes = lp
    }

    private fun hideRoomSheet(){
        roomSheet.dismiss()
    }

    private fun loadData(){
        try {
            viewDialog.showDialog()
            getData()
            try {
                if (prefManage.getTerm() == "null"){
                    if (isNetworkAvailable(binding.root.context)){
                        setTerm()

                    }
                }

                if (prefManage.getT8() == 0){
                    if (isNetworkAvailable(binding.root.context)){
                        getTuitionList()
                    }
                }
            } catch (e:Exception){
                Log.d("setTerm", e.message!!)
            }
        } catch (e:Exception){

        }
    }

    private fun getData(){
        val apiClient = ApiClient()
        val apiService:ApiService = apiClient.getClient().create(ApiService::class.java)
        val call: Call<RoomMsg> = apiService.getRooms("rid")

        call.enqueue(object : Callback<RoomMsg>{
            override fun onFailure(call: Call<RoomMsg>, t: Throwable) {
                viewDialog.hideDialog()
                binding.root.tvRNDA.visibility = View.VISIBLE
                binding.root.rvRoom.visibility = View.GONE
            }

            override fun onResponse(call: Call<RoomMsg>, response: Response<RoomMsg>) {
                viewDialog.hideDialog()
                binding.root.tvRNDA.visibility = View.GONE
                binding.root.rvRoom.visibility = View.VISIBLE
                if (response.body() != null){
                    if (response.body()?.rooms!!.isNotEmpty()){
                        roomList = (response.body()!!.rooms as MutableList<Room>)
                        roomAdapter.setRoomListItem(roomList)
                    } else {
                        binding.root.tvRNDA.visibility = View.VISIBLE
                        binding.root.rvRoom.visibility = View.GONE
                    }
                } else {
                    binding.root.tvRNDA.visibility = View.VISIBLE
                    binding.root.rvRoom.visibility = View.GONE
                }
            }

        })
    }

    private fun showAddRoomDialog(context: Context, mode: String, roomI:Int, roomN:String, roomF: Int, roomC: Int){
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_add_room)
        dialog.setCancelable(true)


        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        val etRoomName: EditText = dialog.findViewById(R.id.etRoomName)
        val tilRoomName: TextInputLayout = dialog.findViewById(R.id.tilRoomName)
        val etRoomFloor: EditText = dialog.findViewById(R.id.etRoomFloor)
        val tilRoomFloor: TextInputLayout = dialog.findViewById(R.id.tilRoomFloor)
        val spinnerAddRoom: MaterialSpinner = dialog.findViewById(R.id.spinnerAddRoom)

        val btnAddRoom: Button = dialog.findViewById(R.id.btnAddRoom)

        val roomCapacities: ArrayList<Int> = arrayListOf(8, 10, 12)

        spinnerAddRoom.setItems(roomCapacities)


        if (mode == "edit"){
            etRoomName.setText(roomN)
            etRoomFloor.setText(roomF.toString())
            btnAddRoom.text = "ویرایش اتاق"
        }


        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        dialog.findViewById<ImageButton>(R.id.btnRoomClose).setOnClickListener {
            dialog.dismiss()
        }

        btnAddRoom.setOnClickListener {
            val roomName = etRoomName.text.toString().trim()
            val roomFloor = etRoomFloor.text.toString().trim()
            when {
                roomName.length < 3 -> tilRoomName.error = "عنوان باید حداقل ۳ کاراکتر باشد"
                roomFloor.isEmpty()  -> tilRoomFloor.error = "طبقه نباید خالی باشد"
                else -> {
                    tilRoomName.error = null
                    tilRoomFloor.error = null
                    try {
                        if (mode=="add"){
                            val apiClient = ApiClient()
                            val apiService = apiClient.getClient().create(ApiService::class.java)
                            val call = apiService.manageRooms(mode, 0, roomName, roomFloor.toInt(), roomCapacities[spinnerAddRoom.selectedIndex])

                            call.enqueue(object : Callback<Msg>{
                                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                                    val res = response.body()!!
                                    if (res.success == 1){
                                        dialog.dismiss()
                                        loadData()
                                        roomAdapter.notifyDataSetChanged()
                                    } else {
                                        toast("خطا", context)
                                    }
                                }

                                override fun onFailure(call: Call<Msg>, t: Throwable) {
                                    toast(context.getString(R.string.network_failure), context)
                                }

                            })
                        } else if (mode=="edit"){
                            val apiClient = ApiClient()
                            val apiService = apiClient.getClient().create(ApiService::class.java)
                            val call = apiService.manageRooms(mode, roomI, roomName, roomFloor.toInt(), roomCapacities[spinnerAddRoom.selectedIndex])

                            call.enqueue(object : Callback<Msg>{
                                override fun onResponse(call: Call<Msg>, response: Response<Msg>) {
                                    val res = response.body()!!
                                    if (res.success == 1){
                                        dialog.dismiss()
                                        loadData()
                                        roomAdapter.notifyDataSetChanged()
                                    } else {
                                        toast("خطا", context)
                                    }
                                }

                                override fun onFailure(call: Call<Msg>, t: Throwable) {
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

    private fun getTuitionList() {
        val apiClient = ApiClient()
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.getTuitions("tuId")

        call.enqueue(object : Callback<TuitionMsg>{

            override fun onResponse(call: Call<TuitionMsg>, response: Response<TuitionMsg>) {
                val res = response.body()
                if (res!!.tuitions != null){
                    prefManage.setT8(res.tuitions?.get(0)?.tuPrice!!.toInt())
                    prefManage.setT10(res.tuitions[1].tuPrice!!.toInt())
                    prefManage.setT12(res.tuitions[2].tuPrice!!.toInt())
                }
            }

            override fun onFailure(call: Call<TuitionMsg>, t: Throwable) {

            }
        })
    }

    private fun setTerm() {
        val apiClient = ApiClient()
        val apiService = apiClient.getClient().create(ApiService::class.java)
        val call = apiService.getTerms("termId")
        call.enqueue(object : Callback<TermMsg> {
            override fun onResponse(call: Call<TermMsg>, response: Response<TermMsg>) {
                if (response.body() != null){
                    if (response.body()!!.terms!!.isNotEmpty()){
                        val terms = response.body()!!.terms!!
                        prefManage.setTerm(terms[0].termName!!)
                    }
                }
            }

            override fun onFailure(call: Call<TermMsg>, t: Throwable) {
            }
        })
    }
}