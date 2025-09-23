package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;

public interface CardService {
    Card createCard(User user);
}
