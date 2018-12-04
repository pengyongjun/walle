package com.amwalle.walle.raspi.camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Camera implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Camera.class);

    private ServerSocket cameraServerSocket;
    private static List<Socket> cameraSocketList;

    private Camera() throws IOException {
        cameraServerSocket = new ServerSocket(3333);
        cameraSocketList = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            Socket cameraSocket = cameraServerSocket.accept();
            logger.info("New camera accepted, now there are " + (getCameraCount()+1) + " camera(s) now!");
            cameraSocketList.add(cameraSocket);
        } catch (IOException e) {
            logger.info("Camera server socket failed to accept!");
            logger.info(Arrays.toString(e.getStackTrace()));
        }
    }

    private static int getCameraCount() {
        return cameraSocketList.size();
    }

    static Socket getCamera(int index) {
        if (index > getCameraCount()) {
            logger.info("Error, There's only " + (getCameraCount() + 1) + " camera(s) available! But try to get camera number: " + index + "!");
            return null;
        }

        return cameraSocketList.get(index);
    }
}
