package ru.otus;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.domain.Quote;
import ru.otus.generator.QuotesSender;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Aleksandr Semykin
 */
@RequiredArgsConstructor
public class QuotesReceiver {
    private static final Logger log = LoggerFactory.getLogger(QuotesReceiver.class);

    private final int destinationPort;
    private final int quotesCount;
    private final Gson gson;

    public static void main(String[] args) throws IOException {
        try (InputStream input = GeneratorApp.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties props = new Properties();
            props.load(input);
            int quotesCount = Integer.parseInt(props.getProperty("quotes.count"));
            int destPort = Integer.parseInt(props.getProperty("quotes.destination-port"));

            new QuotesReceiver(destPort, quotesCount, new Gson()).go();
        }
    }

    private void go() throws IOException {
        try(var channel = DatagramChannel.open()) {
            channel.socket().bind(new InetSocketAddress(destinationPort));

            while (!Thread.currentThread().isInterrupted()) {
                log.info("waiting for quotes");
                ByteBuffer buf = ByteBuffer.allocate(QuotesSender.BYTES_PER_QUOTE * quotesCount);
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
