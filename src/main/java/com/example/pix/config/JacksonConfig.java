package com.example.pix.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer instantSerializerCustomizer() {
        return builder -> builder.modules(new SimpleModule()
                .addSerializer(Instant.class, new JsonSerializer<Instant>() {
                    @Override
                    public void serialize(
                            Instant value,
                            JsonGenerator gen,
                            SerializerProvider serializers
                    ) throws IOException {
                        String formatted = FORMATTER.format(value.atZone(SAO_PAULO));
                        gen.writeString(formatted);
                    }
                }));
    }
}
