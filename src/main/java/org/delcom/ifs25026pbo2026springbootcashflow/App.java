package org.delcom.ifs25026pbo2026springbootcashflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

    public static void main(String[] args) throws Exception {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }
}
