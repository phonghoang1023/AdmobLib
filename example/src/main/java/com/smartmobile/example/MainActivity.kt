package com.smartmobile.example

import android.content.Intent
import com.smartmobile.admob.Admob
import com.smartmobile.example.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun initView() {
        loadAdmob()
        binding.btnInter.setOnClickListener {
            Admob.showAdmobInter(this) {
                startActivity(Intent(this, NewActivity::class.java))
            }
        }
    }

    private fun loadAdmob() {
        Admob.loadInter(this, "")
        Admob.showAdBanner(this, "", binding.frBannerTop, true)
        Admob.showAdBanner(this, "", binding.frBanner)
        Admob.showAdNative(this, "", binding.frNative)
    }

}