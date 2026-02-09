package com.example.pix.adapters.out.persistence;

import com.example.pix.domain.enums.PixDirection;
import com.example.pix.domain.enums.PixStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pix_communication")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PixCommunicationEntity {
    @Id
    private String id;

    private String transactionId;

    private PixDirection direction;

    private PixStatus status;

    private String reason;

    private String payload;

    private Instant createdAt;
}
