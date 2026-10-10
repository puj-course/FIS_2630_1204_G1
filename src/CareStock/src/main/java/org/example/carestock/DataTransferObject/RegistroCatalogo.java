    package org.example.carestock.DataTransferObject;

    public record RegistroCatalogo<T>(int id, T producto, String estado) {
    }