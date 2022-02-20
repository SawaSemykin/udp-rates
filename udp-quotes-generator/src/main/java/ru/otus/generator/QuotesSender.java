package ru.otus.generator;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.domain.Quote;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;

/**
 * @author Aleksandr Semykin
 */
@RequiredArgsConstructor
public class QuotesSender implements Runnable {
    public final static int BYTES_PER_QUOTE = 60;

    private static final Logger log = LoggerFactory.getLogger(QuotesSender.class);

    private final String destinationHost;
    private final int destinationPort;
    private final DatagramChannel channel;
    private final QuotesGenerator generator;
    private final Gson gson;

    public void send() {
        List<Quote> quotes = generator.generate();
        String quotesJson = gson.toJson(quotes);
        ByteBuffer buf = ByteBuffer.allocate(BYTES_PER_QUOTE * generator.getQuotesCount());
        buf.clear();
        buf.put(quotesJson.getBytes());
        buf.flip();

        int bytesSent = 0;
        try {
            bytesSent = channel.send(buf, new InetSocketAddress(destinationHost, destinationPort));
        } catch (IOException e) {
            log.error("error on sending quotes: {}", e.getMessage(), e);
        }
        log.info("bytes sent: {}", bytesSent);
    }

    @Override
    public void run() {
        send();
    }
}
