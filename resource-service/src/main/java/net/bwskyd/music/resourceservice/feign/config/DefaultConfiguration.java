package net.bwskyd.music.resourceservice.feign.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultConfiguration {
    @Bean
    ErrorDecoder errorDecoder() {
        return new FeignClientErrorDecoder();
    }
}