package ru.otus.domain;

import lombok.Value;

import java.math.BigDecimal;

/**
 * @author Aleksandr Semykin
 */
@Value
public class Elvl {
    String isin;
    BigDecimal value;
}
