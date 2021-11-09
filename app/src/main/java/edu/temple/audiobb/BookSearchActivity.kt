package edu.temple.audiobb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject

public const val CHANGE: String = "bookListUpdated"
public const val RET_LIST: String = "bookList"

class BookSearchActivity : AppCompatActivity() {

    private val volleyQueue : RequestQueue by lazy {
        Volley.newRequestQueue(this)
    }

    private lateinit var resultIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_search)

        // Create an intent to pass information to calling activity
        resultIntent = Intent().putExtra(CHANGE, false)
        resultIntent = Intent().putExtra(RET_LIST, BookList())

        // Set code and data for returned result
        setResult(RESULT_OK, resultIntent)

        val searchBox = findViewById<EditText>(R.id.searchBox)
        val searchButton = findViewById<Button>(R.id.searchButton)
        val cancelButton = findViewById<Button>(R.id.cancelSearch)

        searchButton.setOnClickListener {
            val bookList = fetchBooks(searchBox.text.toString())

            // Update Intent
            resultIntent = Intent().putExtra(CHANGE, true)
            resultIntent = Intent().putExtra(RET_LIST, bookList)
            setResult(RESULT_OK, resultIntent)

            finish()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchBooks(searchTerm: String) : BookList {
        val url = "https://kamorris.com/lab/cis3515/search.php?=term$searchTerm"
        val bookList = BookList()

        volleyQueue.add(
            JsonArrayRequest(
                Request.Method.GET,
                url,
                null, { jsonArr ->
                    Log.d("Response", jsonArr.toString())
                    try {
                        for (i in 0 until jsonArr.length()) {
                            val jsonObj = jsonArr.getJSONObject(i)
                            val book = Book(
                                jsonObj.getInt("id"), jsonObj.getString("title"),
                                jsonObj.getString("author"), jsonObj.getString("cover_url")
                            )
                            bookList.add(book)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, {
                    Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
                }
            )
        )

        return bookList
    }
}