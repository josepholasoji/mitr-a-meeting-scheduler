package com.doodle.challenge.dto;

import org.springframework.data.domain.Page;

import java.util.List;

// own envelope instead of serializing Spring Data's Page/PageImpl directly - those carry internal, non-contract fields
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
