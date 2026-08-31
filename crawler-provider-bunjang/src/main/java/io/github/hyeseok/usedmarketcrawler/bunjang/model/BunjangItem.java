package io.github.hyeseok.usedmarketcrawler.bunjang.model;

public record BunjangItem(
    Long pid,
    String name,
    Long price,
    String status,
    String productImage,
    Long shopUid,
    Integer favoriteCount,
    Integer buntalkCount,
    String updatedAt,
    boolean care,
    boolean video,
    boolean ad
) {
}