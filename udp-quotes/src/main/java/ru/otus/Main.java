package ru.otus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.otus.service.QuotesReceiverProcess;

@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private QuotesReceiverProcess receiverProcess;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        receiverProcess.start();
    }
}
