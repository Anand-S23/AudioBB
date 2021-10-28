package edu.temple.audiobb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity(), BookListFragment.BookListInterface {

    var twoPane = false
    lateinit var bookViewModel: BookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bookListFragment = BookListFragment.newInstance(generateBookList())

        twoPane = findViewById<View>(R.id.container2) != null
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)

       if (savedInstanceState == null) {
           if (twoPane) {
               supportFragmentManager.beginTransaction()
                   .replace(R.id.container1, bookListFragment)
                   .commit()
           } else {
               supportFragmentManager.beginTransaction()
                   .replace(R.id.container1, bookListFragment)
                   .addToBackStack(null)
                   .commit()
           }
       }

        if (twoPane) {
            if (supportFragmentManager.findFragmentById(R.id.container1) is BookDetailsFragment) {
                supportFragmentManager.popBackStack()
            }

            if (supportFragmentManager.findFragmentById(R.id.container2) == null) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.container2, BookDetailsFragment())
                    .commit()
            }
        } else if (!bookNullOrEmpty(bookViewModel.getBook().value)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container2, BookDetailsFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun selectionMade() {
        if (!twoPane) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container1, BookDetailsFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun generateBookList() : BookList {
        val bookList = BookList()

        bookList.add(Book("The Lord of the Rings", "J. R. R. Tolkien"))
        bookList.add(Book("Assassin's Apprentice", "Robin Hobb"))
        bookList.add(Book("Dune", "Frank Herbert"))
        bookList.add(Book("1984", "George Orwell"))
        bookList.add(Book("The Way of the Kings", "Brandon Sanderson"))
        bookList.add(Book("The Wheel of Time", "Robert Jordan"))
        bookList.add(Book("Ender's Game", "Orson Scott Card"))
        bookList.add(Book("Neuromancer", "William Gibson"))
        bookList.add(Book("Red Rising", "Pierce Brown"))
        bookList.add(Book("American Gods", "Neil Gaiman"))
        bookList.add(Book("Harry Potter", "J.K. Rowling"))

        return bookList
    }

    private fun bookNullOrEmpty(_book: Book?) : Boolean {
        if (_book == null || _book == Book("", "")) {
            return true
        }
        return false
    }
}