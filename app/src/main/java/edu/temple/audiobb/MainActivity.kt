package edu.temple.audiobb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity(), BookListFragment.BookListInterface {

    var twoPane = false
    private lateinit var bookViewModel: BookViewModel
    private var bookList: BookList = BookList()

    private val searchActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val changed: Boolean? = it.data?.getBooleanExtra(CHANGE, false)
        if (changed == true) {
            // Update bookList only if it is changed
            bookList = (it.data?.getSerializableExtra(RET_LIST) as BookList)

            Log.d("TheTest", bookList.size().toString())
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

        if (supportFragmentManager.findFragmentById(R.id.container1) is BookDetailsFragment &&
            bookNullOrBlank(bookViewModel.getBook().value)) {
            supportFragmentManager.popBackStack()
        }

        if (supportFragmentManager.findFragmentById(R.id.container1) is BookDetailsFragment && twoPane) {
            supportFragmentManager.popBackStack()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container1, BookListFragment.newInstance(bookList))
                .commit()
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

    override fun selectionMade() {
        if (!twoPane) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container1, BookDetailsFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun bookNullOrBlank(_book: Book?) : Boolean {
        return (_book?.title.isNullOrBlank() || _book?.author.isNullOrBlank())
    }
}