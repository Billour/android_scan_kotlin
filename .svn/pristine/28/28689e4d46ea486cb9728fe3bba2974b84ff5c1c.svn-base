package com.repongroup.repon_00

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.repongroup.repon_android.Repon_list
import kotlinx.android.synthetic.main.repon_00_main.*

class Repon_00_main : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.repon_00_main)
        setupViewComponent() //自定義
    }

    //自定義
    private fun setupViewComponent() {
        main_text.setOnClickListener {
            val it: Intent = Intent(this, Repon_list::class.java)
            startActivity(it)
        }
    }
}
