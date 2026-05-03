package com.ecommerce.common.dto;

import java.util.List;

/**
 * Standard paginated response for list endpoints.
 */
public class PageResponse<T> {
    
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public PageResponse() {
    }

    public PageResponse(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages,
                        boolean hasNext, boolean hasPrevious) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    public static <T> PageResponse<T> of(List<T> content, int pageNumber, int pageSize, 
                                          long totalElements, int totalPages) {
        PageResponse<T> response = new PageResponse<>();
        response.content = content;
        response.pageNumber = pageNumber;
        response.pageSize = pageSize;
        response.totalElements = totalElements;
        response.totalPages = totalPages;
        response.hasNext = pageNumber < totalPages - 1;
        response.hasPrevious = pageNumber > 0;
        return response;
    }
}
