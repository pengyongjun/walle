package com.amwalle.walle;

import com.amwalle.walle.raspi.camera.WebCamera;
import com.amwalle.walle.scheduler.JobManager;
import org.quartz.SchedulerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class WalleApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(WalleApplication.class, args);
        WebCamera webCamera = new WebCamera();

        webCamera.forwardCameraVideo();
//        webCamera.testForwardPic();

        try {
            JobManager jobManager = new JobManager();
            jobManager.test();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
