import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.rtmp_exoplayer.HLSPlayerFragment
import com.example.rtmp_exoplayer.databinding.FragmentFfplayerBinding

class FFPlayerFragment : Fragment() {

    private var _binding: FragmentFfplayerBinding? = null
    private val binding get() = _binding!!

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    lateinit var rtmpInputUrl: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFfplayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rtmpInputUrl = arguments?.getString(ARG_URL) ?: ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loadButton.setOnClickListener {
            if (rtmpInputUrl.isNotEmpty()) {
                releasePlayer()
                initializePlayer(rtmpInputUrl)
            } else {
                Toast.makeText(context, "Please enter a URL", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializePlayer(rtmpUrl: String) {
        player = ExoPlayer.Builder(requireContext())
            .build()
            .also { exoPlayer ->
                binding.playerView.player = exoPlayer

                val mediaItem = MediaItem.Builder()
                    .setUri(rtmpUrl)
                    .setLiveConfiguration(
                        MediaItem.LiveConfiguration.Builder()
                            .setMaxPlaybackSpeed(1.02f)
                            .setTargetOffsetMs(500)
                            .build()
                    )
                    .build()

                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentItem, playbackPosition)
                exoPlayer.prepare()
            }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

    override fun onStart() {
        super.onStart()
        val url = binding.rtmpUrlEditText.text.toString().trim()
        if (url.isNotEmpty()) {
            initializePlayer(url)
        }
    }

    override fun onResume() {
        super.onResume()
        if (player == null) {
            val url = binding.rtmpUrlEditText.text.toString().trim()
            if (url.isNotEmpty()) {
                initializePlayer(url)
            }
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