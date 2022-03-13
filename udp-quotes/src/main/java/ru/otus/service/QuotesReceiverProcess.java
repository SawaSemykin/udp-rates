package ru.otus.service;

import ru.otus.domain.Quote;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface QuotesReceiverProcess {
    void init();
    void start() throws IOException;
    void stop();
}
