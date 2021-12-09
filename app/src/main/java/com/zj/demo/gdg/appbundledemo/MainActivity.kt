package com.zj.demo.gdg.appbundledemo

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.play.core.splitcompat.SplitCompat
import com.zj.demo.gdg.nativelib.NativeLib
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

const val MODULE_ON_DEMAND = "dynamic_demand"
const val TAG = "app_bundle_demo"

class MainActivity : AppCompatActivity() {

    // View
    private var jniTV: AppCompatTextView? = null
    private var installTV: AppCompatTextView? = null
    private var demandTV: AppCompatTextView? = null
    private var conditionApi1929TV: AppCompatTextView? = null
    private var conditionApi30TV: AppCompatTextView? = null
    private var conditionApi31TV: AppCompatTextView? = null
    private var conditionCameraTV: AppCompatTextView? = null
    private var conditionNFCTV: AppCompatTextView? = null
    private var installedTV: AppCompatTextView? = null

    //Data
    private var splitInstallManager: SplitInstallManager? = null
    private var sessionId = 0


    private val splitInstallListener = SplitInstallStateUpdatedListener { state ->
        if (state.sessionId() == sessionId) {
            when (state.sessionId()) {
                SplitInstallSessionStatus.INSTALLED -> {
                    updateDemandData()
                }
                SplitInstallSessionStatus.FAILED -> {
                    Toast.makeText(this, "Module install failed with ${state.errorCode()}", Toast.LENGTH_SHORT).show()
                    updateDemandData(state.errorCode().toString())
                }
                else -> {
                    updateDemandData("Status: ${state.status()}")
                }
            }
        }
    }

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)
        SplitCompat.installActivity(this)
    }


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        splitInstallManager = SplitInstallManagerFactory.create(this)

        jniTV = findViewById(R.id.tv_jni)
        installTV = findViewById(R.id.tv_model_install)
        demandTV = findViewById(R.id.tv_model_demand)
        conditionApi1929TV = findViewById(R.id.tv_model_condition_api19_29)
        conditionApi30TV = findViewById(R.id.tv_model_condition_api30)
        conditionApi31TV = findViewById(R.id.tv_model_condition_api31)
        conditionCameraTV = findViewById(R.id.tv_model_condition_camera)
        conditionNFCTV = findViewById(R.id.tv_model_condition_nfc)
        installedTV = findViewById(R.id.tv_models)

        jniTV?.text = "JNI String :" + NativeLib().stringFromJNI()
        conditionApi1929TV?.text = getModelInfo("com.zj.demo.gdg.dynamic_condition_api19_29.StringUtil")
        conditionApi30TV?.text = getModelInfo("com.zj.demo.gdg.dynamic_condition_api30.StringUtil")
        conditionApi31TV?.text = getModelInfo("com.zj.demo.gdg.dynamic_condition_api31.StringUtil")
        conditionCameraTV?.text = getModelInfo("com.zj.demo.gdg.dynamic_condition_camera.StringUtil")
        conditionNFCTV?.text = getModelInfo("com.zj.demo.gdg.dynamic_condition_nfc.StringUtil")
        installTV?.text = getModelInfo("com.zj.demo.gdg.dynamic_install.StringUtil")
        demandTV?.text = getModelInfo("com.zj.demo.gdg.dynamic_demand.StringUtil")
        installedTV?.text = splitInstallManager?.installedModules.toString()

    }

    override fun onResume() {
        super.onResume()
        splitInstallManager?.registerListener(splitInstallListener)
    }

    override fun onPause() {
        splitInstallManager?.unregisterListener(splitInstallListener)
        super.onPause()
    }


    private fun getModelInfo(className: String) = try {
        val clazz = classLoader.loadClass(className)
        val method = clazz.getMethod("getModelInfo")
        method.invoke(clazz.newInstance()) as String
    } catch (e: Throwable) {
        e.toString()
    }

    fun loadOnDemandModel(view: View) {
        // check state
        if (splitInstallManager?.installedModules?.contains(MODULE_ON_DEMAND) == true) {
            updateDemandData()
            return
        }
        val request = SplitInstallRequest.newBuilder().addModule(MODULE_ON_DEMAND).build()
        splitInstallManager?.startInstall(request)
            ?.addOnSuccessListener { id ->
                sessionId = id
            }
            ?.addOnFailureListener {
                Log.e(TAG, "Error installing module: ", it)
                Toast.makeText(this, "Error requesting module install", Toast.LENGTH_SHORT).show()
                updateDemandData(it.toString())
            }
    }

    private fun updateDemandData(message: String? = null) {
        demandTV?.text = message ?: getModelInfo("com.zj.demo.gdg.dynamic_demand.StringUtil")
        installedTV?.text = splitInstallManager?.installedModules.toString()
    }

}