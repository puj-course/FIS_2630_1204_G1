    package org.example.carestock.dto;

    public record RegistroCatalogo<T>(int id, T producto, String estado) {
    }