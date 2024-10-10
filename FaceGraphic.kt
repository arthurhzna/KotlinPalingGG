package com.google.mlkit.vision.demo.kotlin.facedetector

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.demo.GraphicOverlay
import com.google.mlkit.vision.demo.GraphicOverlay.Graphic
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import com.google.mlkit.vision.face.FaceLandmark.LandmarkType
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

class FaceGraphic(
  overlay: GraphicOverlay,
  private val face: Face,
  private val trackingId: Int?,
  private val time: Int,
  private val direction: String,
  private val timestamp: String
) : GraphicOverlay.Graphic(overlay) {

  private val paint: Paint
  private val textPaint: Paint

  init {
    paint = Paint().apply {
      color = Color.RED // Color for the bounding box
      style = Paint.Style.STROKE
      strokeWidth = 10.0f // Set the width of the bounding box
    }

    textPaint = Paint().apply {
      color = Color.WHITE // Color for the text
      textSize = 40.0f // Set the text size
      style = Paint.Style.FILL
      // Optionally set anti-aliasing for smoother text
      isAntiAlias = true
    }
  }

  override fun draw(canvas: Canvas) {
    // Get the bounding box of the face
    val bounds = face.boundingBox

    // Convert bounding box to the overlay's coordinate system
    val left = translateX(bounds.left.toFloat())
    val top = translateY(bounds.top.toFloat())
    val right = translateX(bounds.right.toFloat())
    val bottom = translateY(bounds.bottom.toFloat())

    // Draw the bounding box on the canvas
    canvas.drawRect(left, top, right, bottom, paint)

    // Prepare text to be drawn
    val idText = "ID: ${trackingId ?: "N/A"}"
    val timeText = "Time: $time"
    val directionText = "Direction: $direction"
    val TimeStampText = "TimeStamp: $timestamp"


    // Draw text above the bounding box
    val textX = left
    var textY = top - 10 // Adjust position above the box

    // Draw each line of text
    canvas.drawText(idText, textX, textY, textPaint)
    textY -= textPaint.textSize // Move down for the next line
    canvas.drawText(timeText, textX, textY, textPaint)
    textY -= textPaint.textSize // Move down for the next line
    canvas.drawText(directionText, textX, textY, textPaint)
    textY -= textPaint.textSize // Move down for the next line
    canvas.drawText(TimeStampText, textX, textY, textPaint)

  }
}
