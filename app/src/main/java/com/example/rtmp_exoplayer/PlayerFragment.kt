package com.example.rtmp_exoplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.datasource.rtmp.RtmpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.rtmp_exoplayer.databinding.FragmentPlayerBinding


class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!! // This property is only valid between onCreateView and onDestroyView.

    private var player: ExoPlayer? = null
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the URL from fragment arguments.
        url = arguments?.getString(ARG_URL) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Player initialization is done here because the view is guaranteed to be created.
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun initializePlayer() {
        // Create an instance of the ExoPlayer.
        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player

        // Create a MediaItem for the RTMP stream.
        val mediaItem = MediaItem.fromUri(url)

        // Create a data source factory for RTMP.
        val rtmpDataSourceFactory = RtmpDataSource.Factory()

        // Create a media source using the data source factory.
        val mediaSource = ProgressiveMediaSource.Factory(rtmpDataSourceFactory)
            .createMediaSource(mediaItem)

        // Set the media source to be played.
        player?.setMediaSource(mediaSource)

        // Prepare the player.
        player?.prepare()

        // Start playback when ready.
        player?.playWhenReady = true
    }

    // Use onStart/onResume and onPause/onStop to initialize and release the player
    // This is aligned with the fragment's view lifecycle.
    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Release the binding when the view is destroyed to avoid memory leaks.
        _binding = null
    }

    companion object {
        private const val ARG_URL = "url"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param url The RTMP stream URL.
         * @return A new instance of fragment PlayerFragment.
         */
        @JvmStatic
        fun newInstance(url: String) =
            PlayerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                }
            }
    }
}