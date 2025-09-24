package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardSearchDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardEncryptionService;
import com.example.bankcards.service.CardNumberGenerator;
import com.example.bankcards.service.CardService;
import com.example.bankcards.specification.CardSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final CardEncryptionService cardEncryptionService;

    @Autowired
    public CardServiceImpl(CardRepository cardRepository, CardNumberGenerator cardNumberGenerator, CardEncryptionService cardEncryptionService) {
        this.cardRepository = cardRepository;
        this.cardNumberGenerator = cardNumberGenerator;
        this.cardEncryptionService = cardEncryptionService;
    }

    @Override
    public Card createCard(User user) {
        Card card = new Card();
        card.setUser(user);
        String plainNumber = cardNumberGenerator.generateUniqueCardNumber();
        card.setEncryptedNumber(cardEncryptionService.encrypt(plainNumber));
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(LocalDate.now().plusYears(3));
        return cardRepository.save(card);
    }

    // CRUD операции
    @Override
    @Transactional(readOnly = true)
    public Optional<Card> getCardById(Long id) {
        return cardRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsByUser(User user) {
        return cardRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsByUserId(Long userId) {
        return cardRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> getCardsByStatus(CardStatus status) {
        return cardRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> getActiveCardsByUserId(Long userId) {
        return cardRepository.findByUserIdAndStatus(userId, CardStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Card> getCardByMaskedNumber(String maskedNumber) {
        // Поиск по замаскированному номеру - проходим по всем картам и сравниваем маски
        return cardRepository.findAll().stream()
                .filter(card -> {
                    String decrypted = cardEncryptionService.decrypt(card.getEncryptedNumber());
                    String masked = cardEncryptionService.mask(decrypted);
                    return maskedNumber.equals(masked);
                })
                .findFirst();
    }

    // Операции управления картой
    @Override
    public Card blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карта с ID " + cardId + " не найдена"));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Карта уже заблокирована");
        }

        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    @Override
    public Card activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карта с ID " + cardId + " не найдена"));

        if (isCardExpired(card)) {
            throw new IllegalStateException("Нельзя активировать просроченную карту");
        }

        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    @Override
    public Card deactivateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карта с ID " + cardId + " не найдена"));

        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    @Override
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карта с ID " + cardId + " не найдена"));

        cardRepository.delete(card);
    }

    // Проверки статуса
    @Override
    @Transactional(readOnly = true)
    public boolean isCardActive(Long cardId) {
        return cardRepository.findById(cardId)
                .map(card -> card.getStatus() == CardStatus.ACTIVE && !isCardExpired(card))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCardExpired(Long cardId) {
        return cardRepository.findById(cardId)
                .map(this::isCardExpired)
                .orElse(true);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCardBlocked(Long cardId) {
        return cardRepository.findById(cardId)
                .map(card -> card.getStatus() == CardStatus.BLOCKED)
                .orElse(true);
    }

    // Бизнес-логика
    @Override
    @Transactional(readOnly = true)
    public boolean canPerformTransaction(Long cardId) {
        return cardRepository.findById(cardId)
                .map(card -> card.getStatus() == CardStatus.ACTIVE && !isCardExpired(card))
                .orElse(false);
    }

    @Override
    public Card renewCard(Long cardId) {
        Card oldCard = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карта с ID " + cardId + " не найдена"));

        // Создаем новую карту для того же пользователя
        Card newCard = createCard(oldCard.getUser());

        // Блокируем старую карту
        oldCard.setStatus(CardStatus.EXPIRED);
        cardRepository.save(oldCard);

        return newCard;
    }

    // Методы поиска
    @Override
    @Transactional(readOnly = true)
    public List<Card> searchCards(CardSearchDto searchDto) {
        Specification<Card> spec = null;

        if (searchDto.getStatus() != null) {
            spec = CardSpecification.hasStatus(searchDto.getStatus());
        }

        if (searchDto.getUserId() != null) {
            Specification<Card> ownerSpec = CardSpecification.hasOwner(searchDto.getUserId());
            spec = spec == null ? ownerSpec : spec.and(ownerSpec);
        }

        if (searchDto.getOwnerName() != null && !searchDto.getOwnerName().trim().isEmpty()) {
            Specification<Card> nameSpec = CardSpecification.hasOwnerName(searchDto.getOwnerName());
            spec = spec == null ? nameSpec : spec.and(nameSpec);
        }

        List<Card> cards;
        if (spec != null) {
            cards = cardRepository.findAll(spec);
        } else {
            cards = cardRepository.findAll();
        }

        // Дополнительная фильтрация по маске номера (поскольку номера зашифрованы)
        if (searchDto.getMask() != null && !searchDto.getMask().trim().isEmpty()) {
            cards = filterByMask(cards, searchDto.getMask());
        }

        return cards;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> searchCardsByMask(String mask) {
        if (mask == null || mask.trim().isEmpty()) {
            return List.of();
        }

        List<Card> allCards = cardRepository.findAll();
        return filterByMask(allCards, mask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> searchCardsByOwnerName(String ownerName) {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            return List.of();
        }

        Specification<Card> spec = CardSpecification.hasOwnerName(ownerName);
        return cardRepository.findAll(spec);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> searchCardsByStatusAndOwner(CardStatus status, Long userId) {
        Specification<Card> spec = null;

        if (status != null) {
            spec = CardSpecification.hasStatus(status);
        }

        if (userId != null) {
            Specification<Card> ownerSpec = CardSpecification.hasOwner(userId);
            spec = spec == null ? ownerSpec : spec.and(ownerSpec);
        }

        if (spec != null) {
            return cardRepository.findAll(spec);
        } else {
            return cardRepository.findAll();
        }
    }

    // Вспомогательный метод для фильтрации по маске
    private List<Card> filterByMask(List<Card> cards, String mask) {
        String searchMask = mask.trim();

        return cards.stream()
                .filter(card -> {
                    try {
                        String decrypted = cardEncryptionService.decrypt(card.getEncryptedNumber());
                        String maskedNumber = cardEncryptionService.mask(decrypted);

                        // Поиск по последним 4 цифрам
                        if (searchMask.length() == 4 && searchMask.matches("\\d{4}")) {
                            return maskedNumber.endsWith(searchMask);
                        }

                        // Поиск по полной маске
                        return maskedNumber.contains(searchMask);
                    } catch (Exception e) {
                        // Если не удается расшифровать, исключаем карту из результата
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    // Вспомогательные методы
    private boolean isCardExpired(Card card) {
        return card.getExpirationDate().isBefore(LocalDate.now());
    }
}
