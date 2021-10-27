package org.anythingmc.updatechecker.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatusManager {

    private final Long date;

    public boolean isOutdated(final Status exceptType) {
        return exceptType.getException().apply(date);
    }
}
