package ru.otus.generator;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.*;

/**
 * @author Aleksandr Semykin
 */
public class QuotesGeneratorProcess implements Process {

    private static final Logger log = LoggerFactory.getLogger(QuotesGeneratorProcess.class);

    private final int sendingRateInMillis;
    private final QuotesGenerator quotesGenerator;
    private final ScheduledExecutorService quotesGenExecutor = Executors.newSingleThreadScheduledExecutor();

    public QuotesGeneratorProcess(int sendingRateInMillis, QuotesGenerator quotesGenerator) {
        this.sendingRateInMillis = sendingRateInMillis;
        this.quotesGenerator = quotesGenerator;

        var shutdownHook = new Thread(this::stop);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    @Override
    public void start() {
        try (var channel = DatagramChannel.open()) {
            QuotesSender sender = new QuotesSender(channel, quotesGenerator, new Gson());
            ScheduledFuture<?> future = quotesGenExecutor.scheduleAtFixedRate(
                    sender,
                    0,
                    sendingRateInMillis,
                    TimeUnit.MILLISECONDS);

            try {
                future.get();
            }  catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Quotes sending was interrupted", e);
            } catch (ExecutionException e) {
                log.error("Quotes sending error", e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        quotesGenExecutor.shutdown();
        boolean terminated = false;
        try {
            terminated = quotesGenExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Termination awaiting was interrupted", e);
        }
        if (terminated) {
            log.info("All quotes have been sent");
        } else {
            log.warn("Not all quotes have been sent");
        }
        log.info("server stopped");
    }
}
