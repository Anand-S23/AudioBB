package edu.temple.audiobb

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import edu.temple.audlibplayer.PlayerService
import java.io.File
import java.lang.Exception

class MainActivity : AppCompatActivity(), BookListFragment.BookListInterface, ControlFragment.ControlInterface {

    private var twoPane = false
    private lateinit var bookViewModel: BookViewModel
    private var bookList: BookList = BookList()

    private var isConnected = false
    private lateinit var mediaControlBinder: PlayerService.MediaControlBinder
    private lateinit var progressViewModel: BookProgressViewModel

    private val mediaControlHandler = Handler(Looper.getMainLooper()) {
        if (it.obj != null) {
            val progress = it.obj as PlayerService.BookProgress
            progressViewModel.setBookProgress(progress)
        }
        true
    }

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isConnected = true
            mediaControlBinder = service as PlayerService.MediaControlBinder
            mediaControlBinder.setProgressHandler(mediaControlHandler)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isConnected = false
        }
    }

    private var playing: Boolean = true

    private val searchActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val changed: Boolean? = it.data?.getBooleanExtra(CHANGE, false)
        if (changed == true) {
            // Update bookList only if it is changed
            bookList = (it.data?.getSerializableExtra(RET_LIST) as BookList)

            Log.d("BookList", "Updated")

            // Update Fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.container1, BookListFragment.newInstance(bookList))
                .commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val searchActivityIntent = Intent(this, BookSearchActivity::class.java)
        findViewById<Button>(R.id.searchLaunchButton).setOnClickListener {
            searchActivityLauncher.launch(searchActivityIntent)
        }

        twoPane = findViewById<View>(R.id.container2) != null
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)
        progressViewModel = ViewModelProvider(this).get(BookProgressViewModel::class.java)

        if (supportFragmentManager.findFragmentById(R.id.container1) is BookDetailsFragment &&
            bookNullOrBlank(bookViewModel.getBook().value)) {
            supportFragmentManager.popBackStack()
        }

        if (supportFragmentManager.findFragmentById(R.id.container1) is BookDetailsFragment && twoPane) {
            supportFragmentManager.popBackStack()
        }

        if (savedInstanceState == null) {
            bindService(
                Intent(this, PlayerService::class.java),
                serviceConnection,
                BIND_AUTO_CREATE
            )

            supportFragmentManager.beginTransaction()
                .replace(R.id.container1, BookListFragment.newInstance(bookList))
                .commit()

            supportFragmentManager.beginTransaction()
                .replace(R.id.container3, ControlFragment.newInstance())
                .commit()
        } else {
            mediaControlBinder = savedInstanceState.getBinder("binder") as PlayerService.MediaControlBinder
        }

        if (twoPane) {
            if (supportFragmentManager.findFragmentById(R.id.container2) == null) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container2, BookDetailsFragment.newInstance())
                    .commit()
            }
        } else if (!bookNullOrBlank(bookViewModel.getBook().value)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container1, BookDetailsFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBinder("binder", mediaControlBinder)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }


    override fun selectionMade() {
        if (!twoPane) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container1, BookDetailsFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun bookNullOrBlank(_book: Book?) : Boolean {
        return (_book?.id == 0 || _book?.title.isNullOrBlank() ||
                _book?.author.isNullOrBlank() || _book?.coverURL.isNullOrBlank())
    }

    override fun play(id: Int) {
        mediaControlBinder.play(id)
        playing = true
    }

    override fun play(file: File, startPosition: Int) {
        mediaControlBinder.play(file, startPosition)
    }

    override fun pause() {
        mediaControlBinder.pause()
        playing = !playing
    }

    override fun stop() {
        mediaControlBinder.stop()
        playing = false
    }

    override fun seekTo(position: Int) {
        mediaControlBinder.seekTo(position)
    }
}