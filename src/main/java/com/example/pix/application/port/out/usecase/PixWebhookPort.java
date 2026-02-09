package com.example.pix.application.port.out.usecase;

import com.example.pix.application.port.out.model.PixWebhookRequest;

public interface PixWebhookPort {
    void notify(PixWebhookRequest request);
}
