package com.trixxwids.app.data

data class WidgetElement(
    val id: String,
    val type: ElementType,
    var x: Float,
    var y: Float,
    var width: Int,
    var height: Int,
    var content: String, // Holds text, color hex, or image URI depending on type
    var color: String,
    var fontSize: Float,
    var cornerRadius: Float,
    var opacity: Float,
    var zIndex: Int
)

enum class ElementType {
    TEXT,
    SHAPE_RECTANGLE,
    SHAPE_CIRCLE,
    IMAGE,
    CLOCK,
    DATE
}
