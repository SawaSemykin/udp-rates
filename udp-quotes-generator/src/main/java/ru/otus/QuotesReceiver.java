package ru.otus;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.domain.Quote;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Aleksandr Semykin
 */
@RequiredArgsConstructor
public class QuotesReceiver {
    private static final Logger log = LoggerFactory.getLogger(QuotesReceiver.class);

    private final Gson gson;

    public static void main(String[] args) throws IOException {
        new QuotesReceiver(new Gson()).go();
    }

    private void go() throws IOException {
        try(var channel = DatagramChannel.open()) {
            channel.socket().bind(new InetSocketAddress(8080));

            while (!Thread.currentThread().isInterrupted()) {
                log.info("waiting for quotes");
                ByteBuffer buf = ByteBuffer.allocate(5000);
                buf.clear();

                channel.receive(buf);

                var quotes = extractData(buf);
                log.info("received quotes count: {}", quotes.size());
                log.info("received quotes (first 10): {}", quotes.stream().limit(10).collect(Collectors.toList()));
            }
        }
    }

    private List<Quote> extractData(ByteBuffer buf) {
        buf.flip();
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        return gson.fromJson(new String(bytes), new TypeToken<ArrayList<Quote>>() {}.getType());
    }
}
