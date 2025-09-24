package com.example.bankcards.specification;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Expression;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardSpecificationTest {

    private Root<Card> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder criteriaBuilder;
    private Path<Object> statusPath;
    private Path<Object> userPath;
    private Path<Object> userIdPath;
    private Path<Object> namePath;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        criteriaBuilder = mock(CriteriaBuilder.class);
        statusPath = mock(Path.class);
        userPath = mock(Path.class);
        userIdPath = mock(Path.class);
        namePath = mock(Path.class);

        when(root.get("status")).thenReturn(statusPath);
        when(root.get("user")).thenReturn(userPath);
        when(userPath.get("id")).thenReturn(userIdPath);
        when(userPath.get("name")).thenReturn(namePath);
    }

    @Test
    void hasStatusShouldReturnConjunctionWhenStatusIsNull() {
        // Given
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(expectedPredicate);

        // When
        Specification<Card> spec = CardSpecification.hasStatus(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).equal(any(), any());
    }

    @Test
    void hasStatusShouldReturnEqualPredicateWhenStatusProvided() {
        // Given
        CardStatus status = CardStatus.ACTIVE;
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.equal(statusPath, status)).thenReturn(expectedPredicate);

        // When
        Specification<Card> spec = CardSpecification.hasStatus(status);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).equal(statusPath, status);
        verify(criteriaBuilder, never()).conjunction();
    }

    @Test
    void hasOwnerShouldReturnConjunctionWhenUserIdIsNull() {
        // Given
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(expectedPredicate);

        // When
        Specification<Card> spec = CardSpecification.hasOwner(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).equal(any(), any());
    }

    @Test
    void hasOwnerShouldReturnEqualPredicateWhenUserIdProvided() {
        // Given
        Long userId = 1L;
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.equal(userIdPath, userId)).thenReturn(expectedPredicate);

        // When
        Specification<Card> spec = CardSpecification.hasOwner(userId);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).equal(userIdPath, userId);
        verify(criteriaBuilder, never()).conjunction();
    }

    @Test
    void hasOwnerNameShouldReturnConjunctionWhenNameIsNull() {
        // Given
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(expectedPredicate);

        // When
        Specification<Card> spec = CardSpecification.hasOwnerName(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void hasOwnerNameShouldReturnConjunctionWhenNameIsEmpty() {
        // Given
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(expectedPredicate);

        // When
        Specification<Card> spec = CardSpecification.hasOwnerName("   ");
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void hasOwnerNameShouldReturnLikePredicateWhenNameProvided() {
        // Given
        String ownerName = "Иван";
        Predicate expectedPredicate = mock(Predicate.class);

        // Настраиваем моки для упрощенной проверки
        Expression<String> nameLower = mock(Expression.class);
        when(criteriaBuilder.lower(any(Expression.class))).thenReturn(nameLower);
        when(criteriaBuilder.like(eq(nameLower), anyString())).thenReturn(expectedPredicate);

        // When
        Specification<Card> spec = CardSpecification.hasOwnerName(ownerName);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).lower(any(Expression.class));
        verify(criteriaBuilder).like(eq(nameLower), anyString());
    }

    @Test
    void hasMaskShouldAlwaysReturnConjunction() {
        // Given
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(expectedPredicate);

        // When
        Specification<Card> spec = CardSpecification.hasMask("1234");
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void activeCardsOfUserShouldCombineOwnerAndStatusSpecs() {
        // Given
        Long userId = 1L;

        // When
        Specification<Card> spec = CardSpecification.activeCardsOfUser(userId);

        // Then
        assertNotNull(spec);

        // Проверяем, что спецификация корректно создана
        // (более детальное тестирование требует более сложной настройки моков)
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Проверяем, что были вызваны нужные методы
        verify(criteriaBuilder).equal(userIdPath, userId);
        verify(criteriaBuilder).equal(statusPath, CardStatus.ACTIVE);
    }

    @Test
    void blockedCardsShouldUseBlockedStatus() {
        // Given
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.equal(statusPath, CardStatus.BLOCKED)).thenReturn(expectedPredicate);

        // When
        Specification<Card> spec = CardSpecification.blockedCards();
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).equal(statusPath, CardStatus.BLOCKED);
    }

    @Test
    void expiredCardsShouldUseExpiredStatus() {
        // Given
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.equal(statusPath, CardStatus.EXPIRED)).thenReturn(expectedPredicate);

        // When
        Specification<Card> spec = CardSpecification.expiredCards();
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).equal(statusPath, CardStatus.EXPIRED);
    }
}
