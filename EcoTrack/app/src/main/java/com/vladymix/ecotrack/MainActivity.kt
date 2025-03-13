package com.vladymix.ecotrack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import com.vladymix.ecotrack.factory.ViewModelFactory
import com.vladymix.ecotrack.ui.TrackActivity

class MainActivity : BaseActivity() {
    
    lateinit var texlog :TextView
    lateinit var scrollded :NestedScrollView
    private val viewMode = ViewModelFactory.airQualityViewModel


    fun oldCode(){
        findViewById<EditText>(R.id.editTextDevice).text.let {
            if (it.toString().isEmpty()) {
                Toast.makeText(this, "Device can´t empty", Toast.LENGTH_LONG).show()
            } else {
                api.setDevice(findViewById<EditText>(R.id.editTextDevice).text.toString())
                startActivity(Intent(this, TrackActivity::class.java))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        texlog = findViewById(R.id.tvLogs)
        scrollded = findViewById(R.id.scrollded)

        viewMode.log.observe(this){
            texlog.text = it
            scrollded.post {
                scrollded.fullScroll(View.FOCUS_DOWN)
            }
        }

    }

    fun setLog(tag:String, msn:String){
        Log.i(tag, msn)
        viewMode.setLog("$tag:$msn")

    }

}