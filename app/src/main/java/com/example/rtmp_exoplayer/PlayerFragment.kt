package com.example.rtmp_exoplayer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.rtmp_exoplayer.databinding.FragmentPlayerBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView.SHOW_BUFFERING_ALWAYS


class PlayerFragment : Fragment() {

    private lateinit var binding: FragmentPlayerBinding
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var url: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        url = arguments?.getString("url") ?: ""
        binding = FragmentPlayerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer.stop()
        exoPlayer.release()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializePlayer()
        playVideo()
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    /**
     * setup ExoPlayer
     */
    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(requireContext())
            .build()
        binding.playerView.player = exoPlayer
        binding.playerView.setShowBuffering(SHOW_BUFFERING_ALWAYS)
    }

    /**
     * Play RTMP
     */
    private fun playVideo() {
        val videoSource = ProgressiveMediaSource.Factory(RtmpDataSource.Factory())
            .createMediaSource(MediaItem.fromUri(Uri.parse(url)))
        exoPlayer.setMediaSource(videoSource)
    }

    companion object {
        fun newInstance() = PlayerFragment()
    }
}