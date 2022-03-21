package ru.otus.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.otus.domain.Quote;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;

/**
 * @author Aleksandr Semykin
 */
@Component
@Slf4j
public class QuotesReceiverProcessImpl implements QuotesReceiverProcess {
    private final static int BYTES_PER_QUOTE = 60;

    private final Gson gson;
    private final QuotesHandler quotesHandler;
    private final QuotesReceiverProps props;

    public QuotesReceiverProcessImpl(Gson gson, QuotesHandler quotesHandler, QuotesReceiverProps props) {
        this.gson = gson;
        this.quotesHandler = quotesHandler;
        this.props = props;

        init();
    }

    @Override
    public void start() throws IOException {
        try (var channel = DatagramChannel.open()) {
            channel.socket().bind(new InetSocketAddress(props.getUdpGeneratorPort()));
            log.info("receiver started. Waiting for quotes");

            while (!Thread.currentThread().isInterrupted()) {
                ByteBuffer buf = ByteBuffer.allocate(BYTES_PER_QUOTE * props.getCount());
                buf.clear();

                channel.receive(buf);

                var newQuotes = receiveQuotes(buf);
                quotesHandler.handle(newQuotes);
            }
        }
    }

    @Override
    @PreDestroy
    public void stop() {
        log.info("receiver stopped");
    }

    private void init() {
        var baseTemplate = "RU0000000000";
        for (int i = 0; i < props.getIsinsCount(); i++) {
            var tail = String.valueOf(i);
            var template = baseTemplate.substring(0, baseTemplate.length() - tail.length());
            var isin = template + tail;
            quotesHandler.handle(Collections.singletonList(new Quote(isin, BigDecimal.valueOf(100), BigDecimal.valueOf(100))));
        }
    }

    private List<Quote> receiveQuotes(ByteBuffer buf) {
        buf.flip();
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        return gson.fromJson(new String(bytes), new TypeToken<ArrayList<Quote>>() {}.getType());
    }
}
