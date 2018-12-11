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

    Video(int port) throws IOException {
        videoServerSocket = new ServerSocket(port);
        videoList = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            while (true) {
                logger.info("Video server socket is listening...");
                Socket videoSocket = videoServerSocket.accept();
                logger.info("A new web video connected");

                VideoHandler videoHandler = new VideoHandler(videoSocket);
                new Thread(videoHandler).start();
            }
        } catch (IOException e) {
            logger.info("Video server socket failed to accept!");
            logger.info(Arrays.toString(e.getStackTrace()));
        }
    }

}

