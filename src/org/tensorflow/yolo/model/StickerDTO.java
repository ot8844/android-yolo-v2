package org.tensorflow.yolo.model;
/**
 * Model to store the data of a bounding box
 *
 * Created by Zoltan Szabo on 12/17/17.
 * URL: https://github.com/szaza/android-yolo-v2
 */
public class StickerDTO {
    private String targetUser;
    private int red;
    private int blue;
    private int green;

    public String getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public StickerDTO() {
    }

    public StickerDTO(String targetUser, int red, int blue, int green) {
        this.targetUser = targetUser;
        this.red = red;
        this.blue = blue;
        this.green = green;
    }
}