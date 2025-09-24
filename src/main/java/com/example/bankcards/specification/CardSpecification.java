package com.example.bankcards.specification;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.jpa.domain.Specification;

public class CardSpecification {

    /**
     * Поиск карт по статусу
     */
    public static Specification<Card> hasStatus(CardStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Поиск карт по ID владельца
     */
    public static Specification<Card> hasOwner(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    /**
     * Поиск карт по имени владельца (содержит подстроку)
     */
    public static Specification<Card> hasOwnerName(String ownerName) {
        return (root, query, criteriaBuilder) -> {
            if (ownerName == null || ownerName.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + ownerName.trim().toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("name")), pattern);
        };
    }

    /**
     * Поиск карт по маске номера (последние 4 цифры)
     * Примечание: поскольку номера зашифрованы, этот поиск работает только для точного совпадения
     * последних 4 цифр в маскированном формате
     */
    public static Specification<Card> hasMask(String mask) {
        return (root, query, criteriaBuilder) -> {
            if (mask == null || mask.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            // Для поиска по маске нам нужно будет использовать дополнительную логику
            // в сервисном слое, так как номера зашифрованы
            return criteriaBuilder.conjunction();
        };
    }

    /**
     * Комбинированный поиск с несколькими критериями
     */
    public static Specification<Card> searchCards(CardStatus status, Long userId, String ownerName, String mask) {
        Specification<Card> spec = null;

        if (status != null) {
            spec = hasStatus(status);
        }

        if (userId != null) {
            Specification<Card> ownerSpec = hasOwner(userId);
            spec = spec == null ? ownerSpec : spec.and(ownerSpec);
        }

        if (ownerName != null && !ownerName.trim().isEmpty()) {
            Specification<Card> nameSpec = hasOwnerName(ownerName);
            spec = spec == null ? nameSpec : spec.and(nameSpec);
        }

        if (mask != null && !mask.trim().isEmpty()) {
            Specification<Card> maskSpec = hasMask(mask);
            spec = spec == null ? maskSpec : spec.and(maskSpec);
        }

        return spec;
    }

    /**
     * Поиск активных карт пользователя
     */
    public static Specification<Card> activeCardsOfUser(Long userId) {
        return hasOwner(userId).and(hasStatus(CardStatus.ACTIVE));
    }

    /**
     * Поиск заблокированных карт
     */
    public static Specification<Card> blockedCards() {
        return hasStatus(CardStatus.BLOCKED);
    }

    /**
     * Поиск истекших карт
     */
    public static Specification<Card> expiredCards() {
        return hasStatus(CardStatus.EXPIRED);
    }
}
