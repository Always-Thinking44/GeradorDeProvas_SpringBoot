package com.prova.gerador_provas.enums;

public enum ModeloTemplate {
    MODELO_1,
    MODELO_2,
    MODELO_3;

    public String fileName() {
        return switch (this) {
            case MODELO_1 -> "modelo1";
            case MODELO_2 -> "modelo2";
            case MODELO_3 -> "modelo3";
        };
    }
}
