package com.trixxwids.app.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trixxwids.app.R
import com.trixxwids.app.data.AppDatabase
import com.trixxwids.app.data.WidgetEntity
import com.trixxwids.app.widget.TrixxWidgetProvider
import kotlinx.coroutines.launch

class GalleryFragment : Fragment() {

    private lateinit viewModel: WidgetViewModel
    private lateinit adapter: GalleryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)
        
        val dao = AppDatabase.getDatabase(requireContext()).widgetDao()
        val factory = WidgetViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[WidgetViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewGallery)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)
        
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = GalleryAdapter { widget -> applyWidgetToHomeScreen(widget) }
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.allWidgets.collect { widgets ->
                adapter.submitList(widgets)
                if (widgets.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }
        return view
    }

    private fun applyWidgetToHomeScreen(widget: WidgetEntity) {
        val appWidgetManager = AppWidgetManager.getInstance(requireContext())
        val componentName = ComponentName(requireContext(), TrixxWidgetProvider::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                // Save the selected widget ID to shared preferences so the Provider knows what to draw
                val prefs = requireContext().getSharedPreferences("trixx_prefs", Context.MODE_PRIVATE)
                prefs.edit().putInt("pending_widget_id", widget.id).apply()

                val successCallback = PendingIntent.getBroadcast(
                    requireContext(), 0, Intent(requireContext(), TrixxWidgetProvider::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                appWidgetManager.requestPinAppWidget(componentName, null, successCallback)
            } else {
                Toast.makeText(requireContext(), "Pinning not supported on this launcher.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(requireContext(), "Please add the widget manually from the home screen.", Toast.LENGTH_LONG).show()
        }
    }
}
