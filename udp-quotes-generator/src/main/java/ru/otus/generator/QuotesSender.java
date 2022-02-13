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

    private static final Logger log = LoggerFactory.getLogger(QuotesSender.class);

    private final DatagramChannel channel;
    private final QuotesGenerator generator;
    private final Gson gson;

    @Override
    public void run() {
        List<Quote> quotes = generator.generate();
        String quotesJson = gson.toJson(quotes);
        ByteBuffer buf = ByteBuffer.allocate(5000);
        buf.clear();
        buf.put(quotesJson.getBytes());
        buf.flip();

        int bytesSent = 0;
        try {
            bytesSent = channel.send(buf, new InetSocketAddress("localhost", 8080));
        } catch (IOException e) {
            log.error("error on sending quotes: {}", e.getMessage(), e);
        }
        log.info("bytes sent: {}", bytesSent);
    }
}
