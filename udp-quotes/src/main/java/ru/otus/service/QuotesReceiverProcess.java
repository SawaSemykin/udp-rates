package ru.otus.service;

import java.io.IOException;

public interface QuotesReceiverProcess {
    void start() throws IOException;
    void stop();
}
