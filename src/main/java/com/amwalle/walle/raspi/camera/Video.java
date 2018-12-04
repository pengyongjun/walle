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
import java.util.Arrays;

public class Video implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Video.class);

    private ServerSocket videoServerSocket;

    Video() throws IOException {
        videoServerSocket = new ServerSocket(5555);
    }

    @Override
    public void run() {
        try {
            Socket videoSocket = videoServerSocket.accept();

            // 单独的线程来处理这个连接，主线程回到监听状态
            VideoHandler videoHandler = new VideoHandler(videoSocket);
            videoHandler.run();
        } catch (IOException e) {
            logger.info("Video server socket failed to accept!");
            logger.info(Arrays.toString(e.getStackTrace()));
        }
    }
}

class VideoHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(VideoHandler.class);

    private Socket videoSocket;

    VideoHandler(Socket socket) {
        this.videoSocket = socket;
    }

    @Override
    public void run() {
        // Forward camera video

        // 获取监控摄像头的socket
        Socket cameraSocket = Camera.getCamera(0);
        InputStream cameraStream = null;
        OutputStream videoStream;

        try {
            if (cameraSocket != null) {
                cameraStream = cameraSocket.getInputStream();
            }
            videoStream = videoSocket.getOutputStream();
            DataOutputStream videoDataStream = new DataOutputStream(videoStream);

            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(cameraStream);

            frameGrabber.setFrameRate(100);
            frameGrabber.setFormat("h264");
            frameGrabber.setVideoBitrate(15);
            frameGrabber.setVideoOption("preset", "ultrafast");
            frameGrabber.setNumBuffers(25000000);

            frameGrabber.start();

            videoDataStream.write(("HTTP/1.0 200 OK\r\n" + "Server: YourServerName\r\n" + "Connection: close\r\n" + "Max-Age: 0\r\n" + "Expires: 0\r\n"
                    + "Cache-Control: no-cache, private\r\n" + "Pragma: no-cache\r\n" + "Content-Type: multipart/x-mixed-replace; "
                    + "boundary=--BoundaryString\r\n\r\n").getBytes());

            Frame frame = frameGrabber.grab();

            Java2DFrameConverter converter = new Java2DFrameConverter();

            while (frame != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                BufferedImage bufferedImage = converter.convert(frame);
                ImageIO.write(bufferedImage, "jpg", baos);
                baos.flush();
                baos.close();

                byte[] imageInByte = baos.toByteArray();

                videoDataStream.write(("--BoundaryString" + "\r\n").getBytes());
                videoDataStream.write(("Content-Type: image/jpg" + "\r\n").getBytes());


                videoDataStream.write(("Content-Length: " + imageInByte.length + "\r\n\r\n").getBytes());
                videoDataStream.write(imageInByte);
                videoDataStream.write(("\r\n").getBytes());


                videoDataStream.flush();
                frame = frameGrabber.grab();
            }

        } catch (IOException e) {
            logger.info("Video handle error, exit ...");
            try {
                assert cameraSocket != null;
                cameraSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }
}
