package vn.edu.mku.ecoscan.ui.scan;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.Rational;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.text.SimpleDateFormat;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import vn.edu.mku.ecoscan.R;
import vn.edu.mku.ecoscan.ml.YoloPoseDetector;
import vn.edu.mku.ecoscan.ui.MainActivity;

public class ExerciseFragment extends Fragment {
    private static final String ARG_EXERCISE = "exercise";
    private ExerciseType exerciseType = ExerciseType.NECK;

    public static ExerciseFragment forType(ExerciseType type) {
        ExerciseFragment fragment = new ExerciseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE, type.key);
        fragment.setArguments(args);
        return fragment;
    }

    private PreviewView preview;
    private FrameLayout cameraFrame;
    private PoseOverlayView overlay;
    private TextView cameraStatus, recording, coach, posture, progress, percent, start, rotateBtn, flipCameraBtn;
    private View cameraActions;
    private ImageView guideImage, hostPreview;
    private ExerciseTimerView timer;
    private YoloPoseDetector yoloPoseDetector;
    private PoseLandmarker poseLandmarker;
    private PoseSuggestionModel suggestionModel;
    private StretchClassifier stretchClassifier;
    private ProcessCameraProvider cameraProvider;
    private final ExecutorService analysisExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService webcamExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean analyzingWebcam = new AtomicBoolean();
    private final AtomicInteger webcamSession = new AtomicInteger();
    private boolean frontCamera = true, useFrontCamera = true, cameraRequested, finished;
    private static final int[] ROTATIONS = {
            Surface.ROTATION_0, Surface.ROTATION_90,
            Surface.ROTATION_180, Surface.ROTATION_270
    };
    private int rotationIndex = 1;
    private volatile boolean webcamRunning, hostMode;
    private Bitmap displayedWebcamFrame;
    private boolean modelLoading;
    private final PostureHoldTimer holdTimer = new PostureHoldTimer(15000, 1200);
    private final PoseAccuracyTracker accuracyTracker = new PoseAccuracyTracker();
    private final ActivityResultLauncher<String> cameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                if (!isAdded() || getView() == null) return;
                if (granted) startCamera();
                else {
                    cameraRequested = false;
                    start.setEnabled(true);
                    start.setVisibility(View.VISIBLE);
                    cameraStatus.setText("Cần cấp quyền camera");
                }
            });

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_exercise, container, false);
        exerciseType = ExerciseType.fromKey(getArguments() == null ? null
                : getArguments().getString(ARG_EXERCISE));
        ((TextView) root.findViewById(R.id.exerciseTitle)).setText(exerciseType.title);
        ((TextView) root.findViewById(R.id.exerciseSubtitle)).setText(exerciseType.instruction);
        ((TextView) root.findViewById(R.id.exerciseBadge)).setText("15 giây");
        preview = root.findViewById(R.id.exercisePreview);
        hostPreview = root.findViewById(R.id.exerciseHostPreview);
        hostPreview.setScaleX(-1f);
        cameraFrame = root.findViewById(R.id.exerciseCameraFrame);
        cameraFrame.addOnLayoutChangeListener((view, left, top, right, bottom,
                oldLeft, oldTop, oldRight, oldBottom) -> {
            int width = right - left;
            if (width > 0 && bottom - top != width) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = width;
                view.setLayoutParams(params);
            }
        });
        preview.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        preview.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        guideImage = root.findViewById(R.id.exerciseGuideImage);
        guideImage.setImageResource(exerciseType.imageRes);
        guideImage.setVisibility(View.GONE);
        if (exerciseType != ExerciseType.NECK) {
            guideImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            guideImage.setBackgroundColor(Color.parseColor("#F7FAF5"));
        }
        overlay = root.findViewById(R.id.exerciseOverlay);
        cameraStatus = root.findViewById(R.id.exerciseCameraStatus);
        cameraStatus.setVisibility(View.VISIBLE);
        cameraStatus.setText("Đang chờ camera…");
        cameraStatus.setOnClickListener(v -> {
            if ((requireContext().getApplicationInfo().flags
                    & ApplicationInfo.FLAG_DEBUGGABLE) == 0) return;
            LinearLayout fields = new LinearLayout(requireContext());
            fields.setOrientation(LinearLayout.VERTICAL);
            fields.setPadding(32, 0, 32, 0);
            EditText address = new EditText(requireContext());
            address.setHint("IP máy tính");
            address.setSingleLine(true);
            address.setText(WebcamBridge.host(requireContext()));
            address.setSelectAllOnFocus(true);
            EditText code = new EditText(requireContext());
            code.setHint("Mã ghép nối từ script Windows");
            code.setSingleLine(true);
            code.setText(WebcamBridge.token(requireContext()));
            fields.addView(address);
            fields.addView(code);
            new AlertDialog.Builder(requireContext())
                    .setTitle("Webcam máy tính")
                    .setMessage("Chạy tools\\start_webcam_windows.bat, rồi nhập IP và mã ghép nối do script in ra.")
                    .setView(fields)
                    .setPositiveButton("Kết nối", (dialog, which) -> {
                        String host = address.getText().toString().trim();
                        String token = code.getText().toString().trim();
                        if (!WebcamBridge.validHost(host) || token.isEmpty()) {
                            Toast.makeText(requireContext(), "Cần IP và mã ghép nối hợp lệ",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        WebcamBridge.setConnection(requireContext(), host, token);
                        webcamRunning = false;
                        webcamSession.incrementAndGet();
                        startCamera();
                    })
                    .setNegativeButton("Hủy", null).show();
        });
        timer = root.findViewById(R.id.exerciseTimer);
        timer.setVisibility(View.GONE);
        recording = root.findViewById(R.id.exerciseRecording);
        recording.setVisibility(View.GONE);
        coach = root.findViewById(R.id.exerciseCoach);
        coach.setVisibility(View.GONE);
        posture = root.findViewById(R.id.exerciseLandmarks);
        root.findViewById(R.id.exerciseCard).setVisibility(View.VISIBLE);
        progress = root.findViewById(R.id.exerciseProgress);
        percent = root.findViewById(R.id.exercisePercent);
        start = root.findViewById(R.id.exerciseStart);
        start.setVisibility(View.GONE);
        cameraActions = root.findViewById(R.id.exerciseCameraActions);
        rotateBtn = root.findViewById(R.id.exerciseRotate);
        if (rotateBtn != null) {
            rotateBtn.setOnClickListener(v -> {
                rotationIndex = (rotationIndex + 1) % ROTATIONS.length;
                if (hostMode) {
                    applyCameraRotation();
                } else if (cameraProvider != null && !finished) {
                    bindCamera();
                }
            });
        }
        flipCameraBtn = root.findViewById(R.id.exerciseFlipCamera);
        if (flipCameraBtn != null) {
            flipCameraBtn.setOnClickListener(v -> {
                if (cameraProvider != null && !finished) {
                    useFrontCamera = !useFrontCamera;
                    bindCamera();
                }
            });
        }
        overlay.setMode("Exercise");
        coach.setText(exerciseType.instruction);
        root.findViewById(R.id.exerciseExit).setOnClickListener(v -> ((MainActivity) requireActivity()).navigateToHome());
        start.setOnClickListener(v -> {
            if (finished) return;
            cameraRequested = true;
            start.setEnabled(false);
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) startCamera();
            else cameraPermission.launch(Manifest.permission.CAMERA);
        });
        cameraRequested = true;
        start.setEnabled(false);
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA);
        }
        return root;
    }

    private void startCamera() {
        cameraStatus.setText("Đang mở camera…");
        start.setVisibility(View.GONE);
        guideImage.setVisibility(View.GONE);
        if (WebcamBridge.canUse(requireContext())) {
            int session = webcamSession.incrementAndGet();
            webcamExecutor.execute(() -> {
                try {
                    Bitmap first = WebcamBridge.frame(WebcamBridge.host(requireContext()),
                            WebcamBridge.token(requireContext()));
                    if (isAdded()) requireActivity().runOnUiThread(() -> startWebcam(first, session));
                    else first.recycle();
                } catch (Exception e) {
                    Log.i("ExerciseFragment", "PC webcam bridge is unavailable", e);
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        if (getView() == null || finished || session != webcamSession.get()) return;
                        hostMode = false;
                        cameraStatus.setText("Chạm để nhập IP và mã webcam máy tính");
                        start.setEnabled(true);
                        start.setVisibility(View.VISIBLE);
                    });
                }
            });
        } else {
            preview.setVisibility(View.VISIBLE);
            preview.post(this::bindCamera);
        }
        if (modelLoading || yoloPoseDetector != null || poseLandmarker != null) return;
        modelLoading = true;
        analysisExecutor.execute(() -> {
            try {
                try {
                    yoloPoseDetector = new YoloPoseDetector(requireContext().getApplicationContext());
                } catch (Throwable e) {
                    // OutOfMemoryError là Error, không phải Exception: phải bắt Throwable
                    // để app thoái lui sang MediaPipe thay vì crash cả tiến trình.
                    Log.w("ExerciseFragment", "YoloPoseDetector unavailable, falling back to MediaPipe", e);
                    yoloPoseDetector = null;
                }
                try {
                    suggestionModel = new PoseSuggestionModel(requireContext().getApplicationContext());
                } catch (Exception e) {
                    Log.w("ExerciseFragment", "Suggestion model unavailable", e);
                    suggestionModel = null;
                }
                try {
                    stretchClassifier = new StretchClassifier(requireContext().getApplicationContext());
                } catch (Exception e) {
                    Log.w("ExerciseFragment", "StretchClassifier unavailable", e);
                    stretchClassifier = null;
                }
                try {
                    BaseOptions baseOptions = BaseOptions.builder()
                            .setModelAssetPath("pose_landmarker_full.task").build();
                    PoseLandmarker.PoseLandmarkerOptions options = PoseLandmarker.PoseLandmarkerOptions
                            .builder().setBaseOptions(baseOptions).setRunningMode(RunningMode.VIDEO)
                            .setNumPoses(1).build();
                    poseLandmarker = PoseLandmarker.createFromOptions(
                            requireContext().getApplicationContext(), options);
                } catch (Exception e) {
                    Log.w("ExerciseFragment", "MediaPipe PoseLandmarker unavailable", e);
                    poseLandmarker = null;
                }
                modelLoading = false;
                if (yoloPoseDetector == null && poseLandmarker == null) {
                    throw new IllegalStateException("Neither YoloPoseDetector nor PoseLandmarker initialized");
                }
            } catch (Throwable e) {
                modelLoading = false;
                if (yoloPoseDetector != null) { yoloPoseDetector.close(); yoloPoseDetector = null; }
                if (suggestionModel != null) { suggestionModel.close(); suggestionModel = null; }
                if (stretchClassifier != null) { stretchClassifier.close(); stretchClassifier = null; }
                Log.e("ExerciseFragment", "Cannot initialize pose models", e);
                if (isAdded()) requireActivity().runOnUiThread(() -> {
                    if (getView() == null) return;
                    cameraRequested = false;
                    start.setEnabled(true);
                    start.setVisibility(View.VISIBLE);
                    cameraStatus.setText("Không tải được nhận diện tư thế. Nhấn để thử lại");
                });
            }
        });
    }

    @Override public void onViewCreated(@NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preview.getPreviewStreamState().observe(getViewLifecycleOwner(), state -> {
            if (!hostMode && state == PreviewView.StreamState.STREAMING && cameraRequested
                    && !WebcamBridge.canUse(requireContext()))
                cameraStatus.setText("● Camera đang hoạt động");
        });
    }

    private void startWebcam(Bitmap first, int session) {
        if (getView() == null || finished || session != webcamSession.get()) {
            first.recycle(); return;
        }
        if (cameraProvider != null) cameraProvider.unbindAll();
        hostMode = true;
        webcamRunning = true;
        frontCamera = true;
        preview.setVisibility(View.GONE);
        hostPreview.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.VISIBLE);
        if (cameraActions != null) cameraActions.setVisibility(View.VISIBLE);
        applyCameraRotation();
        cameraStatus.setText("● Webcam máy tính đang hoạt động");
        showWebcamFrame(first);
        webcamExecutor.execute(() -> {
            while (webcamRunning && session == webcamSession.get()
                    && !Thread.currentThread().isInterrupted()) {
                try {
                    Bitmap frame = WebcamBridge.frame(WebcamBridge.host(requireContext()),
                            WebcamBridge.token(requireContext()));
                    if (!webcamRunning || session != webcamSession.get()) {
                        frame.recycle(); break;
                    }
                    if (isAdded()) requireActivity().runOnUiThread(() -> showWebcamFrame(frame));
                    else frame.recycle();
                    Thread.sleep(80);
                } catch (Exception e) {
                    if (session != webcamSession.get()) break;
                    Log.e("ExerciseFragment", "PC webcam disconnected", e);
                    webcamRunning = false;
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        if (getView() == null || finished) return;
                        cameraStatus.setText("Mất webcam máy tính · chạm để nhập IP");
                        start.setEnabled(true);
                        start.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }

    private void showWebcamFrame(Bitmap frame) {
        if (getView() == null || finished || !hostMode) { frame.recycle(); return; }
        if (displayedWebcamFrame != null) displayedWebcamFrame.recycle();
        displayedWebcamFrame = frame;
        hostPreview.setImageBitmap(frame);
        if ((yoloPoseDetector == null && poseLandmarker == null)
                || !analyzingWebcam.compareAndSet(false, true)) return;
        int viewWidth = hostPreview.getWidth(), viewHeight = hostPreview.getHeight();
        if (viewWidth == 0 || viewHeight == 0) { analyzingWebcam.set(false); return; }
        Bitmap analysisFrame = frame.copy(Bitmap.Config.ARGB_8888, false);
        float scale = Math.max(viewWidth / (float) frame.getWidth(),
                viewHeight / (float) frame.getHeight());
        float cropWidth = viewWidth / scale, cropHeight = viewHeight / scale;
        float[] crop = {(frame.getWidth() - cropWidth) / 2f,
                (frame.getHeight() - cropHeight) / 2f,
                (frame.getWidth() + cropWidth) / 2f,
                (frame.getHeight() + cropHeight) / 2f};
        analysisExecutor.execute(() -> {
            try { analyzeBitmap(analysisFrame, crop); }
            finally {
                analysisFrame.recycle();
                analyzingWebcam.set(false);
            }
        });
    }

    private void bindCamera() {
        if (!isAdded() || getView() == null || finished) return;
        if (preview.getViewPort() == null) {
            preview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override public void onGlobalLayout() {
                    if (preview.getViewPort() == null) return;
                    preview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    bindCamera();
                }
            });
            return;
        }
        ProcessCameraProvider.getInstance(requireContext()).addListener(() -> {
            if (!isAdded() || getView() == null || finished) return;
            try {
                cameraProvider = ProcessCameraProvider.getInstance(requireContext()).get();
                boolean hasFront = cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
                boolean hasBack = cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);

                CameraSelector selector;
                if (useFrontCamera && hasFront) {
                    selector = CameraSelector.DEFAULT_FRONT_CAMERA;
                    frontCamera = true;
                } else if (hasBack) {
                    selector = CameraSelector.DEFAULT_BACK_CAMERA;
                    frontCamera = false;
                } else if (hasFront) {
                    selector = CameraSelector.DEFAULT_FRONT_CAMERA;
                    frontCamera = true;
                } else {
                    cameraRequested = false;
                    start.setEnabled(true);
                    start.setVisibility(View.VISIBLE);
                    cameraStatus.setText("Không tìm thấy camera");
                    return;
                }

                Preview.Builder previewBuilder = new Preview.Builder();
                ImageAnalysis.Builder analysisBuilder = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888);

                int displayRotation = preview.getDisplay() != null
                        ? preview.getDisplay().getRotation() : Surface.ROTATION_0;
                int targetRotation = ROTATIONS[(displayRotation + rotationIndex) % ROTATIONS.length];
                previewBuilder.setTargetRotation(targetRotation);
                analysisBuilder.setTargetRotation(targetRotation);

                Preview cameraPreview = previewBuilder.build();
                cameraPreview.setSurfaceProvider(preview.getSurfaceProvider());
                ImageAnalysis analysis = analysisBuilder.build();
                analysis.setAnalyzer(analysisExecutor, this::analyzeFrame);

                overlay.setVisibility(View.VISIBLE);
                start.setVisibility(View.GONE);
                // PreviewView mirrors the front camera itself; landmarks are mirrored in the overlay.
                preview.setScaleX(1f);
                preview.setScaleY(1f);
                overlay.setScaleX(1f);
                overlay.setScaleY(1f);
                preview.setRotation(0f);
                overlay.setRotation(0f);
                applyCameraRotation();

                cameraProvider.unbindAll();
                ViewPort viewPort = preview.getViewPort();
                if (viewPort == null) viewPort = new ViewPort.Builder(
                        new Rational(Math.max(1, preview.getWidth()), Math.max(1, preview.getHeight())),
                        targetRotation)
                        .setScaleType(ViewPort.FILL_CENTER)
                        .build();
                UseCaseGroup useCases = new UseCaseGroup.Builder()
                        .setViewPort(viewPort).addUseCase(cameraPreview)
                        .addUseCase(analysis).build();
                cameraStatus.setText("Đang nhận hình camera…");
                cameraProvider.bindToLifecycle(this, selector, useCases);
            } catch (Exception e) {
                Log.e("ExerciseFragment", "Error binding camera", e);
                cameraRequested = false;
                start.setEnabled(true);
                start.setVisibility(View.VISIBLE);
                cameraStatus.setText("Không mở được camera, nhấn để thử lại");
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }


    private void applyCameraRotation() {
        float angle = rotationIndex * 90f;
        if (hostPreview != null) hostPreview.setRotation(angle);
        if (overlay != null) overlay.setRotation(hostMode ? angle : 0f);
        if (preview != null && hostMode) preview.setRotation(angle);
    }

    private void analyzeFrame(ImageProxy proxy) {
        if (finished || (yoloPoseDetector == null && poseLandmarker == null)) {
            proxy.close();
            return;
        }
        boolean proxyClosed = false;
        try {
            Bitmap raw = copyRgba(proxy);
            int rotation = proxy.getImageInfo().getRotationDegrees();
            Rect cropRect = proxy.getCropRect();
            float[] uprightCrop = PoseOverlayTransform.rotateCrop(cropRect.left, cropRect.top,
                    cropRect.right, cropRect.bottom, proxy.getWidth(), proxy.getHeight(), rotation);
            proxy.close();
            proxyClosed = true;
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap upright = Bitmap.createBitmap(raw, 0, 0, raw.getWidth(), raw.getHeight(),
                    matrix, true);
            if (upright != raw) raw.recycle();
            analyzeBitmap(upright, uprightCrop);
            upright.recycle();
        } catch (Exception e) {
            if (!proxyClosed) proxy.close();
            showAnalysisError(e);
        }
    }

    private void analyzeBitmap(Bitmap upright, float[] uprightCrop) {
        try {
            int imageWidth = upright.getWidth();
            int imageHeight = upright.getHeight();
            List<NormalizedLandmark> landmarks = null;
            if (yoloPoseDetector != null) {
                landmarks = yoloPoseDetector.detect(upright);
            } else if (poseLandmarker != null) {
                MPImage image = new BitmapImageBuilder(upright).build();
                PoseLandmarkerResult result = poseLandmarker.detectForVideo(image,
                        SystemClock.uptimeMillis());
                landmarks = result.landmarks().isEmpty() ? null : result.landmarks().get(0);
            }
            NeckPoseEvaluator.Result candidate = exerciseType == ExerciseType.NECK
                    ? NeckPoseEvaluator.evaluate(landmarks)
                    : ExercisePoseEvaluator.evaluate(exerciseType, landmarks);
            NeckPoseEvaluator.Result assessment = candidate.correct
                    && !PoseOverlayTransform.requiredPointsInCrop(exerciseType, landmarks,
                            imageWidth, imageHeight, uprightCrop)
                    ? new NeckPoseEvaluator.Result(false,
                            NeckPoseEvaluator.StretchType.NONE,
                            "Đặt các khớp cần theo dõi vào giữa khung hình") : candidate;
            String suggestion = (candidate.correct || suggestionModel == null) ? null
                    : suggestionModel.suggest(exerciseType, landmarks);
            if (suggestion != null)
                assessment = new NeckPoseEvaluator.Result(false,
                        NeckPoseEvaluator.StretchType.NONE, assessment.guidance + ". " + suggestion);

            if (stretchClassifier != null && landmarks != null && landmarks.size() == 33) {
                try {
                    float[] probs = stretchClassifier.classifyLandmarks(landmarks, 0.3f);
                    if (probs != null) {
                        int best = 0;
                        for (int i = 1; i < probs.length; i++) {
                            if (probs[i] > probs[best]) best = i;
                        }
                        if (probs[best] > 0.45f) {
                            Log.d("ExerciseFragment", "StretchClassifier: "
                                    + stretchClassifier.labelAt(best) + " (" + probs[best] + ")");
                        }
                    }
                } catch (Exception ignored) { }
            }

            NeckPoseEvaluator.Result displayedAssessment = assessment;
            final List<NormalizedLandmark> detectedLandmarks = landmarks;
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                if (getView() == null || finished) return;
                overlay.setMediaPipePose(detectedLandmarks, imageWidth, imageHeight,
                        frontCamera, uprightCrop);
                updatePose(displayedAssessment);
            });
        } catch (Exception e) {
            showAnalysisError(e);
        }
    }

    private void showAnalysisError(Exception e) {
        Log.e("ExerciseFragment", "Cannot analyze pose", e);
        if (isAdded()) requireActivity().runOnUiThread(() -> {
            if (getView() != null) {
                cameraStatus.setText("Không đọc được khung hình");
                overlay.setMediaPipePose(null, 1, 1, frontCamera, null);
                updatePose(new NeckPoseEvaluator.Result(false,
                        NeckPoseEvaluator.StretchType.NONE,
                        "Đặt đầu và hai vai vào khung hình"));
            }
        });
    }

    private Bitmap copyRgba(ImageProxy proxy) {
        int width = proxy.getWidth(), height = proxy.getHeight();
        ImageProxy.PlaneProxy plane = proxy.getPlanes()[0];
        ByteBuffer source = plane.getBuffer().duplicate();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (plane.getPixelStride() == 4 && plane.getRowStride() == width * 4) {
            source.rewind();
            bitmap.copyPixelsFromBuffer(source);
            return bitmap;
        }
        ByteBuffer packed = ByteBuffer.allocateDirect(width * height * 4);
        if (plane.getPixelStride() == 4) {
            byte[] rowBytes = new byte[width * 4];
            for (int row = 0; row < height; row++) {
                source.position(row * plane.getRowStride());
                source.get(rowBytes);
                packed.put(rowBytes);
            }
        } else {
            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {
                    int offset = row * plane.getRowStride() + column * plane.getPixelStride();
                    for (int channel = 0; channel < 4; channel++)
                        packed.put(source.get(offset + channel));
                }
            }
        }
        packed.flip();
        bitmap.copyPixelsFromBuffer(packed);
        return bitmap;
    }

    private void updatePose(NeckPoseEvaluator.Result result) {
        accuracyTracker.observe(result.correct);
        long heldMs = holdTimer.update(result.correct, SystemClock.elapsedRealtime());
        int seconds = (int) (heldMs / 1000);
        timer.setSeconds((int) Math.ceil((15000 - heldMs) / 1000.0));
        progress.setText(seconds + " / 15 giây");
        percent.setText(accuracyTracker.percent() + "%");
        posture.setText(result.correct
                ? (exerciseType == ExerciseType.NECK
                    ? (result.type == NeckPoseEvaluator.StretchType.SIDE ? "Nghiêng bên" : "Cúi chéo")
                    : "Đúng tư thế")
                : "Chưa đúng");
        recording.setText(result.correct ? "● Đang ghi nhận" : "● Chưa ghi nhận");
        int feedbackColor = Color.parseColor(result.correct ? "#087D4A" : "#D34B3C");
        posture.setTextColor(feedbackColor);
        recording.setTextColor(feedbackColor);
        overlay.setExerciseCorrectness(result.correct);
        coach.setTextColor(feedbackColor);
        coach.setText(result.guidance);
        if (heldMs >= 15000) completeExercise();
    }

    private void completeExercise() {
        if (finished) return;
        finished = true;
        if (cameraProvider != null) cameraProvider.unbindAll();
        String key = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        ExerciseSessionStore.record(requireContext(), key, exerciseType,
                accuracyTracker.percent());
        cameraStatus.setText("● Đã hoàn thành");
        recording.setText("● Đã lưu");
        coach.setText("Đã lưu bài tập");
        Toast.makeText(requireContext(), "Đã hoàn thành " + exerciseType.title,
                Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) ((MainActivity) requireActivity()).navigateToTracking();
        }, 700);
    }

    @Override public void onDestroyView() {
        webcamRunning = false;
        webcamSession.incrementAndGet();
        webcamExecutor.shutdownNow();
        if (cameraProvider != null) cameraProvider.unbindAll();
        hostPreview.setImageBitmap(null);
        if (displayedWebcamFrame != null) {
            displayedWebcamFrame.recycle();
            displayedWebcamFrame = null;
        }
        analysisExecutor.execute(() -> {
            if (yoloPoseDetector != null) yoloPoseDetector.close();
            if (poseLandmarker != null) poseLandmarker.close();
            if (suggestionModel != null) suggestionModel.close();
            if (stretchClassifier != null) stretchClassifier.close();
            yoloPoseDetector = null;
            poseLandmarker = null;
            suggestionModel = null;
            stretchClassifier = null;
        });
        super.onDestroyView();
    }
    @Override public void onDestroy() {
        analysisExecutor.shutdown();
        super.onDestroy();
    }
}

