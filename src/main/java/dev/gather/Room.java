package dev.gather;

import java.math.BigDecimal;
import java.util.List;

public record Room(String id, String name, String description, int capacity,
                   BigDecimal hourlyRate, String color, List<String> amenities) {}
