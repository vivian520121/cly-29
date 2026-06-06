package com.cly.project.common;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {

    private long total;
    private List<T> list;
    private long pageNum;
    private long pageSize;
    private long pages;

    public PageResult() {}

    public PageResult(long total, List<T> list, long pageNum, long pageSize) {
        this.total = total;
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = pageSize > 0 ? (total + pageSize - 1) / pageSize : 0;
    }

    public static <T> PageResult<T> of(long total, List<T> list, long pageNum, long pageSize) {
        return new PageResult<>(total, list, pageNum, pageSize);
    }
}
