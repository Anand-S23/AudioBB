package edu.temple.audiobb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

public const val CHANGE: String = "bookListUpdated"
public const val RET_LIST: String = "bookList"

class BookSearchActivity : AppCompatActivity() {

    private val volleyQueue : RequestQueue by lazy {
        Volley.newRequestQueue(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_search)

        // Create an intent to pass information to calling activity
        setIntentExtra(false, BookList())

        val searchBox = findViewById<EditText>(R.id.searchBox)
        val searchButton = findViewById<Button>(R.id.searchButton)
        val cancelButton = findViewById<Button>(R.id.cancelSearch)

        searchButton.setOnClickListener {
            fetchBooks(searchBox.text.toString())
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchBooks(searchTerm: String) {
        val url = "https://kamorris.com/lab/cis3515/search.php?term=$searchTerm"

        volleyQueue.add(
            JsonArrayRequest(
                Request.Method.GET,
                url,
                null, { jsonArr ->
                    Log.d("FetchCall", "$url, ${jsonArr.length()}, $searchTerm, $jsonArr")
                    try {
                        val bookList = BookList()

                        for (i in 0 until jsonArr.length()) {
                            val jsonObj = jsonArr.getJSONObject(i)
                            val book = Book(
                                jsonObj.getInt("id"), jsonObj.getString("title"),
                                jsonObj.getString("author"), jsonObj.getString("cover_url")
                            )
                            Log.d("Response", book.title)
                            bookList.add(book)
                        }

                        setIntentExtra(true, bookList)
                        finish()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, {
                    Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
                }
            )
        )
    }

    private fun setIntentExtra(change: Boolean, retList: BookList) {
        // Update Intent
        intent.apply {
            putExtra(CHANGE, change)
            putExtra(RET_LIST, retList)
        }

        setResult(RESULT_OK, intent)
    }
}