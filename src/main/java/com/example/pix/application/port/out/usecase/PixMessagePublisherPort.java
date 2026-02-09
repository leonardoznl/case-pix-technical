package com.example.pix.application.port.out.usecase;

import com.example.pix.domain.PixMessage;

public interface PixMessagePublisherPort {
    void publish(PixMessage message);
}
