package edu.temple.audiobb

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class BookSearchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_search)

        findViewById<Button>(R.id.searchButton).setOnClickListener {

            val url = "https://kamorris.com/lab/cis3515/search.php?term=" +
                    findViewById<EditText>(R.id.searchStringEditText).text.toString()

            Volley.newRequestQueue(this).add(
                JsonArrayRequest(Request.Method.GET, url, null, {
                    setResult(RESULT_OK,
                        Intent().apply {
                            putExtra(BookList.BOOKLIST_KEY, BookList().apply{populateBooks(it)})
                            putExtra(JSON_ARR, it.toString())
                        }
                    )
                    finish()
                }, {})
            )
        }
    }
}