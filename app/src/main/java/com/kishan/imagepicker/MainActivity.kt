package com.kishan.imagepicker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView


class MainActivity : AppCompatActivity(), ImagePicker.MyInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pop = ImagePicker(this)
        pop.show(supportFragmentManager, "ImagePicker")
    }

    override fun getPath(path: String) {
        Log.d("TAG", "getPath: ---> $path")
    }
}