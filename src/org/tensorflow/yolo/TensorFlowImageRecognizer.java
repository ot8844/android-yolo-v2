package org.tensorflow.yolo;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import android.util.Pair;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.yolo.model.BoxPosition;
import org.tensorflow.yolo.model.Recognition;
import org.tensorflow.yolo.model.StickerDTO;
import org.tensorflow.yolo.util.ClassAttrProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static org.tensorflow.yolo.Config.IMAGE_MEAN;
import static org.tensorflow.yolo.Config.IMAGE_STD;
import static org.tensorflow.yolo.Config.INPUT_NAME;
import static org.tensorflow.yolo.Config.INPUT_SIZE;
import static org.tensorflow.yolo.Config.MODEL_FILE;
import static org.tensorflow.yolo.Config.OUTPUT_NAME;

/**
 * A classifier specialized to label images using TensorFlow.
 * Modified by Zoltan Szabo
 */
public class TensorFlowImageRecognizer {
    private int outputSize;
    private Vector<String> labels;
    private TensorFlowInferenceInterface inferenceInterface;

    private TensorFlowImageRecognizer() {
    }

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager The asset manager to be used to load assets.
     * @throws IOException
     */
    public static TensorFlowImageRecognizer create(AssetManager assetManager) {
        TensorFlowImageRecognizer recognizer = new TensorFlowImageRecognizer();
        recognizer.labels = ClassAttrProvider.newInstance(assetManager).getLabels();
        recognizer.inferenceInterface = new TensorFlowInferenceInterface(assetManager,
                "file:///android_asset/" + MODEL_FILE);
        recognizer.outputSize = YOLOClassifier.getInstance()
                .getOutputSizeByShape(recognizer.inferenceInterface.graphOperation(OUTPUT_NAME));
        return recognizer;
    }

    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        return YOLOClassifier.getInstance().classifyImage(runTensorFlow(bitmap), labels);
    }

    public String getStatString() {
        return inferenceInterface.getStatString();
    }

    public void close() {
        inferenceInterface.close();
    }

    private float[] runTensorFlow(final Bitmap bitmap) {
        final float[] tfOutput = new float[outputSize];
        // Copy the input data into TensorFlow.
        inferenceInterface.feed(INPUT_NAME, processBitmap(bitmap), 1, INPUT_SIZE, INPUT_SIZE, 3);

        // Run the inference call.
        inferenceInterface.run(new String[]{OUTPUT_NAME});

        // Copy the output Tensor back into the output array.
        inferenceInterface.fetch(OUTPUT_NAME, tfOutput);

        return tfOutput;
    }

    /**
     * Preprocess the image data from 0-255 int to normalized float based
     * on the provided parameters.
     *
     * @param bitmap
     */
    private float[] processBitmap(final Bitmap bitmap) {
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        float[] floatValues = new float[INPUT_SIZE * INPUT_SIZE * 3];

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3 + 0] = (((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
            floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
            floatValues[i * 3 + 2] = ((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
        }
        return floatValues;
    }

    private static int dist_sq(int red, int blue, int green,
                               int target_red, int target_blue, int target_green
    ) {
        int dist = (int) ((red-target_red)*(red-target_red) +
                (blue-target_blue)*(blue-target_blue) +
                (green-target_green)*(green-target_green));
        return dist;
    }

    private static boolean pixel_condition(int red, int blue, int green,
                                           int target_red, int target_blue, int target_green
    ){
        int dist = (int) ((red-target_red)*(red-target_red) +
                (blue-target_blue)*(blue-target_blue) +
                (green-target_green)*(green-target_green));
        return dist < 25000;
    }

    private static boolean scan(Bitmap origin, int x, int y, StickerDTO target_sticker) {
        int scan_box_size = 10;
        double scan_rate = 0.8;
        int num_min = (int)(scan_box_size * scan_box_size * scan_rate);
        int width = origin.getWidth();
        int height = origin.getHeight();
        int cnt = 0;
        for (int i=-scan_box_size/2; i<scan_box_size/2; i++){
            for (int j=-scan_box_size/2; j<scan_box_size/2; j++){
                int px = i+x;
                int py = j+y;
                if (px < 0 || px >= width || py < 0 || py >= height) {
                    continue;
                }
                int col = origin.getPixel(px, py);
                int alpha = col & 0xFF000000;
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                if (pixel_condition(red, green, blue,
                        target_sticker.getRed(),
                        target_sticker.getBlue(),
                        target_sticker.getGreen())){
                    cnt += 1;
                }
            }
        }
        if (x%100 == 0 && y%100 == 0) {
            Log.d("jack_debug", "scan: " + cnt + ",x: " + x + ",y: " + y);
        }
        return cnt >= num_min;
    }

    private static double scan_dist(Bitmap origin, RectF box, StickerDTO target_sticker) {
        int scan_box_size = 10;
        double scan_rate = 0.8;
        int num_min = (int)(scan_box_size * scan_box_size * scan_rate);
        int width = origin.getWidth();
        int height = origin.getHeight();
        int sum = 0;
        for (int i=(int)box.left/2; i<(int)box.right/2; i++){
            for (int j=(int)box.top/2; j<(int)box.bottom/2; j++){
                int px = i;
                int py = j;
                if (px < 0 || px >= width || py < 0 || py >= height) {
                    continue;
                }
                int col = origin.getPixel(px, py);
                int alpha = col & 0xFF000000;
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                sum += dist_sq(red, green, blue,
                        target_sticker.getRed(),
                        target_sticker.getBlue(),
                        target_sticker.getGreen());
            }
        }
        return sum * 1.0 / (scan_box_size*scan_box_size);
    }
//
//    public static List<Recognition> find(Bitmap origin, List<StickerDTO> target_sticker){
//        String tag = "jack";
//        int width = origin.getWidth();
//        int height = origin.getHeight();
//        int cnt = 0;
//        int cnt_2 = 0;
//        int x = 0;
//        int y = 0;
//        ArrayList<ArrayList<Pair<Integer, Integer>>> true_positions_per_sticker =
//                new ArrayList<ArrayList<Pair<Integer, Integer>>>();
//        for (StickerDTO sticker: target_sticker) {
//            true_positions_per_sticker.add(new ArrayList<Pair<Integer, Integer>>());
//        }
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                int col = origin.getPixel(i, j);
//                int alpha = col & 0xFF000000;
//                int red = (col & 0x00FF0000) >> 16;
//                int green = (col & 0x0000FF00) >> 8;
//                int blue = (col & 0x000000FF);
//                if (i == height/2 && j == width/2){
//                    Log.d(tag, "find: " + red + ","+green+","+blue);
//                }
//                int dist = (int) ((red - 255)*(red - 255) + (blue-0)*(blue-0) + (green-0)*(green-0));
//                if (dist < 5000){
////                    Log.d(tag, "R: " + red + " G: " + green + " B: " + blue);
////                    Log.d(tag, "dist : " + dist);
////                    Log.d(tag, "X: " + i + "Y: " + j);
//                    cnt +=1;
//                    x += i;
//                    y += j;
//                }
//                if (red > 210 && blue < 40 && green < 40) {
//                    cnt_2 +=1;
//                }
//                for (int k=0; k<target_sticker.size(); k++) {
//                    if (scan(origin, i, j, target_sticker.get(k))){
//                        true_positions_per_sticker.get(k).add(new Pair<Integer, Integer>(i, j));
//                    }
//                }
//            }
//        }
//        Log.d(tag, "cnt: " + cnt);
//        Log.d(tag, "cnt2: " + cnt_2);
//        ArrayList<Recognition> res = new ArrayList<Recognition>();
//        if (cnt > 0) {
//            x = x / cnt;
//            y = y / cnt;
//            //res.add(new Recognition(1, "red", 1.0f, new BoxPosition(x - 1, y - 1, 2, 2)));
//        }
//        for (int k=0; k<target_sticker.size(); k++) {
//            ArrayList<Pair<Integer, Integer>> pairs = true_positions_per_sticker.get(k);
//            StickerDTO sticker = target_sticker.get(k);
//            int xx = 0;
//            int yy = 0;
//            for (Pair<Integer, Integer> pair : pairs) {
//                xx += pair.first;
//                yy += pair.second;
//            }
//            if (pairs.size() > 0) {
//                xx = xx / pairs.size();
//                yy = yy / pairs.size();
//                res.add(new Recognition(sticker.id,
//                        sticker.sticker, 1.0f,
//                        new BoxPosition(xx - 1, yy - 1, 2, 2)));
//            }
//        }
//        return res;
//    }

    public String fix(Bitmap origin, HashMap<String, StickerDTO> target_sticker, RectF box) {
        Pair<Double, String> true_position = new Pair<>(null, null);
        for (Map.Entry<String, StickerDTO> entry : target_sticker.entrySet()) {
            String name = entry.getKey();
            StickerDTO dto = entry.getValue();
            double get_dist = scan_dist(origin, box, dto);
            if (true_position.first == null || true_position.first >= get_dist) {
                true_position = new Pair<>(get_dist, name);
            }
        }
        return true_position.second;
    }

    public double[] fix_pattern(Bitmap origin, RectF box) {
        double[] rgb = new double[3];
        rgb[0]=0;
        rgb[1]=0;
        rgb[2]=0;
        int cnt = 0;
        int width = origin.getWidth();
        int height = origin.getHeight();

        for (int i=(int)box.left/2; i<(int)box.right/2; i++){
            for (int j=(int)box.top/2; j<(int)box.bottom/2; j++){
                if (i < 0 || i >= width || j < 0 || j >= height) {
                    continue;
                }
                int col = origin.getPixel(i, j);
                int alpha = col & 0xFF000000;
                rgb[0] += (col & 0x00FF0000) >> 16;
                rgb[1] += (col & 0x0000FF00) >> 8;
                rgb[2] += (col & 0x000000FF);
                cnt += 1;
            }
        }

        if (cnt > 0) {
            rgb[0] /= cnt;
            rgb[1] /= cnt;
            rgb[2] /= cnt;
        }
        return rgb;
    }

//    public void draw_magic(Bitmap origin) {
//        int width = origin.getWidth();
//        int height = origin.getHeight();
//
//        for (int i=0; i<width; i++){
//            for(int j=0; j<height; j++) {
//                int col = origin.getPixel(i, j);
//                int alpha = col & 0xFF000000;
//                int red = (col & 0x00FF0000) >> 16;
//                int blue = (col & 0x0000FF00) >> 8;
//                int green = (col & 0x000000FF);
//
//                if red / blue == 0
//            }
//        }
//    }
}
