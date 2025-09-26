package com.example.bankcards.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Стандартный формат пагинированного ответа")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDto<T> {

    @Schema(description = "Содержимое страницы")
    private List<T> content;
    @Schema(description = "Номер страницы (0..N)", example = "0")
    private int page;
    @Schema(description = "Размер страницы", example = "20")
    private int size;
    @Schema(description = "Всего элементов", example = "100")
    private long totalElements;
    @Schema(description = "Всего страниц", example = "5")
    private int totalPages;
    @Schema(description = "Это первая страница", example = "true")
    private boolean first;
    @Schema(description = "Это последняя страница", example = "false")
    private boolean last;
    @Schema(description = "Пустой ли ответ", example = "false")
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
