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
import kotlin.properties.Delegates

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

        ViewModelProvider(requireActivity())
            .get(BookViewModel::class.java)
            .getBook().observe(viewLifecycleOwner, { book: Book ->
                currentBook = book

                if (currentBook.id != -1) {
                    nowPlaying.text = currentBook.title
                    mediaButton.text = getString(R.string.pause)
                    isPlaying = true
                    (requireActivity() as ControlInterface).play(currentBook.id)
                }
            })

        ViewModelProvider(requireActivity())
            .get(BookProgressViewModel::class.java)
            .getBookProgress().observe(viewLifecycleOwner, { bookProgress: PlayerService.BookProgress ->
                if (currentBook.id != -1) {
                    beforePauseProgress = bookProgress.progress
                    val done: Boolean = (bookProgress.progress.toFloat() / currentBook.duration.toFloat()) >= .999
                    if (done) {
                        mediaButton.text = getString(R.string.play)
                        isPlaying = false
                    } else {
                        bookSeekBar.progress =
                            ((bookProgress.progress.toFloat() / currentBook.duration.toFloat()) * 100).toInt()
                    }
                }
            })

        bookSeekBar = layout.findViewById(R.id.bookSeekBar)
        nowPlaying = layout.findViewById(R.id.nowPlaying)
        mediaButton = layout.findViewById(R.id.mediaControl)
        stopButton = layout.findViewById(R.id.stop)

        mediaButton.setOnClickListener {
            if (currentBook.id != -1) {
                if (isPlaying) {
                    isPlaying = false
                    mediaButton.text = getString(R.string.play)
                    (requireActivity() as ControlInterface).pause()
                } else {
                    isPlaying = true
                    mediaButton.text = getString(R.string.pause)
                    (requireActivity() as ControlInterface).play(currentBook.id)
                    (requireActivity() as ControlInterface).seekTo(beforePauseProgress)
                }
            }
        }

        stopButton.setOnClickListener {
            currentBook = blank
            nowPlaying.text = ""
            mediaButton.text = getString(R.string.play)
            isPlaying = false
            (requireActivity() as ControlInterface).stop()
        }

        bookSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (currentBook.id != -1) {
                    val position: Int =
                        ((bookSeekBar.progress / 100.0f) * currentBook.duration).toInt()
                    (requireActivity() as ControlInterface).seekTo(position)
                } else {
                    bookSeekBar.progress = 0
                }
            }
        })

        return layout
    }

    companion object {
        @JvmStatic
        fun newInstance(): ControlFragment {
            return ControlFragment()
        }
    }

    interface ControlInterface {
        fun play(id: Int)
        fun play(file: File, startPosition: Int)
        fun pause()
        fun stop()
        fun seekTo(position: Int)
    }
}