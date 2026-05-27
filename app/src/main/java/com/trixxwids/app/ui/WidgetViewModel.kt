package com.trixxwids.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trixxwids.app.data.WidgetDao
import com.trixxwids.app.data.WidgetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WidgetViewModel(private val widgetDao: WidgetDao) : ViewModel() {

    val allWidgets: Flow<List<WidgetEntity>> = widgetDao.getAllWidgets()

    fun insertWidget(widget: WidgetEntity, onResult: (Long) -> Unit) {
        viewModelScope.launch {
            val id = widgetDao.insertWidget(widget)
            onResult(id)
        }
    }

    fun deleteWidget(widget: WidgetEntity) {
        viewModelScope.launch {
            widgetDao.deleteWidget(widget)
        }
    }
}
