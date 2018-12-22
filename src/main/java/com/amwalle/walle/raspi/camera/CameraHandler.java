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
import java.nio.Buffer;

public class CameraHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CameraHandler.class);

    private final String CAMERA_ID;
    private final String CAMERA_NAME;

    private final String LOCK = "LOCK";

    private Socket cameraSocket;
    private static BufferedImage bufferedImage;

    CameraHandler(Socket socket) throws IOException {
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

//            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(cameraStream))) {
//                String line = bufferedReader.readLine();
//
//                while (null != line && !line.equals("")) {
//                    logger.info(line);
//                    line = bufferedReader.readLine();
//                }
//            }

            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(cameraStream);

            frameGrabber.setFrameRate(30);
            frameGrabber.setFormat("h264");
            frameGrabber.setVideoBitrate(15);
            frameGrabber.setVideoOption("preset", "ultrafast");
            frameGrabber.setNumBuffers(25000000);

            frameGrabber.start();

            Frame frame = frameGrabber.grab();

            Java2DFrameConverter converter = new Java2DFrameConverter();

            while (frame != null) {
                BufferedImage bufferedImage = converter.convert(frame);

                setBufferedImage(bufferedImage);

                synchronized (LOCK) {
                    LOCK.notifyAll();
                }

                frame = frameGrabber.grab();
            }
        } catch (IOException e) {
            logger.info("Video handle error, exit ...");
            logger.info(e.getMessage());
        }
    }

    private void setBufferedImage(BufferedImage image) {
        bufferedImage = image;
    }

    BufferedImage getBufferedImage() {
        return bufferedImage;
    }
}
