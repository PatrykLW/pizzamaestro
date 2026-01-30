package com.pizzamaestro.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO dla miksu mąk.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlourMixRequest {
    
    @NotEmpty(message = "Lista mąk nie może być pusta")
    @Size(min = 1, max = 10, message = "Lista musi zawierać od 1 do 10 mąk")
    @Valid
    private List<CalculationRequest.FlourMixEntry> flourMix;
}
