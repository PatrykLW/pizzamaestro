package com.pizzamaestro.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO dla listy ID mąk.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlourIdsRequest {
    
    @NotEmpty(message = "Lista mąk nie może być pusta")
    @Size(min = 2, max = 10, message = "Lista musi zawierać od 2 do 10 mąk")
    private List<String> flourIds;
}
