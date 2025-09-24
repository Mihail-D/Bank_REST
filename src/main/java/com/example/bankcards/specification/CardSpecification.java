package com.example.bankcards.specification;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

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
    public static Specification<Card> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    /**
     * Поиск карт по ID владельца (альтернативное название)
     */
    public static Specification<Card> hasOwner(Long userId) {
        return hasUserId(userId);
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

            // Поиск по name, username или email
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("username")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("email")), pattern)
            );
        };
    }

    /**
     * Поиск просроченных карт
     */
    public static Specification<Card> isExpired() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.lessThan(root.get("expirationDate"), LocalDate.now());
        };
    }

    /**
     * Поиск непросроченных карт
     */
    public static Specification<Card> isNotExpired() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.greaterThanOrEqualTo(root.get("expirationDate"), LocalDate.now());
        };
    }

    /**
     * Поиск активных карт
     */
    public static Specification<Card> isActive() {
        return hasStatus(CardStatus.ACTIVE).and(isNotExpired());
    }

    /**
     * Поиск заблокированных карт
     */
    public static Specification<Card> isBlocked() {
        return hasStatus(CardStatus.BLOCKED);
    }

    /**
     * Поиск карт, созданных после определенной даты
     */
    public static Specification<Card> createdAfter(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), date.atStartOfDay());
        };
    }

    /**
     * Поиск карт, созданных до определенной даты
     */
    public static Specification<Card> createdBefore(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), date.atStartOfDay());
        };
    }

    /**
     * Поиск карт по диапазону дат создания
     */
    public static Specification<Card> createdBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (startDate == null) {
                return createdBefore(endDate).toPredicate(root, query, criteriaBuilder);
            }
            if (endDate == null) {
                return createdAfter(startDate).toPredicate(root, query, criteriaBuilder);
            }
            return criteriaBuilder.between(root.get("createdAt"),
                    startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        };
    }

    /**
     * Комбинированный поиск по статусу и владельцу
     */
    public static Specification<Card> hasStatusAndOwner(CardStatus status, Long userId) {
        Specification<Card> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }

        if (userId != null) {
            spec = spec.and(hasUserId(userId));
        }

        return spec;
    }

    /**
     * Поиск по маске номера карты
     * Примечание: поскольку номера зашифрованы, этот метод возвращает conjunction
     * (поиск по маске должен выполняться в сервисном слое после расшифровки)
     */
    public static Specification<Card> hasMask(String mask) {
        return (root, query, criteriaBuilder) -> {
            // Поскольку номера карт зашифрованы, поиск по маске невозможен на уровне базы данных
            // Возвращаем conjunction (всегда true), чтобы получить все записи
            // Фильтрация по маске должна выполняться в сервисном слое
            return criteriaBuilder.conjunction();
        };
    }

    /**
     * Поиск активных карт пользователя
     */
    public static Specification<Card> activeCardsOfUser(Long userId) {
        return hasUserId(userId).and(hasStatus(CardStatus.ACTIVE));
    }

    /**
     * Поиск заблокированных карт
     */
    public static Specification<Card> blockedCards() {
        return hasStatus(CardStatus.BLOCKED);
    }

    /**
     * Поиск просроченных карт (по статусу)
     */
    public static Specification<Card> expiredCards() {
        return hasStatus(CardStatus.EXPIRED);
    }
}
