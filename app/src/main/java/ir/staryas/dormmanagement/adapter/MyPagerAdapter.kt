package ir.staryas.dormmanagement.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ir.staryas.dormmanagement.fragment.PaymentFragment
import ir.staryas.dormmanagement.fragment.TermsFragment
import ir.staryas.dormmanagement.fragment.ViolationFragment

class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                PaymentFragment()
            }
            1 -> ViolationFragment()
            else -> {
                return TermsFragment()
            }
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "پرداخت ها"
            1 -> "تخلف ها"
            else -> {
                return "ترم ها"
            }
        }
    }
}