package com.trixxwids.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.trixxwids.app.data.WidgetDao

class WidgetViewModelFactory(private val widgetDao: WidgetDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WidgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WidgetViewModel(widgetDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
