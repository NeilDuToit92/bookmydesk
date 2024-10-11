package za.co.neildutoit.deskbooking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CustomConfig {
    @Bean
    @ConfigurationProperties("system")
    public SystemConfig systemConfig() {
        return new SystemConfig();
    }
}
