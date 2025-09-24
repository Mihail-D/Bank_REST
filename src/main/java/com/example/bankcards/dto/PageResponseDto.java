package com.example.bankcards.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDto<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;

    public static <T> PageResponseDto<T> of(List<T> content, int page, int size,
                                           long totalElements, int totalPages,
                                           boolean first, boolean last) {
        PageResponseDto<T> response = new PageResponseDto<>();
        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setFirst(first);
        response.setLast(last);
        response.setEmpty(content.isEmpty());
        return response;
    }
}
