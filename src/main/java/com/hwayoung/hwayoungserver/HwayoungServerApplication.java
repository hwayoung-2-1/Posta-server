package com.hwayoung.hwayoungserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

//@SpringBootApplication
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class HwayoungServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HwayoungServerApplication.class, args);
    }

}
