package org.example.logfilewatcherwithredis.dto;

import java.util.List;

public record ReadRecord(List<String> lines, long offset) {
}
