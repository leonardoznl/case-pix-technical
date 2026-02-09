package com.example.pix.application.port.in.usecase;

import com.example.pix.domain.PixResponseMessage;

public interface HandlePixResponseUseCase {
    void handle(PixResponseMessage response);
}
