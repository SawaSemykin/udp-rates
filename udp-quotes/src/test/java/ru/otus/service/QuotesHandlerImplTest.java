package ru.otus.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import ru.otus.domain.Quote;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class QuotesHandlerImplTest {

    private static final Logger log = LoggerFactory.getLogger(QuotesHandlerImplTest.class);

    private final String ISIN = "RU1000000000";

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    private ExecutorService elvlExecutor;

    private QuotesHandler quotesHandler;

    @BeforeEach
    public void setUp() {
        elvlExecutor = Executors.newSingleThreadExecutor();
        quotesHandler = new QuotesHandlerImpl(messagingTemplate, elvlExecutor);
    }

    @DisplayName("Если bid > elvl, то elvl = bid")
    @Test
    void shouldReturnQuoteBidAsElvlOnExistingElvl() {
        quotesHandler.handle(Collections.singletonList(new Quote(ISIN, BigDecimal.valueOf(100), BigDecimal.valueOf(100))));
        BigDecimal bid = BigDecimal.valueOf(150);
        quotesHandler.handle(Collections.singletonList(new Quote(ISIN, bid, BigDecimal.valueOf(200))));

        waitForHandlingQuotesToFinish();

        assertThat(quotesHandler.getElvls()).allMatch(actualElvl -> actualElvl.getValue().compareTo(bid) == 0);
    }

    @DisplayName("Если ask < elvl, то elvl = ask")
    @Test
    void shouldReturnQuoteAskAsElvlOnExistingElvl() {
        quotesHandler.handle(Collections.singletonList(new Quote(ISIN, BigDecimal.valueOf(100), BigDecimal.valueOf(100))));
        BigDecimal ask = BigDecimal.valueOf(95);
        quotesHandler.handle(Collections.singletonList(new Quote(ISIN, BigDecimal.valueOf(90), ask)));

        waitForHandlingQuotesToFinish();

        assertThat(quotesHandler.getElvls()).allMatch(actualElvl -> actualElvl.getValue().compareTo(ask) == 0);
    }

    @DisplayName("Если значение elvl для данной бумаги отсутствует, то elvl = bid")
    @Test
    void shouldReturnQuoteBidAsElvlOnNonExistingElvl() {
        BigDecimal bid = BigDecimal.valueOf(100);
        quotesHandler.handle(Collections.singletonList(new Quote(ISIN, bid, BigDecimal.valueOf(100))));

        waitForHandlingQuotesToFinish();

        assertThat(quotesHandler.getElvls()).allMatch(actualElvl -> actualElvl.getValue().compareTo(bid) == 0);
    }

    @DisplayName("Если значение elvl для данной бумаги отсутствует и bid у котировки тоже отсутствует, то elvl = ask")
    @Test
    void shouldReturnQuoteAskAsElvlOnNonExistingBothQuoteBidAndElvl() {
        BigDecimal ask =  BigDecimal.valueOf(200);
        quotesHandler.handle(Collections.singletonList(new Quote(ISIN, null, ask)));

        waitForHandlingQuotesToFinish();

        assertThat(quotesHandler.getElvls()).allMatch(actualElvl -> actualElvl.getValue().compareTo(ask) == 0);
    }

    @DisplayName("Если bid < elvl и ask > elvl, то в требованиях не описано как вычислять elvl")
    @Test
    void shouldReturnUnhandledElvl() {
        quotesHandler.handle(Collections.singletonList(new Quote(ISIN, BigDecimal.valueOf(100), BigDecimal.valueOf(100))));
        quotesHandler.handle(Collections.singletonList(new Quote(ISIN, BigDecimal.valueOf(95), BigDecimal.valueOf(105))));

        waitForHandlingQuotesToFinish();

        assertThat(quotesHandler.getElvls()).allMatch(actualElvl -> actualElvl.getValue().compareTo(QuotesHandler.UNHANDLED_ELVL_VALUE) == 0);
    }

    private void waitForHandlingQuotesToFinish() {
        elvlExecutor.shutdown();
        try {
            boolean terminated = elvlExecutor.awaitTermination(2, TimeUnit.SECONDS);
            if (!terminated) {
                log.warn("At least one quote is still under handling");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Waiting for handling quotes to finish is interrupted");
        }

    }
}