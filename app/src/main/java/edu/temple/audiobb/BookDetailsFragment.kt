package edu.temple.audiobb

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider

class BookDetailsFragment : Fragment() {

    private lateinit var layout: View
    private lateinit var titleTextView: TextView
    private lateinit var authorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout = inflater.inflate(R.layout.fragment_book_details, container, false)
        titleTextView = layout.findViewById(R.id.titleTextView)
        authorTextView = layout.findViewById(R.id.authorTextView)

        ViewModelProvider(requireActivity())
            .get(BookViewModel::class.java)
            .getBook().observe(viewLifecycleOwner, { book: Book ->
                titleTextView.text = book.title
                authorTextView.text = book.author
                Log.d("AudioBB", "Changed")
            })

        return layout
    }
}