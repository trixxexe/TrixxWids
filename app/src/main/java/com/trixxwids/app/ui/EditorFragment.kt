package com.trixxwids.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.trixxwids.app.R
import com.trixxwids.app.data.AppDatabase
import com.trixxwids.app.data.ElementType
import com.trixxwids.app.data.WidgetElement
import com.trixxwids.app.data.WidgetEntity
import com.trixxwids.app.utils.BitmapHelper
import com.trixxwids.app.utils.GyroscopeHelper
import java.util.UUID

class EditorFragment : Fragment() {

    private lateinit var canvasView: CanvasView
    private lateinit var viewModel: WidgetViewModel
    private var gyroHelper: GyroscopeHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editor, container, false)

        val dao = AppDatabase.getDatabase(requireContext()).widgetDao()
        val factory = WidgetViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[WidgetViewModel::class.java]

        val spinnerSize = view.findViewById<Spinner>(R.id.spinnerSize)
        val canvasContainer = view.findViewById<FrameLayout>(R.id.canvasContainer)
        val btnAddElement = view.findViewById<Button>(R.id.btnAddElement)
        val switchGyro = view.findViewById<SwitchMaterial>(R.id.switchGyro)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        // Setup Widget Size Spinner
        val sizes = arrayOf("2x1", "2x2", "4x1", "4x2", "4x4")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sizes)
        spinnerSize.adapter = spinnerAdapter

        // Setup the Canvas
        canvasView = CanvasView(requireContext())
        canvasContainer.addView(canvasView)

        // Setup Gyroscope Listener
        gyroHelper = GyroscopeHelper(requireContext()) { pitch, roll ->
            // Update canvas with tiny adjustments based on device tilt
            canvasView.gyroOffsetX = roll * 5f
            canvasView.gyroOffsetY = pitch * 5f
        }

        switchGyro.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                gyroHelper?.start()
            } else {
                gyroHelper?.stop()
                canvasView.gyroOffsetX = 0f
                canvasView.gyroOffsetY = 0f
            }
        }

        btnAddElement.setOnClickListener { showAddElementDialog() }
        btnSave.setOnClickListener { saveWidget(spinnerSize.selectedItem.toString(), switchGyro.isChecked) }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gyroHelper?.stop() // Prevent battery drain when leaving the screen
    }

    private fun showAddElementDialog() {
        val types = arrayOf("Text", "Rectangle Shape", "Circle Shape", "Clock")
        AlertDialog.Builder(requireContext())
            .setTitle("Add Element")
            .setItems(types) { _, which ->
                val type = when (which) {
                    0 -> ElementType.TEXT
                    1 -> ElementType.SHAPE_RECTANGLE
                    2 -> ElementType.SHAPE_CIRCLE
                    3 -> ElementType.CLOCK
                    else -> ElementType.TEXT
                }
                
                val content = if (type == ElementType.CLOCK) "12:00" else "New Element"
                
                val newElement = WidgetElement(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    x = 100f,
                    y = 100f,
                    width = 200,
                    height = 200,
                    content = content,
                    color = "#0061A4", // Default app primary color
                    fontSize = 60f,
                    cornerRadius = 16f,
                    opacity = 1f,
                    zIndex = canvasView.getElements().size
                )
                canvasView.addElement(newElement)
            }
            .show()
    }

    private fun saveWidget(sizeStr: String, isGyroEnabled: Boolean) {
        // Generate the Bitmap image to use as the widget thumbnail/actual background
        val bitmap = canvasView.generateBitmap()
        val filename = "widget_${System.currentTimeMillis()}"
        val imagePath = BitmapHelper.saveBitmapToInternalStorage(requireContext(), bitmap, filename)

        // Save the layer data for future editing
        val elementsJson = Gson().toJson(canvasView.getElements())

        val dims = sizeStr.split("x")
        val gridW = dims[0].toIntOrNull() ?: 2
        val gridH = dims[1].toIntOrNull() ?: 2

        val entity = WidgetEntity(
            name = "My Trixx Widget",
            gridWidth = gridW,
            gridHeight = gridH,
            isGyroEnabled = isGyroEnabled,
            elementsJson = elementsJson,
            previewImagePath = imagePath
        )

        // Save to Database
        viewModel.insertWidget(entity) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Widget Saved! Go to Gallery to Apply.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
