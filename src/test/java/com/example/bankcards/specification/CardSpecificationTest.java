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
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(expectedPredicate);

        Specification<Card> spec = CardSpecification.hasStatus(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).equal(any(), any());
    }

    @Test
    void hasStatusShouldReturnEqualPredicateWhenStatusProvided() {
        CardStatus status = CardStatus.ACTIVE;
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.equal(statusPath, status)).thenReturn(expectedPredicate);

        Specification<Card> spec = CardSpecification.hasStatus(status);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).equal(statusPath, status);
        verify(criteriaBuilder, never()).conjunction();
    }

    @Test
    void hasOwnerShouldReturnConjunctionWhenUserIdIsNull() {
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(expectedPredicate);

        Specification<Card> spec = CardSpecification.hasOwner(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).equal(any(), any());
    }

    @Test
    void hasOwnerShouldReturnEqualPredicateWhenUserIdProvided() {
        Long userId = 1L;
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.equal(userIdPath, userId)).thenReturn(expectedPredicate);

        Specification<Card> spec = CardSpecification.hasOwner(userId);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).equal(userIdPath, userId);
        verify(criteriaBuilder, never()).conjunction();
    }

    @Test
    void hasOwnerNameShouldReturnConjunctionWhenNameIsNull() {
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(expectedPredicate);

        Specification<Card> spec = CardSpecification.hasOwnerName(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void hasOwnerNameShouldReturnConjunctionWhenNameIsEmpty() {
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(expectedPredicate);

        Specification<Card> spec = CardSpecification.hasOwnerName("   ");
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void hasOwnerNameShouldReturnLikePredicateWhenNameProvided() {
        String ownerName = "Иван";
        Predicate expectedPredicate = mock(Predicate.class);

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

        Specification<Card> spec = CardSpecification.hasOwnerName(ownerName);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

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
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(expectedPredicate);

        Specification<Card> spec = CardSpecification.hasMask("1234");
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void activeCardsOfUserShouldCombineOwnerAndStatusSpecs() {
        Long userId = 1L;

        Specification<Card> spec = CardSpecification.activeCardsOfUser(userId);

        assertNotNull(spec);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).equal(userIdPath, userId);
        verify(criteriaBuilder).equal(statusPath, CardStatus.ACTIVE);
    }

    @Test
    void blockedCardsShouldUseBlockedStatus() {
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.equal(statusPath, CardStatus.BLOCKED)).thenReturn(expectedPredicate);

        Specification<Card> spec = CardSpecification.blockedCards();
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).equal(statusPath, CardStatus.BLOCKED);
    }

    @Test
    void expiredCardsShouldUseExpiredStatus() {
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.equal(statusPath, CardStatus.EXPIRED)).thenReturn(expectedPredicate);

        Specification<Card> spec = CardSpecification.expiredCards();
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(expectedPredicate, result);
        verify(criteriaBuilder).equal(statusPath, CardStatus.EXPIRED);
    }
}
