package org.tensorflow.yolo.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tensorflow.yolo.R;
import org.tensorflow.yolo.TensorFlowImageRecognizer;
import org.tensorflow.yolo.model.BoxPosition;
import org.tensorflow.yolo.model.Recognition;
import org.tensorflow.yolo.model.UserInfoDTO;
import org.tensorflow.yolo.util.FirebaseHelper;
import org.tensorflow.yolo.util.ImageUtils;
import org.tensorflow.yolo.view.components.BorderedText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import static org.tensorflow.yolo.Config.INPUT_SIZE;
import static org.tensorflow.yolo.Config.LOGGING_TAG;

/**
 * Classifier activity class
 * Modified by Zoltan Szabo
 */
public class ClassifierActivity extends TextToSpeechActivity implements OnImageAvailableListener {
    private boolean MAINTAIN_ASPECT = true;
    private float TEXT_SIZE_DIP = 10;

    private TensorFlowImageRecognizer recognizer;
    private Integer sensorOrientation;
    private int previewWidth = 0;
    private int previewHeight = 0;
    private Bitmap croppedBitmap = null;
    private boolean computing = false;
    private Matrix frameToCropTransform;

    private RelativeLayout relativeLayout;
    private List<View> recognizedViews = new ArrayList<>();
    private OverlayView overlayView;
    private BorderedText borderedText;
    private long lastProcessingTimeMs;
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fbHelper = FirebaseHelper.getInstance();
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        recognizer = TensorFlowImageRecognizer.create(getAssets());

        relativeLayout = (RelativeLayout) findViewById(R.id.parent_layout);
        overlayView = (OverlayView) findViewById(R.id.overlay);
        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        final int screenOrientation = getWindowManager().getDefaultDisplay().getRotation();

        Log.i(LOGGING_TAG, String.format("Sensor orientation: %d, Screen orientation: %d",
                rotation, screenOrientation));

        sensorOrientation = rotation + screenOrientation;

        Log.i(LOGGING_TAG, String.format("Initializing at size %dx%d", previewWidth, previewHeight));

        croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);

        frameToCropTransform = ImageUtils.getTransformationMatrix(previewWidth, previewHeight,
                INPUT_SIZE, INPUT_SIZE, sensorOrientation, MAINTAIN_ASPECT);
        frameToCropTransform.invert(new Matrix());

        addCallback((final Canvas canvas) -> renderAdditionalInformation(canvas));
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = null;

        try {
            image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (computing) {
                image.close();
                return;
            }

            computing = true;
            fillCroppedBitmap(image);
            image.close();
        } catch (final Exception ex) {
            if (image != null) {
                image.close();
            }
            Log.e(LOGGING_TAG, ex.getMessage());
        }

        runInBackground(() -> {
            final long startTime = SystemClock.uptimeMillis();
            Log.d(LOGGING_TAG, "bitmap: " + croppedBitmap.toString());
            final List<Recognition> results = recognizer.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
//            overlayView.setResults(results);
            runOnUiThread(() -> {
                for (View v : recognizedViews) {
                    relativeLayout.removeView(v);
                }
                recognizedViews.clear();
//                Recognition r1 = new Recognition(1, "BBakBBak", 1.0f, new BoxPosition(1,1, 2, 2));
//                Recognition r2 = new Recognition(1, "WassupMan", 1.0f, new BoxPosition(400, 400, 2, 2));
//                results.add(r1);
//                results.add(r2);
                HashMap<String, Pair<RectF, Recognition>> titleMap = overlayView.getTitleBoxMap(results);
                results.clear();
                for (Map.Entry<String, Pair<RectF, Recognition>> entry : titleMap.entrySet()) {
                    String title = entry.getKey();
                    RectF box = entry.getValue().first;
                    Recognition recog = entry.getValue().second;
                    UserInfoDTO user = fbHelper.getUserBySticker(title);
                    String user_id = fbHelper.getUserKey(title);

                    Button button = new Button(ClassifierActivity.this);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            computing = false;
                            if (user == null) return;
                            Intent intent = new Intent(ClassifierActivity.this, EditUserInfoActivity.class);
                            EditUserInfoActivity.fromDiscover = true;
                            intent.putExtra("title", title);
                            intent.putExtra("name", user.getName());
                            intent.putExtra("job", user.getJob());
                            intent.putExtra("email", user.getEmail());
                            intent.putExtra("major", user.getMajor());
                            intent.putExtra("history", user.getHistory());
                            startActivity(intent);
                        }
                    });
                    RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rel_btn.leftMargin = (int) box.left;
                    rel_btn.topMargin = (int) box.top;
                    button.setLayoutParams(rel_btn);
                    button.setBackgroundColor(getResources().getColor(R.color.control_background));
//                    if (user != null) {
//                        button.setText(user.getName() + ":" + user.getJob());
//                    }else{
//                        button.setText("유저 정보 없음");
//                    }
                    button.setText(String.format("%s:%.2f", title, recog.getConfidence()));
                    relativeLayout.addView(button);
                    recognizedViews.add(button);
                }
            });
//            speak(results);
//            requestRender();
            computing = false;
        });
    }

    private void fillCroppedBitmap(final Image image) {
        Bitmap rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        rgbFrameBitmap.setPixels(ImageUtils.convertYUVToARGB(image, previewWidth, previewHeight),
                0, previewWidth, 0, 0, previewWidth, previewHeight);
        new Canvas(croppedBitmap).drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recognizer != null) {
            recognizer.close();
        }
    }

    private void renderAdditionalInformation(final Canvas canvas) {
        final Vector<String> lines = new Vector();
        if (recognizer != null) {
            for (String line : recognizer.getStatString().split("\n")) {
                lines.add(line);
            }
        }
        lines.add("Frame: " + previewWidth + "x" + previewHeight);
        lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
        lines.add("Rotation: " + sensorOrientation);
        lines.add("Inference time: " + lastProcessingTimeMs + "ms");
        borderedText.drawLines(canvas, 10, 10, lines);
    }
}
