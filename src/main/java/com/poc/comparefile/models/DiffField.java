package com.poc.comparefile.models;

import lombok.Data;

@Data
public class DiffField {
    private String fieldName;
    private String leftValue;
    private String rightValue;
}
