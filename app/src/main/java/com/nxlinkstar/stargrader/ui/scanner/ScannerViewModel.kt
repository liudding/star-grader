package com.nxlinkstar.stargrader.ui.scanner

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nxlinkstar.stargrader.data.SUBJECTS
import com.nxlinkstar.stargrader.data.model.Textbook
import com.nxlinkstar.stargrader.data.model.Workbook
import com.nxlinkstar.stargrader.utils.Api
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ScannerViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    val subjects: List<SUBJECTS> = SUBJECTS.values().toList()


    private val _textbooks = MutableLiveData<List<Textbook>>()
    val textbooksState: LiveData<List<Textbook>> = _textbooks

    private val _workbooks = MutableLiveData<List<Workbook>>()
    val workbooksState: LiveData<List<Workbook>> = _workbooks


    private val _subject = MutableLiveData<SUBJECTS?>()
    val subjectState: LiveData<SUBJECTS?> = _subject

    private val _textbook = MutableLiveData<Textbook?>()
    val textbookState: LiveData<Textbook?> = _textbook

    private val _workbook = MutableLiveData<Workbook?>()
    val workbookState: LiveData<Workbook?> = _workbook


    @OptIn(DelicateCoroutinesApi::class)
    fun getTextbooks(subject: String) {
        GlobalScope.launch {
            Api.getTextbooks(subject).let {
                Log.d("SVM", it.toString())
                _textbooks.postValue(it)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun getWorkbooks(subject: String, textbook: String) {
        GlobalScope.launch {
            Api.getWorkbooks(subject, textbook).let {
                _workbooks.postValue(it)
            }
        }
    }

    fun subjectChanged(sub: String) {
        val subject = subjects.find { it.code == sub }
        _subject.value = subject
        _textbook.value = null
        _workbook.value = null

        getTextbooks(sub)
    }

    fun textbookChanged(id: String) {
        if (id == (_textbook.value?.id ?: "")) {
            return
        }

        val tb = _textbooks.value?.find { it.id == id }
        _textbook.value = tb
        _workbook.value = null

        _subject.value?.let { getWorkbooks(it.code, id) }
    }

    fun workbookChanged(id: String) {
        if (id == (_workbook.value?.id ?: "")) {
            return
        }

        val b = _workbooks.value?.find { it.id == id }
        _workbook.value = b
    }

}