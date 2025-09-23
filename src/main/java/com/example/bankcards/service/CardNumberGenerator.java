package com.example.bankcards.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.bankcards.repository.CardRepository;
import java.security.SecureRandom;

@Service
public class CardNumberGenerator {
    private static final int CARD_NUMBER_LENGTH = 16;
    private final SecureRandom random = new SecureRandom();
    private final CardRepository cardRepository;

    @Autowired
    public CardNumberGenerator(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public String generateUniqueCardNumber() {
        String cardNumber;
        do {
            cardNumber = generateCardNumber();
        } while (cardRepository.existsByNumber(cardNumber));
        return cardNumber;
    }

    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder(CARD_NUMBER_LENGTH);
        for (int i = 0; i < CARD_NUMBER_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}

