package com.amwalle.walle.raspi.camera;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

@Component
public class WebCamera {
    private static final Logger logger = LoggerFactory.getLogger(WebCamera.class);

    static final String LOCK = "LOCK";

    public void forwardCameraVideo() throws IOException {
        final String LOCK = "LOCK";
        Camera camera = new Camera(LOCK);
        new Thread(camera).start();

        Video video = new Video(LOCK);
        new Thread(video).start();
    }

    public void forwardVideoStream() throws IOException {
        ServerSocket cameraSS = new ServerSocket(3333);
        ServerSocket viewSS = new ServerSocket(4444);

        logger.info("**************** process start *************");

        while (true) {
            Socket cameraSocket = cameraSS.accept();
            logger.info("*************** camera connected *****************");

            Socket viewSocket = viewSS.accept();
            logger.info("*************** web connected *****************");

            InputStream videoStream = cameraSocket.getInputStream();

            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoStream);

            frameGrabber.setFrameRate(100);
            frameGrabber.setFormat("h264");
            frameGrabber.setVideoBitrate(15);
            frameGrabber.setVideoOption("preset", "ultrafast");
            frameGrabber.setNumBuffers(25000000);

            frameGrabber.start();

            DataOutputStream dataOutputStream = new DataOutputStream(viewSocket.getOutputStream());

            dataOutputStream.write(("HTTP/1.0 200 OK\r\n" + "Server: YourServerName\r\n" + "Connection: close\r\n" + "Max-Age: 0\r\n" + "Expires: 0\r\n"
                    + "Cache-Control: no-cache, private\r\n" + "Pragma: no-cache\r\n" + "Content-Type: multipart/x-mixed-replace; "
                    + "boundary=--BoundaryString\r\n\r\n").getBytes());

            Frame frame = frameGrabber.grab();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            int count = 0;

            while (frame != null) {
                logger.info("************** number: " + count + " ***********");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                BufferedImage bufferedImage = converter.convert(frame);
                ImageIO.write(bufferedImage, "jpg", baos);
                baos.flush();
                baos.close();

                byte[] imageInByte = baos.toByteArray();

                dataOutputStream.write(("--BoundaryString" + "\r\n").getBytes());
                dataOutputStream.write(("Content-Type: image/jpg" + "\r\n").getBytes());


                dataOutputStream.write(("Content-Length: " + imageInByte.length + "\r\n\r\n").getBytes());
                dataOutputStream.write(imageInByte);
                dataOutputStream.write(("\r\n").getBytes());


                dataOutputStream.flush();
                frame = frameGrabber.grab();

                count++;
            }

            dataOutputStream.close();
            viewSocket.close();
        }
    }

    public void testForwardPic() throws IOException {
        ServerSocket viewSS = new ServerSocket(4444);

        while (true) {
            Socket viewSocket = viewSS.accept();

            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(new File(System.getProperty("user.dir") + "/test.h264"));

            frameGrabber.setFrameRate(100);
            frameGrabber.setFormat("h264");
            frameGrabber.setVideoBitrate(15);
            frameGrabber.setVideoOption("preset", "ultrafast");
            frameGrabber.setNumBuffers(25000000);

            frameGrabber.start();

            DataOutputStream dataOutputStream = new DataOutputStream(viewSocket.getOutputStream());

            dataOutputStream.write(("HTTP/1.0 200 OK\r\n" + "Server: YourServerName\r\n" + "Connection: close\r\n" + "Max-Age: 0\r\n" + "Expires: 0\r\n"
                    + "Cache-Control: no-cache, private\r\n" + "Pragma: no-cache\r\n" + "Content-Type: multipart/x-mixed-replace; "
                    + "boundary=--BoundaryString\r\n\r\n").getBytes());

            Frame frame = frameGrabber.grab();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            int count = 0;

            while (frame != null) {

                logger.info(String.valueOf("number: " + count));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                BufferedImage bufferedImage = converter.convert(frame);
                ImageIO.write(bufferedImage, "jpg", baos);
                baos.flush();
                baos.close();

                byte[] imageInByte = baos.toByteArray();

                dataOutputStream.write(("--BoundaryString" + "\r\n").getBytes());
                dataOutputStream.write(("Content-Type: image/jpg" + "\r\n").getBytes());


                dataOutputStream.write(("Content-Length: " + imageInByte.length + "\r\n\r\n").getBytes());
                dataOutputStream.write(imageInByte);
                dataOutputStream.write(("\r\n").getBytes());


                dataOutputStream.flush();
                frame = frameGrabber.grab();

                count++;
            }

            dataOutputStream.close();
            viewSocket.close();
        }
    }

    public void testGetPicFromVideo() throws IOException {
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(new File(System.getProperty("user.dir") + "/test.mp4"));

        frameGrabber.setFrameRate(100);
        frameGrabber.setFormat("h264");
        frameGrabber.setVideoBitrate(15);
        frameGrabber.setVideoOption("preset", "ultrafast");
        frameGrabber.setNumBuffers(25000000);

        Java2DFrameConverter converter = new Java2DFrameConverter();

        frameGrabber.start();
        Frame frame = frameGrabber.grab();

        int count = 0;

        while (frame != null && count < 10) {
            logger.info(String.valueOf(frame.timestamp));

            BufferedImage bufferedImage = converter.convert(frame);

            if (bufferedImage != null) {
                File file = new File(System.getProperty("user.dir") + "/temp/image_" + count + ".jpg");
                ImageIO.write(bufferedImage, "jpg", file);
            }
            count++;

            frame = frameGrabber.grab();
        }
    }

    public void videoStreamToPic() throws IOException {
        ServerSocket cameraSS = new ServerSocket(3333);

        Socket cameraSocket;

        while (true) {
            cameraSocket = cameraSS.accept();

            InputStream cameraStream = cameraSocket.getInputStream();
            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(cameraStream);

            Java2DFrameConverter converter = new Java2DFrameConverter();
            Frame frame = frameGrabber.grab();

            if (frame != null) {
                BufferedImage bufferedImage = converter.convert(frame);
                if (bufferedImage != null) {
                    logger.info(Arrays.toString(bufferedImage.getPropertyNames()));
                    File file = new File(System.getProperty("user.dir") + "image.jpg");
                    ImageIO.write(bufferedImage, "jpg", file);
                }
            }
        }
    }

    public void saveVideoStream() throws IOException {
        ServerSocket cameraSS = new ServerSocket(3333);

        Socket cameraSocket;

        while (true) {
            cameraSocket = cameraSS.accept();

            File file = new File(System.getProperty("user.dir") + "/test.mp4");

            InputStream cameraStream = cameraSocket.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(file);
            int content = cameraStream.read();
            while (content != -1) {
                outputStream.write(content);
                content = cameraStream.read();
                outputStream.flush();
            }
        }
    }

    public void getVideoStream() throws IOException {

        ServerSocket cameraSS = new ServerSocket(3333);
        ServerSocket viewSS = new ServerSocket(4444);

        Socket cameraSocket;
        Socket viewSocket;

        cameraSocket = cameraSS.accept();

        while (true) {
            viewSocket = viewSS.accept();

            logger.info("---------web connected------");

            // 从rasp的视频流中获取一帧
            InputStream cameraIS = cameraSocket.getInputStream();
            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(cameraIS);

            frameGrabber.setFrameRate(100);
            frameGrabber.setFormat("h264");
            frameGrabber.setVideoBitrate(15);
            frameGrabber.setVideoOption("preset", "ultrafast");
            frameGrabber.setNumBuffers(25000000);

            Java2DFrameConverter converter = new Java2DFrameConverter();

            frameGrabber.start();
            Frame frame = frameGrabber.grab();

            OutputStream viewOS = viewSocket.getOutputStream();
            viewOS.write(("HTTP/1.0 200 OK\r\n" + "Server: walle\r\n" + "Connection: close\r\n" + "Max-Age: 0\r\n" + "Expires: 0\r\n"
                    + "Cache-Control: no-cache, private\r\n" + "Pragma: no-cache\r\n" + "Access-Control-Allow-Origin: *\r\n"
                    + "Content-Type: multipart/x-mixed-replace; " + "boundary=--BoundaryString\r\n\r\n").getBytes());


            while (frame != null) {
                BufferedImage image = converter.convert(frame);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                ImageIO.write(image, "jpg", byteArrayOutputStream);
                byteArrayOutputStream.flush();
                byte[] imageInByte = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();

                viewOS.write(("--BoundaryString\r\n" + "Content-type: image/jpg\r\n" + "Content-Length: " + imageInByte.length + "\r\n\r\n").getBytes());
                viewOS.write(("--BoundaryString\r\n").getBytes());

                viewOS.write(imageInByte);

                viewOS.write(("--BoundaryString\r\n").getBytes());
                viewOS.write("\r\n\r\n".getBytes());

                viewOS.flush();

                frame = frameGrabber.grab();
            }
        }
    }
}

