package com.example.bankcards.util;

import com.example.bankcards.dto.PageRequestDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PageableUtils {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    public static Pageable createPageable(PageRequestDto pageRequestDto) {
        int page = Math.max(0, pageRequestDto.getPage());
        int size = Math.min(MAX_PAGE_SIZE, Math.max(1, pageRequestDto.getSize()));

        Sort sort = createSort(pageRequestDto.getSortBy(), pageRequestDto.getSortDirection());

        return PageRequest.of(page, size, sort);
    }

    public static Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        page = Math.max(0, page);
        size = Math.min(MAX_PAGE_SIZE, Math.max(1, size));

        Sort sort = createSort(sortBy, sortDirection);

        return PageRequest.of(page, size, sort);
    }

    private static Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "id";
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

        // Поддержка сортировки по нескольким полям, разделенным запятой
        List<String> fields = Arrays.stream(sortBy.split(","))
                .map(String::trim)
                .filter(field -> !field.isEmpty())
                .filter(PageableUtils::isValidSortField)
                .collect(Collectors.toList());

        if (fields.isEmpty()) {
            fields = List.of("id");
        }

        Sort sort = Sort.by(direction, fields.get(0));
        for (int i = 1; i < fields.size(); i++) {
            sort = sort.and(Sort.by(direction, fields.get(i)));
        }

        return sort;
    }

    private static boolean isValidSortField(String field) {
        // Список разрешенных полей для сортировки карт
        List<String> allowedFields = List.of(
            "id", "status", "expirationDate", "createdAt",
            "user.id", "user.firstName", "user.lastName", "user.email"
        );
        return allowedFields.contains(field);
    }
}
