package com.example.bankcards.service;

import com.example.bankcards.dto.HistoryFilterDto;
import com.example.bankcards.entity.History;
import com.example.bankcards.repository.HistoryRepository;
import com.example.bankcards.specification.HistorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;

    public Page<History> findByFilter(HistoryFilterDto filter, Pageable pageable) {
        Specification<History> spec = (root, query, cb) -> cb.conjunction();
        if (filter.getUserId() != null) {
            spec = spec.and(HistorySpecification.hasUserId(filter.getUserId()));
        }
        if (filter.getCardId() != null) {
            spec = spec.and(HistorySpecification.hasCardId(filter.getCardId()));
        }
        if (filter.getTransferId() != null) {
            spec = spec.and(HistorySpecification.hasTransferId(filter.getTransferId()));
        }
        if (StringUtils.hasText(filter.getEventType())) {
            spec = spec.and(HistorySpecification.hasEventType(filter.getEventType()));
        }
        if (filter.getDateFrom() != null || filter.getDateTo() != null) {
            spec = spec.and(HistorySpecification.eventDateBetween(filter.getDateFrom(), filter.getDateTo()));
        }
        return historyRepository.findAll(spec, pageable);
    }

    public Optional<History> getHistoryById(Long id) {
        return historyRepository.findById(id);
    }
}
