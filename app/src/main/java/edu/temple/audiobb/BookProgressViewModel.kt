package edu.temple.audiobb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.temple.audlibplayer.PlayerService

class BookProgressViewModel : ViewModel() {
    private val bookProgress: MutableLiveData<PlayerService.BookProgress> by lazy {
        MutableLiveData<PlayerService.BookProgress>()
    }

    fun getBookProgress(): LiveData<PlayerService.BookProgress> {
        return bookProgress
    }

    fun setBookProgress(_bookProgress : PlayerService.BookProgress) {
        bookProgress.value = _bookProgress
    }
}