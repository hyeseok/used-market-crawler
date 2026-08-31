package io.github.hyeseok.usedmarketcrawler.bunjang.model;

import java.util.List;

public record BunjangSearchPage(
    String policyKey,
    List<BunjangItem> items,
    String cursor,
    String nextCursor,
    Long totalCount
) {

    public BunjangSearchPage {

        items =
            items == null
                ? List.of()
                : List.copyOf(
                    items
                );
    }

    public boolean hasNext() {
        return nextCursor != null
            && !nextCursor.isBlank();
    }
}