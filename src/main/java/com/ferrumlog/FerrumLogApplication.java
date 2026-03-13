package com.ferrumlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de FerrumLog.
 * 
 * @SpringBootApplication activa:
 * - @Configuration: Configuración de beans
 * - @EnableAutoConfiguration: Configuración automática de Spring
 * - @ComponentScan: Escaneo de componentes (@Service, @Repository, @Controller)
 */
@SpringBootApplication
public class FerrumLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(FerrumLogApplication.class, args);
    }
}
