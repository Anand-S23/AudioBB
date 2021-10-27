package edu.temple.audiobb

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookAdapter(private val bookList: BookList, private val onClick: (Book) -> Unit) :
    RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

        class BookViewHolder(view: View, onClick: (Book) -> Unit) : RecyclerView.ViewHolder(view) {
            private val title : TextView = view.findViewById(R.id.titleTextView)
            private val author : TextView = view.findViewById(R.id.authorTextView)

            var currentBook : Book? = null

            init {
                view.setOnClickListener {
                    currentBook?.let {
                        onClick(it)
                    }
                }
            }

            fun setCurrent(book : Book) {
                currentBook = book
                title.text = book.title
                author.text = book.author
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_book, parent, false)

        return BookViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val current: Book = bookList.get(position)
        holder.setCurrent(current)
    }

    override fun getItemCount(): Int {
        return bookList.size()
    }

}
