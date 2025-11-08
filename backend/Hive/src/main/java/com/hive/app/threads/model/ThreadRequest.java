package com.hive.app.threads.model;

import lombok.Data;

import java.util.List;

@Data
public class ThreadRequest {
    private String title;
    private List<String> tags;
    private String description;
}
