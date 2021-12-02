package edu.temple.audiobb

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import edu.temple.audlibplayer.PlayerService
import java.io.File

private const val CURRENT_BOOK_ID = "currentBookID"
private const val CURRENT_BOOK_PROGRESS = "currentBookProgress"

class ControlFragment : Fragment() {

    private lateinit var layout: View
    private var blank = Book(-1, "", "", "", 0)
    private var currentBook = blank

    private lateinit var bookSeekBar: SeekBar
    private lateinit var nowPlaying: TextView
    private lateinit var mediaButton: Button
    private lateinit var stopButton: Button

    private var beforePauseProgress: Int = 0
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_control, container, false)

        bookSeekBar = layout.findViewById(R.id.bookSeekBar)
        nowPlaying = layout.findViewById(R.id.nowPlaying)
        mediaButton = layout.findViewById(R.id.mediaControl)
        stopButton = layout.findViewById(R.id.stop)

        mediaButton.setOnClickListener {

        }

        bookSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    (activity as ControlInterface).seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        return layout
    }

    fun setNowPlaying(title: String) {
        nowPlaying?.text = title
    }

    fun setPlayProgress(progress: Int) {
        bookSeekBar?.setProgress(progress, true)
    }

    companion object {
        @JvmStatic
        fun newInstance(): ControlFragment {
            return ControlFragment()
        }
    }

    interface ControlInterface {
        fun play()
        fun pause()
        fun stop()
        fun seekTo(position: Int)
    }
}