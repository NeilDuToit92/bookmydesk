package za.co.neildutoit.deskbooking.config;

import lombok.Data;

import java.util.List;

//TODO: Move to DB based system property
@Data
public class SystemConfig {
    private List<String> allowedDomains;
}
