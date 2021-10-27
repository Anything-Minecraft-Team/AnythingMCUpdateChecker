package org.anythingmc.updatechecker.enums;

import lombok.Getter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.function.Function;

@Getter
public enum Status {

    ACTIVE(updated -> LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusMonths(6).toEpochSecond() < updated && LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond() > updated, "Active"),
    INACTIVE(updated -> LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusMonths(12).toEpochSecond() < updated && LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusMonths(6).toEpochSecond() > updated, "Inactive"),
    DISCONTINUED(updated -> LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusMonths(12).toEpochSecond() > updated, "Discontinued"),
    LOST(updated -> false, "Lost"),
    FOUND(updated -> false, "Found"),
    UNRELEASED(updated -> false, "Unreleased"),
    PRIVATE(updated -> false, "Private"),
    PRICE(updated -> false, "Price");

    private final Function<Long, Boolean> exception;
    private final String status;

    Status(final Function<Long, Boolean> exception, String status) {
        this.status = status;
        this.exception = exception;
    }
}