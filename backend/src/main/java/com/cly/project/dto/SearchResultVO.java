package com.cly.project.dto;

import lombok.Data;

@Data
public class SearchResultVO<T> {

    private String type;

    private String typeName;

    private T data;

    private String title;

    private String description;

    private String url;
}
