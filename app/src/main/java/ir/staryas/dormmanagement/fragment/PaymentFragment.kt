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
import ir.staryas.dormmanagement.adapter.PaymentsAdapter
import ir.staryas.dormmanagement.model.Payment
import ir.staryas.dormmanagement.model.PaymentMsg
import ir.staryas.dormmanagement.networking.ApiClient
import ir.staryas.dormmanagement.networking.ApiService
import ir.staryas.dormmanagement.util.Communicator
import ir.staryas.dormmanagement.util.toast
import kotlinx.android.synthetic.main.fragment_payment.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.Exception

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PaymentFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnPaymentFragmentInteractionListener? = null
    private var model: Communicator?=null
    private var studentId:String = ""
    private lateinit var rvPayment: RecyclerView
    private lateinit var paymentsAdapter: PaymentsAdapter
    private lateinit var paymentsList: MutableList<Payment>

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
        val root = inflater.inflate(R.layout.fragment_payment, container, false)
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
        paymentsList = mutableListOf()
        val layoutManager = LinearLayoutManager(view.context)
        rvPayment = view.findViewById(R.id.rvStdPayments)
        paymentsAdapter = PaymentsAdapter(view.context)
        view.rvStdPayments.layoutManager = layoutManager
        view.rvStdPayments.adapter = paymentsAdapter

        try {

            val apiClient = ApiClient()
            val apiService = apiClient.getClient().create(ApiService::class.java)
            //toast(studentId, view.context)
            val call = apiService.getPayments("pId", studentId)
            lateinit var paymentMsg: PaymentMsg
            call.enqueue(object : Callback<PaymentMsg>{
                override fun onResponse(call: Call<PaymentMsg>, response: Response<PaymentMsg>) {
                    val res = response.body()!!
                    if (res.success == 1){
                        if (res.payments!!.isNotEmpty()){
                            paymentMsg = res
                            view.tvPaymentNoData.visibility = View.GONE
                            view.rvStdPayments.visibility = View.VISIBLE
                            paymentsList = paymentMsg.payments as MutableList<Payment>
                            paymentsAdapter.setPaymentListItem(paymentsList)
                        } else {
                            view.rvStdPayments.visibility = View.GONE
                            view.tvPaymentNoData.visibility = View.VISIBLE
                        }
                    } else {
                        view.rvStdPayments.visibility = View.GONE
                        view.tvPaymentNoData.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<PaymentMsg>, t: Throwable) {
                    view.rvStdPayments.visibility = View.GONE
                    toast(getString(R.string.network_failure), view.context)
                    view.tvPaymentNoData.visibility = View.VISIBLE
                }

            })
        } catch (e:Exception){
            Log.d("getPayments", e.message!!)
        }

        paymentsAdapter.setOnItemClickListener(object : PaymentsAdapter.ClickListener{
            override fun onClick(pos: Int, aView: View) {

            }
        })
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onPaymentFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnPaymentFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface OnPaymentFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onPaymentFragmentInteraction(uri: Uri)
    }




    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PaymentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
