package org.example.bishopprototype.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;


@AllArgsConstructor
@Getter
public class MetricsResponse {
    private int queueSize;
    private Map<String, Integer> completedByAuthor;
}
