package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardEncryptionService;
import com.example.bankcards.service.CardNumberGenerator;
import com.example.bankcards.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
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
}
