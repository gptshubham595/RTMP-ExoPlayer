package com.example.rtmp_exoplayer

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.rtmp_exoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            //adding a fragment
            val playerFragment = PlayerFragment.newInstance()
            playerFragment.arguments = getBundleArguments()
            val transactionManager = supportFragmentManager.beginTransaction()
            transactionManager.add(R.id.fragmentContainer, playerFragment)
            transactionManager.addToBackStack("PLAYER_FRAGMENT")
            transactionManager.commit()
        }
    }

    private fun getBundleArguments(): Bundle {
        return Bundle().apply {
            putString("url", binding.editText.text.toString())
        }
    }
}