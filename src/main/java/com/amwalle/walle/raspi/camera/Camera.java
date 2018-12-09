package com.amwalle.walle.raspi.camera;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Camera implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Camera.class);

    private final String LOCK;

    private ServerSocket cameraServerSocket;
    private static int cameraCount = 0;

    Camera(String lock) throws IOException {
        LOCK = lock;
        cameraServerSocket = new ServerSocket(3333);
    }

    @Override
    public void run() {
        try {
            while (true) {
                logger.info("Camera server socket is listening...");

                Socket cameraSocket = cameraServerSocket.accept();
                cameraCount++;

                logger.info("New camera accepted, now there are " + cameraCount + " camera(s)!");

                CameraHandler cameraHandler = new CameraHandler(LOCK, cameraSocket);
                new Thread(cameraHandler).start();
            }
        } catch (IOException e) {
            logger.info("Camera server socket failed to accept!");
            logger.info(Arrays.toString(e.getStackTrace()));
        }
    }

    public static int getCameraCount() {
        return cameraCount;
    }
}

class CameraHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CameraHandler.class);

    private Socket cameraSocket;
    private static byte[] image;

    CameraHandler(String lock, Socket socket) {
        String LOCK = lock;
        this.cameraSocket = socket;
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

            while (frame != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                BufferedImage bufferedImage = converter.convert(frame);
                ImageIO.write(bufferedImage, "jpg", baos);
                baos.flush();
                baos.close();

                byte[] imageInByte = baos.toByteArray();
                setImage(imageInByte);

                synchronized (WebCamera.LOCK) {
                    WebCamera.LOCK.notifyAll();
                }

                frame = frameGrabber.grab();
            }

        } catch (IOException e) {
            logger.info("Video handle error, exit ...");
            logger.info(e.getMessage());
        }
    }

    private static void setImage(byte[] imageInByte) {
        image = imageInByte;
    }

    static byte[] getImage() {
        return image;
    }

    private void forwardVideo(byte[] image) {
        if (Video.getVideoList().isEmpty()) {
            return;
        }

        for (OutputStream videoStream : Video.getVideoList()) {
            DataOutputStream videoDataStream = new DataOutputStream(videoStream);

            try {
                videoDataStream.write(("--BoundaryString" + "\r\n").getBytes());
                videoDataStream.write(("Content-Type: image/jpg" + "\r\n").getBytes());


                videoDataStream.write(("Content-Length: " + image.length + "\r\n\r\n").getBytes());
                videoDataStream.write(image);
                videoDataStream.write(("\r\n").getBytes());

                videoDataStream.flush();
            } catch (IOException e) {
                logger.info("Send image to video failed");
                Video.getVideoList().remove(videoStream);
                logger.info(e.getMessage());
            }

        }
    }
}
