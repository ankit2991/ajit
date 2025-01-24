package com.bumptech.glide.gifencoder;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class AnimatedGifEncoder {

    private static final double MIN_TRANSPARENT_PERCENTAGE = 4d;
    private final boolean[] usedEntry = new boolean[256];
    private int width;
    private int height;
    private int fixedWidth;
    private int fixedHeight;
    private Integer transparent = null;
    private int transIndex;
    private int repeat = -1;
    private int delay = 0;
    private boolean started = false;
    private OutputStream out;
    private Bitmap image;
    private byte[] pixels;
    private byte[] indexedPixels;
    private int colorDepth;
    private byte[] colorTab;
    private int palSize = 7;

    private int dispose = -1;

    private boolean closeStream = false;

    private boolean firstFrame = true;

    private boolean sizeSet = false;

    private int sample = 10;

    private boolean hasTransparentPixels;

    public void setDelay(int ms) {
        delay = Math.round(ms / 10.0f);
    }

    public void setDispose(int code) {
        if (code >= 0) {
            dispose = code;
        }
    }

    public void setTransparent(int color) {
        transparent = color;
    }

    public boolean addFrame(@Nullable Bitmap im) {
        return addFrame(im, 0, 0);
    }

    public boolean addFrame(@Nullable Bitmap im, int x, int y) {
        if ((im == null) || !started) {
            return false;
        }
        boolean ok = true;
        try {
            if (sizeSet) {
                setFrameSize(fixedWidth, fixedHeight);
            } else {
                setFrameSize(im.getWidth(), im.getHeight());
            }
            image = im;
            getImagePixels();
            analyzePixels();
            if (firstFrame) {
                writeLSD();
                writePalette();
                if (repeat >= 0) {
                    writeNetscapeExt();
                }
            }
            writeGraphicCtrlExt();
            writeImageDesc(x, y);
            if (!firstFrame) {
                writePalette();
            }
            writePixels();
            firstFrame = false;
        } catch (IOException e) {
            ok = false;
        }

        return ok;
    }

    public boolean finish() {
        if (!started)
            return false;
        boolean ok = true;
        started = false;
        try {
            out.write(0x3b);
            out.flush();
            if (closeStream) {
                out.close();
            }
        } catch (IOException e) {
            ok = false;
        }

        transIndex = 0;
        out = null;
        image = null;
        pixels = null;
        indexedPixels = null;
        colorTab = null;
        closeStream = false;
        firstFrame = true;

        return ok;
    }

    public void setSize(int w, int h) {
        if (started) {
            return;
        }

        fixedWidth = w;
        fixedHeight = h;
        if (fixedWidth < 1) {
            fixedWidth = 320;
        }
        if (fixedHeight < 1) {
            fixedHeight = 240;
        }

        sizeSet = true;
    }

    private void setFrameSize(int w, int h) {
        width = w;
        height = h;
    }

    public boolean start(@Nullable OutputStream os) {
        if (os == null)
            return false;
        boolean ok = true;
        closeStream = false;
        out = os;
        try {
            writeString("GIF89a");
        } catch (IOException e) {
            ok = false;
        }
        return started = ok;
    }

    public boolean start(@NonNull String file) {
        boolean ok;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            ok = start(out);
            closeStream = true;
        } catch (IOException e) {
            ok = false;
        }
        return started = ok;
    }

    private void analyzePixels() {
        int len = pixels.length;
        int nPix = len / 3;
        indexedPixels = new byte[nPix];
        NeuQuant nq = new NeuQuant(pixels, len, sample);
        colorTab = nq.process();
        for (int i = 0; i < colorTab.length; i += 3) {
            byte temp = colorTab[i];
            colorTab[i] = colorTab[i + 2];
            colorTab[i + 2] = temp;
            usedEntry[i / 3] = false;
        }
        int k = 0;
        for (int i = 0; i < nPix; i++) {
            int index = nq.map(pixels[k++] & 0xff, pixels[k++] & 0xff, pixels[k++] & 0xff);
            usedEntry[index] = true;
            indexedPixels[i] = (byte) index;
        }
        pixels = null;
        colorDepth = 8;
        palSize = 7;
        if (transparent != null) {
            transIndex = findClosest(transparent);
        } else if (hasTransparentPixels) {
            transIndex = findClosest(Color.TRANSPARENT);
        }
    }

    private int findClosest(int color) {
        if (colorTab == null)
            return -1;
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int minpos = 0;
        int dmin = 256 * 256 * 256;
        int len = colorTab.length;
        for (int i = 0; i < len; ) {
            int dr = r - (colorTab[i++] & 0xff);
            int dg = g - (colorTab[i++] & 0xff);
            int db = b - (colorTab[i] & 0xff);
            int d = dr * dr + dg * dg + db * db;
            int index = i / 3;
            if (usedEntry[index] && (d < dmin)) {
                dmin = d;
                minpos = index;
            }
            i++;
        }
        return minpos;
    }

    private void getImagePixels() {
        int w = image.getWidth();
        int h = image.getHeight();

        if ((w != width) || (h != height)) {
            Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(temp);
            canvas.drawBitmap(temp, 0, 0, null);
            image = temp;
        }
        int[] pixelsInt = new int[w * h];
        image.getPixels(pixelsInt, 0, w, 0, 0, w, h);

        pixels = new byte[pixelsInt.length * 3];

        int pixelsIndex = 0;
        hasTransparentPixels = false;
        int totalTransparentPixels = 0;
        for (final int pixel : pixelsInt) {
            if (pixel == Color.TRANSPARENT) {
                totalTransparentPixels++;
            }
            pixels[pixelsIndex++] = (byte) (pixel & 0xFF);
            pixels[pixelsIndex++] = (byte) ((pixel >> 8) & 0xFF);
            pixels[pixelsIndex++] = (byte) ((pixel >> 16) & 0xFF);
        }

        double transparentPercentage = 100 * totalTransparentPixels / (double) pixelsInt.length;

        hasTransparentPixels = transparentPercentage > MIN_TRANSPARENT_PERCENTAGE;
    }

    private void writeGraphicCtrlExt() throws IOException {
        out.write(0x21);
        out.write(0xf9);
        out.write(4);
        int transp, disp;
        if (transparent == null && !hasTransparentPixels) {
            transp = 0;
            disp = 0;
        } else {
            transp = 1;
            disp = 2;
        }
        if (dispose >= 0) {
            disp = dispose & 7;
        }
        disp <<= 2;

        out.write(0 |
                disp |
                0 |
                transp);

        writeShort(delay);
        out.write(transIndex);
        out.write(0);
    }

    private void writeImageDesc(int x, int y) throws IOException {
        out.write(0x2c);
        writeShort(x);
        writeShort(y);
        writeShort(width);
        writeShort(height);
        if (firstFrame) {
            out.write(0);
        } else {
            out.write(0x80 |
                    0 |
                    0 |
                    0 |
                    palSize);
        }
    }

    private void writeLSD() throws IOException {
        writeShort(width);
        writeShort(height);
        // packed fields
        out.write((0x80 |
                0x70 |
                0x00 |
                palSize));

        out.write(0);
        out.write(0);
    }

    private void writeNetscapeExt() throws IOException {
        out.write(0x21);
        out.write(0xff);
        out.write(11);
        writeString("NETSCAPE" + "2.0");
        out.write(3);
        out.write(1);
        writeShort(repeat);
        out.write(0);
    }

    private void writePalette() throws IOException {
        out.write(colorTab, 0, colorTab.length);
        int n = (3 * 256) - colorTab.length;
        for (int i = 0; i < n; i++) {
            out.write(0);
        }
    }

    private void writePixels() throws IOException {
        LZWEncoder encoder = new LZWEncoder(width, height, indexedPixels, colorDepth);
        encoder.encode(out);
    }

    private void writeShort(int value) throws IOException {
        out.write(value & 0xff);
        out.write((value >> 8) & 0xff);
    }

    private void writeString(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            out.write((byte) s.charAt(i));
        }
    }
}