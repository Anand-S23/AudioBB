package edu.temple.audiobb

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val BOOK_LIST = "bookList"

class BookListFragment : Fragment() {
    private lateinit var bookRecycler : RecyclerView
    private lateinit var layout : View

    private var bookList : BookList = BookList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookList = arguments?.getSerializable(BOOK_LIST) as BookList
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_book_list, container, false)

        bookRecycler = layout.findViewById(R.id.books)
        bookRecycler.adapter = BookAdapter(bookList) { obj -> adapterOnClick(obj) }
        bookRecycler.layoutManager = LinearLayoutManager(container!!.context)

        return layout
    }

    companion object {
        fun newInstance (_bookList: BookList) : BookListFragment {
            val fragment = BookListFragment()
            val bundle = Bundle()

            bundle.putSerializable(BOOK_LIST, _bookList)

            fragment.arguments = bundle
            return fragment
        }
    }

    private fun adapterOnClick (_book : Book) {
        ViewModelProvider(requireActivity()).get(BookViewModel::class.java).setBook(_book)
        (requireActivity() as BookListInterface).selectionMade()
    }

    interface BookListInterface {
        fun selectionMade()
    }
}