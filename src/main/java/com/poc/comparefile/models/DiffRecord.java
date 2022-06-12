package com.poc.comparefile.models;

import lombok.Data;

@Data
public class DiffRecord {
    private String leftRecord;
    private String rightRecord;
}
