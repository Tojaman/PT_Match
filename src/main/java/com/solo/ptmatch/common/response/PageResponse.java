package com.solo.ptmatch.common.response;

import org.springframework.data.domain.Page;

public class PageResponse {

    private static final PageResponse EMPTY = new PageResponse(0, 0, 0L, 0, false);

    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;

    private PageResponse(int page, int size, long totalElements, int totalPages, boolean hasNext) {
        this.page = Math.max(page, 0);
        this.size = Math.max(size, 0);
        this.totalElements = Math.max(totalElements, 0L);
        this.totalPages = Math.max(totalPages, 0);
        this.hasNext = hasNext;
    }

    public static PageResponse of(int page, int size, long totalElements, int totalPages, boolean hasNext) {
        return new PageResponse(page, size, totalElements, totalPages, hasNext);
    }

    public static PageResponse from(Page<?> page) {
        if (page == null) {
            return empty();
        }
        return new PageResponse(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.hasNext());
    }

    public static PageResponse empty() {
        return EMPTY;
    }

    public static PageResponse nullSafe(PageResponse pageResponse) {
        return pageResponse == null ? empty() : pageResponse;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }
}
