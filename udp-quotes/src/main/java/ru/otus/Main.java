package ru.otus;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.otus.service.QuotesReceiverProcess;

@SpringBootApplication
@RequiredArgsConstructor
public class Main implements CommandLineRunner {

    private final QuotesReceiverProcess receiverProcess;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        receiverProcess.start();
    }
}
