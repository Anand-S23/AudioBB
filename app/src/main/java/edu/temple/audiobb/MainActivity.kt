package edu.temple.audiobb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var bookList: BookList = generateBookList()
    }

    private fun generateBookList() : BookList {
        var bookList: BookList = BookList()

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
}