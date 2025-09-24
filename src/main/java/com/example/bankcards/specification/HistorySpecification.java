package com.example.bankcards.specification;

import com.example.bankcards.entity.History;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;

public class HistorySpecification {
    public static Specification<History> hasUserId(Long userId) {
        return (root, query, cb) -> userId == null ? cb.conjunction() : cb.equal(root.get("user").get("id"), userId);
    }
    public static Specification<History> hasCardId(Long cardId) {
        return (root, query, cb) -> cardId == null ? cb.conjunction() : cb.equal(root.get("card").get("id"), cardId);
    }
    public static Specification<History> hasTransferId(Long transferId) {
        return (root, query, cb) -> transferId == null ? cb.conjunction() : cb.equal(root.get("transfer").get("id"), transferId);
    }
    public static Specification<History> hasEventType(String eventType) {
        return (root, query, cb) -> (eventType == null || eventType.isBlank()) ? cb.conjunction() : cb.equal(root.get("eventType"), eventType);
    }
    public static Specification<History> eventDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from == null) return cb.lessThanOrEqualTo(root.get("eventDate"), to);
            if (to == null) return cb.greaterThanOrEqualTo(root.get("eventDate"), from);
            return cb.between(root.get("eventDate"), from, to);
        };
    }
}

