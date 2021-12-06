package edu.temple.audiobb

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import edu.temple.audlibplayer.PlayerService
import java.io.File

class MainActivity : AppCompatActivity(), BookListFragment.BookSelectedInterface , ControlFragment.MediaControlInterface{

    private lateinit var bookListFragment : BookListFragment
    private lateinit var serviceIntent : Intent
    private lateinit var mediaControlBinder : PlayerService.MediaControlBinder
    private lateinit var bookProgress: PlayerService.BookProgress
    private var connected = false

    private val downloadedBooks : SparseArray<Int> by lazy {
        SparseArray()
    }

    val audiobookHandler = Handler(Looper.getMainLooper()) { msg ->

        // obj (BookProgress object) may be null if playback is paused
        msg.obj?.let { msgObj ->
            bookProgress = msgObj as PlayerService.BookProgress
            // If the service is playing a book but the activity doesn't know about it
            // (this would happen if the activity was closed and then reopened) then
            // fetch the book details so the activity can be properly updated
            if (playingBookViewModel.getPlayingBook().value == null) {
                Volley.newRequestQueue(this)
                    .add(JsonObjectRequest(Request.Method.GET, API.getBookDataUrl(bookProgress.bookId), null, { jsonObject ->
                        playingBookViewModel.setPlayingBook(Book(jsonObject))
                        // If no book is selected (if activity was closed and restarted)
                        // then use the currently playing book as the selected book.
                        // This allows the UI to display the book details
                        if (selectedBookViewModel.getSelectedBook().value == null) {
                            // set book
                            selectedBookViewModel.setSelectedBook(playingBookViewModel.getPlayingBook().value)
                            // display book - this function was previously implemented as a callback for
                            // the BookListFragment, but it turns out we can use it here - Don't Repeat Yourself
                            bookSelected()
                        }
                    }, {}))
            }

            // Everything that follows is to prevent possible NullPointerExceptions that can occur
            // when the activity first loads (after config change or opening after closing)
            // since the service can (and will) send updates via the handler before the activity fully
            // loads, the currently playing book is downloaded, and all variables have been initialized
            supportFragmentManager.findFragmentById(R.id.controlFragmentContainerView)?.run{
                with (this as ControlFragment) {
                    playingBookViewModel.getPlayingBook().value?.also {
                        setPlayProgress(((bookProgress.progress / it.duration.toFloat()) * 100).toInt())
                    }
                }
            }
        }

        true
    }

