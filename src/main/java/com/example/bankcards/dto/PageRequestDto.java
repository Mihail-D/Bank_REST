package com.example.bankcards.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDto {

    @Min(value = 0, message = "Номер страницы не может быть отрицательным")
    private int page = 0;

    @Min(value = 1, message = "Размер страницы должен быть больше 0")
    private int size = 20;

    @Pattern(regexp = "^[a-zA-Z]+(,[a-zA-Z]+)*$",
             message = "Поле для сортировки должно содержать только буквы и запятые")
    private String sortBy = "id";

    @Pattern(regexp = "^(asc|desc)$",
             message = "Направление сортировки должно быть 'asc' или 'desc'")
    private String sortDirection = "asc";
}
