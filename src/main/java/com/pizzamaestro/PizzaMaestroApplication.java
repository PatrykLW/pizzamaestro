package com.pizzamaestro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PizzaMaestro - Zaawansowany kalkulator do pizzy
 * 
 * Główna klasa uruchomieniowa aplikacji Spring Boot.
 * Aplikacja oferuje precyzyjne kalkulacje receptur ciasta na pizzę,
 * zarządzanie procesem fermentacji, powiadomienia i wiele więcej.
 * 
 * @author PizzaMaestro Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class PizzaMaestroApplication {

    public static void main(String[] args) {
        SpringApplication.run(PizzaMaestroApplication.class, args);
    }
}
