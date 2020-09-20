package ir.staryas.dormmanagement.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import ir.staryas.dormmanagement.R
import ir.staryas.dormmanagement.adapter.AdapterStdTerm
import ir.staryas.dormmanagement.model.*
import ir.staryas.dormmanagement.networking.ApiClient
import ir.staryas.dormmanagement.networking.ApiService
import ir.staryas.dormmanagement.util.Communicator
import ir.staryas.dormmanagement.util.PrefManage
import ir.staryas.dormmanagement.util.toast
import kotlinx.android.synthetic.main.fragment_terms.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class TermsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var model: Communicator?=null
    private var listener: OnTermsFragmentInteractionListener? = null
    private var studentId = ""
    private lateinit var prefManage: PrefManage

    private lateinit var rvTerm: RecyclerView
    private lateinit var stdTermsAdapter: AdapterStdTerm
    private lateinit var stdTermsList: MutableList<RoomStd>

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
        val root = inflater.inflate(R.layout.fragment_terms, container, false)
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
        stdTermsList = mutableListOf()
        prefManage = PrefManage(view.context)
        val layoutManager = LinearLayoutManager(view.context)
        rvTerm = view.findViewById(R.id.rvStdTerms)
        stdTermsAdapter = AdapterStdTerm(view.context)
        rvTerm.layoutManager = layoutManager
        rvTerm.adapter = stdTermsAdapter

        try {

            val apiClient = ApiClient()
            val apiService = apiClient.getClient().create(ApiService::class.java)
            val call = apiService.getRoomStdByTerm("student", "ts_id", prefManage.getTerm()!!, 4, studentId)


            call.enqueue(object : Callback<RoomStdMsg>{
                override fun onResponse(call: Call<RoomStdMsg>, response: Response<RoomStdMsg>) {
                    val res = response.body()!!
                    if (res.success == 1){
                        if (res.termStudents!!.isNotEmpty()){
                            view.tvTermNoData.visibility = View.GONE
                            rvTerm.visibility = View.VISIBLE
                            stdTermsList = res.termStudents as MutableList<RoomStd>
                            stdTermsAdapter.setRoomStdListItem(stdTermsList)
                        } else {
                            rvTerm.visibility = View.GONE
                            view.tvTermNoData.visibility = View.VISIBLE
                        }
                    } else {
                        rvTerm.visibility = View.GONE
                        view.tvTermNoData.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<RoomStdMsg>, t: Throwable) {
                    rvTerm.visibility = View.GONE
                    toast(getString(R.string.network_failure), view.context)
                    view.tvTermNoData.visibility = View.VISIBLE
                }

            })






        } catch (e:Exception){
            Log.d("getPayments", e.message!!)
        }

        stdTermsAdapter.setOnItemClickListener(object : AdapterStdTerm.ClickListener{
            override fun onClick(pos: Int, aView: View) {

            }
        })
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onTermsFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTermsFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface OnTermsFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onTermsFragmentInteraction(uri: Uri)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TermsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}
