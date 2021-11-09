package edu.temple.audiobb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity(), BookListFragment.BookListInterface {

    var twoPane = false
    private lateinit var bookViewModel: BookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                // .replace(R.id.container1, BookListFragment.newInstance(generateBookList()))
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