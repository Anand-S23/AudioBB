package edu.temple.audiobb

class BookList {
    private val bookList: MutableList<Book> = ArrayList()

    fun add(_book: Book) {
        bookList.add(_book)
    }

    fun remove(_book: Book) {
        bookList.remove(_book)
    }

    fun get(_pos: Int) : Book {
        return bookList[_pos]
    }

    fun size() : Int {
        return bookList.size
    }
}