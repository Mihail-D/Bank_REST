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
    private Path<String> namePath;
    private Path<String> usernamePath;
    private Path<String> emailPath;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        criteriaBuilder = mock(CriteriaBuilder.class);
        statusPath = mock(Path.class);
        userPath = mock(Path.class);
        userIdPath = mock(Path.class);
        namePath = (Path<String>) mock(Path.class);
        usernamePath = (Path<String>) mock(Path.class);
        emailPath = (Path<String>) mock(Path.class);

        when(root.get("status")).thenReturn(statusPath);
        when(root.get("user")).thenReturn(userPath);
        when(userPath.get("id")).thenReturn(userIdPath);
        when(userPath.get("name")).thenReturn((Path) namePath);
        when(userPath.get("username")).thenReturn((Path) usernamePath);
        when(userPath.get("email")).thenReturn((Path) emailPath);
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

        // Настраиваем моки для OR условия
        Expression<String> nameLower = mock(Expression.class);
        Expression<String> usernameLower = mock(Expression.class);
        Expression<String> emailLower = mock(Expression.class);

        Predicate namePredicate = mock(Predicate.class);
        Predicate usernamePredicate = mock(Predicate.class);
        Predicate emailPredicate = mock(Predicate.class);

        when(criteriaBuilder.lower(namePath)).thenReturn(nameLower);
        when(criteriaBuilder.lower(usernamePath)).thenReturn(usernameLower);
        when(criteriaBuilder.lower(emailPath)).thenReturn(emailLower);

        when(criteriaBuilder.like(eq(nameLower), anyString())).thenReturn(namePredicate);
        when(criteriaBuilder.like(eq(usernameLower), anyString())).thenReturn(usernamePredicate);
        when(criteriaBuilder.like(eq(emailLower), anyString())).thenReturn(emailPredicate);

        when(criteriaBuilder.or(namePredicate, usernamePredicate, emailPredicate)).thenReturn(expectedPredicate);

        // When
        Specification<Card> spec = CardSpecification.hasOwnerName(ownerName);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).lower(namePath);
        verify(criteriaBuilder).lower(usernamePath);
        verify(criteriaBuilder).lower(emailPath);
        verify(criteriaBuilder).like(eq(nameLower), eq("%иван%"));
        verify(criteriaBuilder).like(eq(usernameLower), eq("%иван%"));
        verify(criteriaBuilder).like(eq(emailLower), eq("%иван%"));
        verify(criteriaBuilder).or(namePredicate, usernamePredicate, emailPredicate);
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
