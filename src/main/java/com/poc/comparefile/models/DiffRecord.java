package com.poc.comparefile.models;

import lombok.Data;

@Data
public class DiffRecord {
    private Record leftRecord;
    private Record rightRecord;
}
