package com.devsuperior.dscommerce.dto;

public class FieldMessage {
    private String fieldName;
    private String menssage;

    public FieldMessage(String fieldName, String menssage) {
        this.fieldName = fieldName;
        this.menssage = menssage;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getMenssage() {
        return menssage;
    }
}
