package com.solo.ptmatch.chat.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ChatMessage {

    @Id
    private Long id;
}
