package com.ecode.modelevalplat.common.page;

import java.util.List;

public class Page<T> {
    private int current;       // 当前页码
    private int size;          // 每页大小
    private int pages;         // 总页数
    private long total;        // 总记录数
    private List<T> records;   // 当前页数据
}