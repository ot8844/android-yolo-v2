package org.tensorflow.yolo.model;
/**
 * Model to store the data of a bounding box
 *
 * Created by Zoltan Szabo on 12/17/17.
 * URL: https://github.com/szaza/android-yolo-v2
 */
public class Sticker {
    public int id;
    public String sticker;
    public int red;
    public int blue;
    public int green;
    public Sticker(int id, String sticker, int red, int blue, int green) {
        this.red = red;
        this.blue = blue;
        this.green = green;
        this.sticker = sticker;
        this.id = id;
    }
}