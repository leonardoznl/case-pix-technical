package com.example.pix.application.port.in.usecase;

import com.example.pix.application.port.in.model.PixPaymentRequest;
import com.example.pix.application.port.in.model.PixResponse;

public interface ProcessPixUseCase {
    PixResponse process(PixPaymentRequest request);
}
