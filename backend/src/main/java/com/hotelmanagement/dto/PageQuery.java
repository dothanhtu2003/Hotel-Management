package com.hotelmanagement.dto;

import com.hotelmanagement.Constants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageQuery {
    @Min(0)
    private int page = 0;
    @Min(1) @Max(Constants.MAX_PAGE_SIZE)
    private int size = Constants.DEFAULT_PAGE_SIZE;
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public PageRequest toPageRequest() {
        return PageRequest.of(page, size, Sort.by("id").ascending());
    }
}
