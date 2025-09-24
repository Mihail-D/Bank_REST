package com.example.bankcards.service;

import com.example.bankcards.dto.HistoryFilterDto;
import com.example.bankcards.entity.History;
import com.example.bankcards.repository.HistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class HistoryServiceTest {
    @Mock
    private HistoryRepository historyRepository;
    @InjectMocks
    private HistoryService historyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByFilter_returnsFilteredPage() {
        HistoryFilterDto filter = new HistoryFilterDto();
        filter.setUserId(1L);
        filter.setEventType("TRANSFER");
        filter.setDateFrom(LocalDateTime.now().minusDays(10));
        filter.setDateTo(LocalDateTime.now());
        Pageable pageable = PageRequest.of(0, 10);
        History history = new History();
        Page<History> page = new PageImpl<>(Collections.singletonList(history));
        when(historyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        Page<History> result = historyService.findByFilter(filter, pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByFilter_emptyFilter_returnsAll() {
        HistoryFilterDto filter = new HistoryFilterDto();
        Pageable pageable = PageRequest.of(0, 10);
        Page<History> page = new PageImpl<>(Collections.emptyList());
        when(historyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        Page<History> result = historyService.findByFilter(filter, pageable);
        assertThat(result.getContent()).isEmpty();
    }
}
