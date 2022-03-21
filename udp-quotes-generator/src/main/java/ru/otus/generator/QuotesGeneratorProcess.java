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

    private final String destinationHost;
    private final int destinationPort;
    private final int sendingRateInMillis;
    private final QuotesGenerator quotesGenerator;
    private final ScheduledExecutorService quotesGenExecutor = Executors.newSingleThreadScheduledExecutor();
    private DatagramChannel channel;

    public QuotesGeneratorProcess(String destinationHost, int destinationPort, int sendingRateInMillis, QuotesGenerator quotesGenerator) {
        this.destinationHost = destinationHost;
        this.destinationPort = destinationPort;
        this.sendingRateInMillis = sendingRateInMillis;
        this.quotesGenerator = quotesGenerator;

        var shutdownHook = new Thread(this::stop);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    @Override
    public void start() {
        openChannel();
        QuotesSender sender = new QuotesSender(destinationHost, destinationPort, channel, quotesGenerator, new Gson());
        quotesGenExecutor.scheduleAtFixedRate(
                sender,
                0,
                sendingRateInMillis,
                TimeUnit.MILLISECONDS);
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

        closeChannel();
        log.info("server stopped");
    }

    private void openChannel() {
        try {
            channel = DatagramChannel.open();
        } catch (IOException e) {
            log.error("Error while opening datagram channel");
            throw new RuntimeException(e);
        }
    }

    private void closeChannel() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                log.error("Error while closing datagram channel");
                throw new RuntimeException(e);
            }
        }
    }
}
