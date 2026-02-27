package org.example.learnlink;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LearnLinkApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USER", dotenv.get("DB_USER"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));


        System.setProperty("AWS_ACCESS_KEY_ID",dotenv.get("AWS_ACCESS_KEY_ID"));
        System.setProperty("AWS_SECRET_ACCESS_KEY",dotenv.get("AWS_SECRET_ACCESS_KEY"));
        System.setProperty("AWS_REGION",dotenv.get("AWS_REGION"));
        System.setProperty("AWS_S3_BUCKET_NAME",dotenv.get("AWS_S3_BUCKET_NAME"));




        SpringApplication.run(LearnLinkApplication.class, args);
    }
}



