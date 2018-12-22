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
    private static List<CameraHandler> cameraList;

    Camera(int port) throws IOException {
        cameraServerSocket = new ServerSocket(port);
        cameraList = new ArrayList<>();
    }

    public static CameraHandler getCameraById(String id) {
        for (CameraHandler cameraHandler : cameraList
        ) {
            if (id.equals(cameraHandler.getCameraId())) {
                return cameraHandler;
            }
        }

        return null;
    }

    public static CameraHandler getCameraByName(String name) {
        for (CameraHandler cameraHandler : cameraList
        ) {
            if (name.equals(cameraHandler.getCameraId())) {
                return cameraHandler;
            }
        }

        return null;
    }

    static CameraHandler getCameraByIndex(int index) {
        if (index >= cameraList.size() || index < 0) {
            return null;
        }

        return cameraList.get(index);
    }

    @Override
    public void run() {
        try {
            while (true) {
                logger.info("Camera server socket is listening...");

                Socket cameraSocket = cameraServerSocket.accept();

                logger.info("New camera accepted, now there are " + (cameraList.size() + 1) + " camera(s)!");

                CameraHandler cameraHandler = new CameraHandler(cameraSocket);
                cameraList.add(cameraHandler);

                new Thread(cameraHandler).start();
            }
        } catch (IOException e) {
            logger.info("Camera server socket failed to accept!");
            logger.info(Arrays.toString(e.getStackTrace()));
        }
    }

    public static int getCameraCount() {
        return cameraList.size();
    }
}

