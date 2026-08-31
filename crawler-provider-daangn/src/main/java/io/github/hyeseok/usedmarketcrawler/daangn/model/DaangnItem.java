package io.github.hyeseok.usedmarketcrawler.daangn.model;

public record DaangnItem(

    String id,

    String title,

    Long price,

    String location,

    String imageUrl,

    String itemUrl,

    String description,

    String status,

    String publishedAt

) {
}