package com.amwalle.walle;

import com.amwalle.walle.raspi.camera.WebCamera;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class WalleApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(WalleApplication.class, args);
        WebCamera webCamera = new WebCamera();
//        new WebCamera().saveVideoStream();
//        new WebCamera().getVideoStream();
        webCamera.testGetPicFromVideo();
    }
}
