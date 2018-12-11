package com.amwalle.walle.raspi.camera;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

public class CameraHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CameraHandler.class);

    private final String CAMERA_ID;
    private final String CAMERA_NAME;

    private final String LOCK = "LOCK";

    private Socket cameraSocket;
    private static byte[] image;

    CameraHandler(Socket socket) {
        this.cameraSocket = socket;

        // TODO 获取Camera入参
        CAMERA_ID = "1";
        CAMERA_NAME = "home";
    }

    String getCameraId() {
        return CAMERA_ID;
    }

    public String getCameraName() {
        return CAMERA_NAME;
    }

    String getLock() {
        return LOCK;
    }

    @Override
    public void run() {
        try {
            InputStream cameraStream = cameraSocket.getInputStream();
            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(cameraStream);

            frameGrabber.setFrameRate(100);
            frameGrabber.setFormat("h264");
            frameGrabber.setVideoBitrate(15);
            frameGrabber.setVideoOption("preset", "ultrafast");
            frameGrabber.setNumBuffers(25000000);

            frameGrabber.start();

            Frame frame = frameGrabber.grab();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            while (frame != null) {
                BufferedImage bufferedImage = converter.convert(frame);
                ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
                byteArrayOutputStream.flush();

                byte[] imageInByte = byteArrayOutputStream.toByteArray();
                setImage(imageInByte);

                synchronized (LOCK) {
                    LOCK.notifyAll();
                }

                frame = frameGrabber.grab();
            }

            byteArrayOutputStream.close();

        } catch (IOException e) {
            logger.info("Video handle error, exit ...");
            logger.info(e.getMessage());
        }
    }

    private void setImage(byte[] imageInByte) {
        image = imageInByte;
    }

    byte[] getImage() {
        return image;
    }
}
