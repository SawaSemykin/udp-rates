package ru.otus.service;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import ru.otus.domain.Elvl;
import ru.otus.domain.Quote;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author Aleksandr Semykin
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class QuotesHandlerImpl implements QuotesHandler {

    private final SimpMessageSendingOperations messagingTemplate;

    private final Map<String, BigDecimal> elvls = new ConcurrentHashMap<>();

    private final ExecutorService elvlExecutor;

    @Override
    @Scheduled(fixedDelay=1000)
    public void pushElvls() {
        var map = new HashMap<String, Object>();
        map.put(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);

        for (var elvl : getElvls()) {
            log.debug("Sending new quote");
            this.messagingTemplate.convertAndSend("/topic/elvls", elvl, map);
        }
    }

    @Override
    public Collection<Elvl> getElvls() {
        return elvls.entrySet().stream()
                .map(entry -> new Elvl(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public void handle(List<Quote> quotes) {
        List<List<Quote>> quoteLists = Lists.partition(quotes, QuotesHandler.POOL_SIZE);
        for (List<Quote> quoteList : quoteLists) {
            elvlExecutor.submit(() -> handleTask(quoteList));
        }
    }

//    @Async("elvlExecutor") // не работает. Возможно из-за прокси: этот метод вызывается из-за этого же компонента
    private void handleTask(List<Quote> quotes) {
        for (var quote : quotes) {
            BigDecimal elvl = calcElvl(quote);
            elvls.merge(quote.getIsin(), elvl, (oldValue, newElvl) -> newElvl);
            log.debug("Isin {} handled. New elvl: {}", quote.getIsin(), elvl);
        }
    }

    private BigDecimal calcElvl(Quote quote) {
        BigDecimal oldElvl = elvls.getOrDefault(quote.getIsin(), UNHANDLED_ELVL_VALUE);
        if (oldElvl.compareTo(UNHANDLED_ELVL_VALUE) == 0) {
            return quote.getBid() != null ? quote.getBid() : quote.getAsk();
        }
        if (quote.getBid() != null && quote.getBid().compareTo(oldElvl) >= 0) {
            return quote.getBid();
        }
        if (quote.getAsk() != null && quote.getAsk().compareTo(oldElvl) <= 0) {
            return quote.getAsk();
        }
        log.warn("Couldn't calc elvl. OldElvl: {}, Quote: {}", oldElvl, quote);
        return UNHANDLED_ELVL_VALUE;
    }
}
