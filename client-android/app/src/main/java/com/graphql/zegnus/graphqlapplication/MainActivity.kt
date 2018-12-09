package com.graphql.zegnus.graphqlapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.graphql.zegnus.graphqlapplication.BookViewModel.Feedback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val bookViewModel: BookViewModel = BookViewModel()
    private val requestBookCallback: (Feedback) -> Unit = { feedback ->
        when (feedback) {
            is Feedback.Loading -> result.text = "Loading"
            is Feedback.Error -> result.text = feedback.message
            is Feedback.Loaded -> {
                result.text = "Loaded"
                book_id.text = feedback.book.id
                book_name.text = feedback.book.name
                book_genre.text = feedback.book.genre
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_request.setOnClickListener {
            bookViewModel.requestBookId(input_book_id.text.toString(), requestBookCallback)
        }
    }

    override fun onDestroy() {
        bookViewModel.stop()
        super.onDestroy()
    }
}
