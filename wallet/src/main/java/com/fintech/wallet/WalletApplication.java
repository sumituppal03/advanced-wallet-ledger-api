package com.fintech.wallet;

import java.time.ZoneId;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WalletApplication {

    public static void main(String[] args) {

        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        System.out.println("user.timezone = " + System.getProperty("user.timezone"));
        System.out.println("ZoneId = " + ZoneId.systemDefault());
        System.out.println("TimeZone = " + TimeZone.getDefault().getID());

        SpringApplication.run(WalletApplication.class, args);
    }
}