    private val searchRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        supportFragmentManager.popBackStack()
        it.data?.run {
            bookListViewModel.copyBooks(getSerializableExtra(BookList.BOOKLIST_KEY) as BookList)
            bookListFragment.bookListUpdated()
        }

    }

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mediaControlBinder = service as PlayerService.MediaControlBinder
            mediaControlBinder.setProgressHandler(audiobookHandler)
            connected = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connected = false
        }

    }

    private val isSingleContainer : Boolean by lazy{
        findViewById<View>(R.id.container2) == null
    }

    private val selectedBookViewModel : SelectedBookViewModel by lazy {
        ViewModelProvider(this).get(SelectedBookViewModel::class.java)
    }

    private val playingBookViewModel : PlayingBookViewModel by lazy {
        ViewModelProvider(this).get(PlayingBookViewModel::class.java)
    }

    private val bookListViewModel : BookList by lazy {
        ViewModelProvider(this).get(BookList::class.java)
    }

    companion object {
        const val BOOKLISTFRAGMENT_KEY = "BookListFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playingBookViewModel.getPlayingBook().observe(this, {
            (supportFragmentManager.findFragmentById(R.id.controlFragmentContainerView) as ControlFragment).setNowPlaying(it.title)
        })

        // Create intent for binding and starting service
        serviceIntent = Intent(this, PlayerService::class.java)

        // bind to service
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)

        // If we're switching from one container to two containers
        // clear BookDetailsFragment from container1
        if (supportFragmentManager.findFragmentById(R.id.container1) is BookDetailsFragment
            && selectedBookViewModel.getSelectedBook().value != null) {
            supportFragmentManager.popBackStack()
        }

        val config = PRDownloaderConfig.newBuilder()
            .setReadTimeout(30000)
            .setConnectTimeout(30000)
            .build()
        PRDownloader.initialize(applicationContext, config)

        // If this is the first time the activity is loading, go ahead and add a BookListFragment
        if (savedInstanceState == null) {
            bookListFragment = BookListFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.container1, bookListFragment, BOOKLISTFRAGMENT_KEY)
                .commit()
        } else {
            bookListFragment = supportFragmentManager.findFragmentByTag(BOOKLISTFRAGMENT_KEY) as BookListFragment
            // If activity loaded previously, there's already a BookListFragment
            // If we have a single container and a selected book, place it on top
            if (isSingleContainer && selectedBookViewModel.getSelectedBook().value != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container1, BookDetailsFragment())
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit()
            }
        }

        // If we have two containers but no BookDetailsFragment, add one to container2
        if (!isSingleContainer && supportFragmentManager.findFragmentById(R.id.container2) !is BookDetailsFragment)
            supportFragmentManager.beginTransaction()
                .add(R.id.container2, BookDetailsFragment())
                .commit()

        findViewById<ImageButton>(R.id.searchButton).setOnClickListener {
            searchRequest.launch(Intent(this, BookSearchActivity::class.java))
        }

    }

    private fun downloadBook(url: String, fileName: String) {
        PRDownloader.download(url, filesDir.absolutePath, fileName)
            .build()
            .setOnProgressListener {}
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    Toast.makeText(this@MainActivity, "Book Finished Downloading", Toast.LENGTH_SHORT)
                        .show()
                }
                override fun onError(error: com.downloader.Error?) {
                    Toast.makeText(this@MainActivity, "Error occurred while downloading file", Toast.LENGTH_LONG)
                        .show()
                }
            })
    }


    override fun onBackPressed() {
        // BackPress clears the selected book
        selectedBookViewModel.setSelectedBook(null)
        super.onBackPressed()
    }

    override fun bookSelected() {
        // Perform a fragment replacement if we only have a single container
        // when a book is selected

        if (isSingleContainer) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container1, BookDetailsFragment())
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun play() {
        // Save previous book if any
        val prevBook = playingBookViewModel.getPlayingBook().value
        if (prevBook != null && prevBook.id != -1) {
            downloadedBooks.put(prevBook.id, bookProgress.progress)
        }

        val book = selectedBookViewModel.getSelectedBook().value
        if (connected && book != null) {
            Log.d("Button pressed", "Play button")
            if (downloadedBooks.contains(book.id)) {
                val audioFile = File(filesDir, "${book.id}.mp3")
                mediaControlBinder.play(audioFile, downloadedBooks.get(book.id))
            } else {
                mediaControlBinder.play(book.id)
                val url = "https://kamorris.com/lab/audlib/download.php?id=${book.id}"
                downloadBook(url, "${book.id}.mp3")
                downloadedBooks.put(book.id, 0)
            }

            playingBookViewModel.setPlayingBook(book)
            startService(serviceIntent)
        }
    }

    override fun pause() {
        // Save previous book if any
        val prevBook = playingBookViewModel.getPlayingBook().value
        if (prevBook != null) {
            downloadedBooks.put(prevBook.id, bookProgress.progress)
        }

        val book = selectedBookViewModel.getSelectedBook().value
        if (connected && book != null) {
            mediaControlBinder.pause()
        }
    }

    override fun stop() {
        val book = playingBookViewModel.getPlayingBook().value!!
        playingBookViewModel.setPlayingBook(Book(-1, "", "", 0, ""))
        downloadedBooks.put(book.id, 0)

        if (connected) {
            mediaControlBinder.stop()
            stopService(serviceIntent)
        }
    }

    override fun seek(position: Int) {
        // Converting percentage to proper book progress
        if (connected && mediaControlBinder.isPlaying) {
            val book = playingBookViewModel.getPlayingBook().value!!
            val progress = (book.duration * (position.toFloat() / 100)).toInt()
            if (downloadedBooks.contains(book.id)) {
                mediaControlBinder.play(File(filesDir, "${book.id}.mp3"), progress)
            } else {
                mediaControlBinder.seekTo(progress)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}