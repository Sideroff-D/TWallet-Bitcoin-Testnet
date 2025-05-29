package com.siderov.btctracker.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Генерира PNG изображение на QR кода за даден низ.
 */
public class QRCodeGenerator {

    /**
     * Генерира QR код изображение с размер width×height за подадения текст.
     *
     * @param text   Строката, която да кодира в QR
     * @param width  Ширина на изображението
     * @param height Височина на изображението
     * @return PNG байтов масив
     * @throws WriterException при грешка в кода на QR
     * @throws IOException      при проблем с писането на PNG
     */
    public static byte[] generateQRCodeImage(String text, int width, int height)
            throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                img.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", out);
        return out.toByteArray();
    }
}
