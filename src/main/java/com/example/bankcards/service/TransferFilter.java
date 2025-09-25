package com.example.bankcards.service;

import com.example.bankcards.entity.Transfer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Утилитарный компонент для унифицированной фильтрации исходящих переводов.
 */
@Component
public class TransferFilter {

    /** Возвращает только исходящие переводы указанного пользователя. */
    public List<Transfer> outgoingForUser(List<Transfer> transfers, Long userId) {
        if (userId == null) return List.of();
        return transfers.stream()
                .filter(t -> t.getSourceCard() != null && t.getSourceCard().getUser() != null
                        && userId.equals(t.getSourceCard().getUser().getId()))
                .collect(Collectors.toList());
    }

    /** Возвращает исходящие переводы по конкретной карте (sourceCard = cardId). */
    public List<Transfer> outgoingForCard(List<Transfer> transfers, Long cardId) {
        if (cardId == null) return List.of();
        return transfers.stream()
                .filter(t -> t.getSourceCard() != null && cardId.equals(t.getSourceCard().getId()))
                .collect(Collectors.toList());
    }

    /** Универсальная фильтрация: если админ – возвращаем все входящие и исходящие, иначе только исходящие пользователя. */
    public List<Transfer> visibleForUserOrAdmin(List<Transfer> transfers, Long currentUserId, boolean isAdmin) {
        if (isAdmin) return transfers; // политика: админ сейчас видит только исходящие выше по контроллеру, но оставляем возможность расширить
        return outgoingForUser(transfers, currentUserId);
    }
}

