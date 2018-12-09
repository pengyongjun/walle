package com.amwalle.walle.raspi.camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Video implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Video.class);

    private ServerSocket videoServerSocket;
    private static List<OutputStream> videoList;
    private final String LOCK;

    Video(String lock) throws IOException {
        LOCK = lock;
        videoServerSocket = new ServerSocket(5555);
        videoList = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            while (true) {
                logger.info("Video server socket is listening...");
                Socket videoSocket = videoServerSocket.accept();
                logger.info("A new web video connected");

                VideoHandler videoHandler = new VideoHandler(LOCK, videoSocket);
                new Thread(videoHandler).start();
            }
        } catch (IOException e) {
            logger.info("Video server socket failed to accept!");
            logger.info(Arrays.toString(e.getStackTrace()));
        }
    }

    static List<OutputStream> getVideoList() {
        return videoList;
    }
}

class VideoHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(VideoHandler.class);

    private final String LOCK;
    private OutputStream videoStream;

    VideoHandler(String lock, Socket socket) throws IOException {
        LOCK = lock;

        // 发送响应报文头
        videoStream = socket.getOutputStream();
        responseVideoHeader(videoStream);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                synchronized (WebCamera.LOCK) {
                    sendImage(CameraHandler.getImage());
                    WebCamera.LOCK.wait();
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void responseVideoHeader(OutputStream videoStream) {
        DataOutputStream videoDataStream = new DataOutputStream(videoStream);
        try {
            videoDataStream.write(("HTTP/1.0 200 OK\r\n" + "Server: YourServerName\r\n" + "Connection: close\r\n" + "Max-Age: 0\r\n" + "Expires: 0\r\n"
                    + "Cache-Control: no-cache, private\r\n" + "Pragma: no-cache\r\n" + "Content-Type: multipart/x-mixed-replace; "
                    + "boundary=--BoundaryString\r\n\r\n").getBytes());
            videoDataStream.flush();

            logger.info("Write video response header success");
        } catch (IOException e) {
            logger.info("Write video response header failed!");
            logger.info(e.getMessage());
        }
    }

    private void sendImage(byte[] image) throws IOException {
        DataOutputStream videoDataStream = new DataOutputStream(videoStream);

        videoDataStream.write(("--BoundaryString" + "\r\n").getBytes());
        videoDataStream.write(("Content-Type: image/jpg" + "\r\n").getBytes());


        videoDataStream.write(("Content-Length: " + image.length + "\r\n\r\n").getBytes());
        videoDataStream.write(image);
        videoDataStream.write(("\r\n").getBytes());

        videoDataStream.flush();
    }
}

