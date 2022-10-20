package com.smartmobile.example

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = createBindingInstance(layoutInflater)
        setContentView(binding.root)
//        findViewById<Toolbar>(R.id.toolbar)?.setNavigationOnClickListener {
//            onBackPressed()
//        }
        initView()
    }

    abstract fun initView()

    /** Creates new [VB] instance using reflection. */
    @Suppress("UNCHECKED_CAST")
    protected open fun createBindingInstance(inflater: LayoutInflater): VB {
        val vbType = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
        val vbClass = vbType as Class<VB>
        val method = vbClass.getMethod(
            "inflate",
            LayoutInflater::class.java
        )
        return method.invoke(null, inflater) as VB
    }

}