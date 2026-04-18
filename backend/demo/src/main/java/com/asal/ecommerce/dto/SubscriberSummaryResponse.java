package com.asal.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberSummaryResponse {
    private Long    id;
    private String  name;
    private String  email;
    private boolean verified;
    private boolean active;
}
