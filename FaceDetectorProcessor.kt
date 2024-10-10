/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mlkit.vision.demo.kotlin.facedetector

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.demo.GraphicOverlay
import com.google.mlkit.vision.demo.kotlin.VisionProcessorBase
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import java.util.Locale
// --------------------------------test-----------------------------//
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
//import com.google.mlkit.vision.demo.GraphicOverlay
import com.google.mlkit.vision.demo.GraphicOverlay.Graphic
//import com.google.mlkit.vision.face.Face
//import com.google.mlkit.vision.face.FaceLandmark
import com.google.mlkit.vision.face.FaceLandmark.LandmarkType
//import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import java.text.SimpleDateFormat
import java.util.Date
//import java.util.Locale
// --------------------------------test-----------------------------//



/** Face Detector Demo.  */
class FaceDetectorProcessor(context: Context, detectorOptions: FaceDetectorOptions?) :
  VisionProcessorBase<List<Face>>(context) {
  private val last_write_times = mutableMapOf<Int, Long>() // or any other type as needed
  private val TimeArray = mutableMapOf<Int, Int>() // Menambahkan TimeArray
  private val detector: FaceDetector
  private val FObject = mutableSetOf<Int>()
  private val TimeStamp = mutableListOf<String>()

  init {
    val options = detectorOptions
      ?: FaceDetectorOptions.Builder()
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .enableTracking()
        .build()

    detector = FaceDetection.getClient(options)

    Log.v(MANUAL_TESTING_LOG, "Face detector options: $options")
  }

  override fun stop() {
    super.stop()
    detector.close()
  }

  override fun detectInImage(image: InputImage): Task<List<Face>> {
    return detector.process(image)
  }

  @RequiresApi(Build.VERSION_CODES.N)
  override fun onSuccess(faces: List<Face>, graphicOverlay: GraphicOverlay) {
    for (face in faces) {
//      val faceGraphic = FaceGraphic(graphicOverlay, face)
//      graphicOverlay.add(faceGraphic)
      logExtrasForTesting(face)

      // Accessing the tracking ID
      val trackingId = face.trackingId
//      Log.v(TAG, "Tracking ID: $trackingId")

      val EulerY = face.headEulerAngleY
//      Log.v(TAG, "Tracking ID: $trackingId + Euler y = $ TimeArray[objectID]")
      val direction: String
      if (EulerY > 15) {
        direction = "not see"
      } else if (EulerY < -15) {
        direction = "not see"
      } else {
        direction = "see"
      }

      val objectID = face.trackingId ?: continue // Pastikan objectID tidak null

//      if (objectID !in FlagObject) {
//        FlagObject.add(objectID)

//        // Add timestamp handling
//        TimeStamp.add(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

      // Mendapatkan timestamp (misalnya, menggunakan System.currentTimeMillis())
      val timestamp = System.currentTimeMillis()

      // Memastikan TimeArray memiliki entri untuk objectID
      if (objectID !in TimeArray) {
        TimeArray[objectID] = 0
      }

      // Check if it's a new object ID and record the timestamp
      if (objectID >= TimeStamp.size) {
        // Expand the TimeStamp list to accommodate the new objectID if needed
        TimeStamp.addAll(List(objectID - TimeStamp.size + 1) { "" })
      }

      val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
      val formattedTimestamp = dateFormat.format(Date(timestamp))

      if (TimeStamp[objectID].isEmpty()) {
        // If no timestamp has been recorded for this objectID, add the current timestamp
        TimeStamp[objectID] = formattedTimestamp
      }

//      Log.v(TAG, TimeStamp[objectID])

        // Logika untuk memperbarui TimeArray
      val lastWriteTime = last_write_times[objectID]
      if (lastWriteTime == null || (timestamp - lastWriteTime) >= 1000 && direction == "see") {
        // Safely increment the value in TimeArray
        TimeArray[objectID] = TimeArray.getOrDefault(objectID, 0) + 1
        last_write_times[objectID] = timestamp
      }
      Log.v(TAG, "Tracking ID: $trackingId + Time = ${TimeArray[objectID]} + Direction = $direction + ${TimeStamp[objectID]}" )
//      Log.v(TAG, direction)
//      Log.v(TAG, TimeArray[objectID].toString())
      val allIndices = TimeArray.keys.toList()
//      graphicOverlay.add(FaceGraphic(graphicOverlay, face, direction, allIndices))
      val faceGraphic = FaceGraphic(graphicOverlay, face, trackingId, TimeArray[trackingId] ?: 0, direction, TimeStamp[objectID] )
      graphicOverlay.add(faceGraphic)

    }
  }

  override fun onFailure(e: Exception) {
    Log.e(TAG, "Face detection failed $e")
  }

  companion object {
    private const val TAG = "FaceDetectorProcessor"
    private fun logExtrasForTesting(face: Face?) {
      if (face != null) {
        Log.v(
          MANUAL_TESTING_LOG,
          "face bounding box: " + face.boundingBox.flattenToString()
        )
        Log.v(
          MANUAL_TESTING_LOG,
          "face Euler Angle X: " + face.headEulerAngleX
        )
        Log.v(
          MANUAL_TESTING_LOG,
          "face Euler Angle Y: " + face.headEulerAngleY
        )
        Log.v(
          MANUAL_TESTING_LOG,
          "face Euler Angle Z: " + face.headEulerAngleZ
        )
        // All landmarks
        val landMarkTypes = intArrayOf(
          FaceLandmark.MOUTH_BOTTOM,
          FaceLandmark.MOUTH_RIGHT,
          FaceLandmark.MOUTH_LEFT,
          FaceLandmark.RIGHT_EYE,
          FaceLandmark.LEFT_EYE,
          FaceLandmark.RIGHT_EAR,
          FaceLandmark.LEFT_EAR,
          FaceLandmark.RIGHT_CHEEK,
          FaceLandmark.LEFT_CHEEK,
          FaceLandmark.NOSE_BASE
        )
        val landMarkTypesStrings = arrayOf(
          "MOUTH_BOTTOM",
          "MOUTH_RIGHT",
          "MOUTH_LEFT",
          "RIGHT_EYE",
          "LEFT_EYE",
          "RIGHT_EAR",
          "LEFT_EAR",
          "RIGHT_CHEEK",
          "LEFT_CHEEK",
          "NOSE_BASE"
        )
        for (i in landMarkTypes.indices) {
          val landmark = face.getLandmark(landMarkTypes[i])
          if (landmark == null) {
            Log.v(
              MANUAL_TESTING_LOG,
              "No landmark of type: " + landMarkTypesStrings[i] + " has been detected"
            )
          } else {
            val landmarkPosition = landmark.position
            val landmarkPositionStr =
              String.format(Locale.US, "x: %f , y: %f", landmarkPosition.x, landmarkPosition.y)
            Log.v(
              MANUAL_TESTING_LOG,
              "Position for face landmark: " +
                      landMarkTypesStrings[i] +
                      " is :" +
                      landmarkPositionStr
            )
          }
        }
        Log.v(
          MANUAL_TESTING_LOG,
          "face left eye open probability: " + face.leftEyeOpenProbability
        )
        Log.v(
          MANUAL_TESTING_LOG,
          "face right eye open probability: " + face.rightEyeOpenProbability
        )
        Log.v(
          MANUAL_TESTING_LOG,
          "face smiling probability: " + face.smilingProbability
        )
        Log.v(
          MANUAL_TESTING_LOG,
          "face tracking id: " + face.trackingId
        )
      }
    }
  }
}