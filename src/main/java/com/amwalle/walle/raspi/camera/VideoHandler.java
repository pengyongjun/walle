package com.amwalle.walle.raspi.camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(VideoHandler.class);

    private CameraHandler cameraHandler;
    private OutputStream videoStream;

    VideoHandler(Socket socket) throws IOException {
        // test
        InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        String line = bufferedReader.readLine();
        while (null != line && !line.equals("")) {
            logger.info(line);
            line = bufferedReader.readLine();
        }

        // TODO 获取CameraHandler
        cameraHandler = Camera.getCameraByIndex(0);

        // 发送响应报文头
        videoStream = socket.getOutputStream();
        sendVideoHeader(videoStream);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                synchronized (cameraHandler.getLock()) {
                    if (null == cameraHandler.getImage()) {
                        cameraHandler.getLock().wait();
                    }
                    sendImage(cameraHandler.getImage());
                    cameraHandler.getLock().wait();
                }
            } catch (InterruptedException | IOException e) {
                logger.info(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    private void sendVideoHeader(OutputStream videoStream) {
        DataOutputStream videoDataStream = new DataOutputStream(videoStream);
        try {
            videoDataStream.write(("HTTP/1.0 200 OK\r\n" + "Server: walle\r\n" + "Connection: close\r\n" + "Max-Age: 0\r\n" + "Expires: 0\r\n"
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

    static byte[] addWaterMark(BufferedImage bufferedImage) throws IOException {
        Graphics2D graphics = bufferedImage.createGraphics();

        // 时间戳
        graphics.setFont(new Font("Arial", Font.ITALIC, 14));
        graphics.setColor(Color.white);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.8f));
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        graphics.drawString(time, 50, 20);

        // logo
        ImageIcon logoImgIcon = new ImageIcon(ImageIO.read(new File(System.getProperty("user.dir") + "/temp/logo.png")));
        Image logoImg = logoImgIcon.getImage();
        graphics.drawImage(logoImg, 15,20,30,30,null);

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        graphics.dispose();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }
}
