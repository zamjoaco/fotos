package com.fotos.common;

/**
 * Recurso no encontrado (o, por diseno, no publicado - ver research.md #4
 * de la feature 001-portfolio-publico: ambos casos deben ser indistinguibles
 * para el cliente).
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super("not_found");
    }
}
