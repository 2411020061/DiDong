# MediaPipe Pose Landmarker asset

`app/src/main/assets/pose_landmarker_full.task` was built from the `pose_detection.tflite` and `pose_landmark_full.tflite` models distributed in the `mediapipe` Python package, version 0.10.21. The model files were bundled under `pose_detector.tflite` and `pose_landmarks_detector.tflite`, with image normalization metadata matching the package's Pose Solutions graphs. The bundle was checked with MediaPipe Tasks 0.10.21 on the sample images in `colab/neck_dataset_one_upload.zip`.

The upstream MediaPipe license is included here. This asset is separate from the user trained classifier `neck_stretch.tflite`.
