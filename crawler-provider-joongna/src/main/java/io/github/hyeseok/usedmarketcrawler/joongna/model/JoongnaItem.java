package io.github.hyeseok.usedmarketcrawler.joongna.model;

public record JoongnaItem(
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