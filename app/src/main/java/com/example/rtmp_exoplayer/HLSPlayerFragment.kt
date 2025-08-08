package com.example.rtmp_exoplayer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.rtmp_exoplayer.databinding.FragmentPlayerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class HLSPlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private var player: ExoPlayer? = null

    lateinit var rtmpInputUrl: String

    private lateinit var hlsDirectory: File
    private val hlsFileName = "stream.m3u8"
    private lateinit var hlsFile: File
    private val localHlsUri by lazy { Uri.fromFile(hlsFile) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rtmpInputUrl = arguments?.getString(ARG_URL) ?:""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Set up the directory and file path for HLS files
        hlsDirectory = File(requireContext().filesDir, "hls")
        hlsFile = File(hlsDirectory, hlsFileName)

        if (!hlsDirectory.exists()) {
            hlsDirectory.mkdirs()
        } else {
            // Clean up old files from previous sessions
            hlsDirectory.listFiles()?.forEach { it.delete() }
        }

        // 2. Start the FFmpeg process to convert RTMP to HLS
        startFfmpegTranscoding()
    }

    private fun startFfmpegTranscoding() {
        val ffmpegCommand =
            "-i $rtmpInputUrl -c:v copy -c:a copy -f hls -hls_time 2 -hls_list_size 4 -hls_flags delete_segments ${hlsFile.absolutePath}"

        Log.d("HLSPlayerFragment", "Executing FFmpeg command: $ffmpegCommand")

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val result = FFmpeg.execute(ffmpegCommand)
            if (result == Config.RETURN_CODE_SUCCESS) {
                Log.i("HLSPlayerFragment", "FFmpeg process completed successfully.")
            } else {
                Log.e("HLSPlayerFragment", "FFmpeg process failed with rc=$result.")
                Config.printLastCommandOutput(Log.INFO)
            }
        }

        // Wait a few seconds for FFmpeg to create the initial .m3u8 file
        // before trying to initialize the player.
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(5000)
            initializePlayer()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun initializePlayer() {
        if (player != null || context == null) return

        Log.d("HLSPlayerFragment", "Initializing player with URI: $localHlsUri")

        val dataSourceFactory = DefaultDataSource.Factory(requireContext())
        val mediaItem = MediaItem.fromUri(localHlsUri)

        val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)

        player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            exoPlayer.setMediaSource(hlsMediaSource)
            exoPlayer.playWhenReady = true
            exoPlayer.prepare()
        }
    }

    private fun releaseResources() {
        player?.release()
        player = null

        FFmpeg.cancel()
        Log.d("HLSPlayerFragment", "All resources released.")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseResources()
        _binding = null
    }

    companion object {
        private const val ARG_URL = "url"

        @JvmStatic
        fun newInstance(url: String) =
            HLSPlayerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                }
            }
    }
}

