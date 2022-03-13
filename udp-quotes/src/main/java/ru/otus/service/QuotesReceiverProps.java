package ru.otus.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Aleksandr Semykin
 */
@Component
@ConfigurationProperties(prefix="quotes")
@Data
public class QuotesReceiverProps {
    private int udpGeneratorPort;
    private int count;
    private int isinsCount;
}
