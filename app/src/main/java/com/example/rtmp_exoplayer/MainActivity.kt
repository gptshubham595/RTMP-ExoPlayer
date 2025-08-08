package com.example.rtmp_exoplayer

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.rtmp_exoplayer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playButton.setOnClickListener {
            binding.fragmentContainer.visibility = View.VISIBLE
            val playerFragment = PlayerFragment.newInstance(binding.urlEditText.text.toString())
            val transactionManager = supportFragmentManager.beginTransaction()
            transactionManager.add(R.id.fragmentContainer, playerFragment)
            transactionManager.addToBackStack("PLAYER_FRAGMENT")
            transactionManager.commit()
        }

        binding.playButtonHLS.setOnClickListener {
            binding.fragmentContainer.visibility = View.VISIBLE
            val playerFragment = HLSPlayerFragment.newInstance(binding.urlEditTextHLS.text.toString())
            val transactionManager = supportFragmentManager.beginTransaction()
            transactionManager.add(R.id.fragmentContainer, playerFragment)
            transactionManager.addToBackStack("PLAYER_FRAGMENT")
            transactionManager.commit()
        }

        binding.streamButton.setOnClickListener {
            binding.fragmentContainer.visibility = View.VISIBLE
            val video = filesDir.absolutePath + "/test.mp4";
            Log.d("FFMPEG", "Video path: $video")
            try {
                FFmpeg.execute("-re -i $video -c copy -f flv ${binding.whereToStreamUrlEditText.text}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.w("FFMPEG", e.toString())
            }
        }
    }

}