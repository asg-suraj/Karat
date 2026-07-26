package com.dcb.jpa;

import java.util.List;


// ==========================================
// 3. PAGINATION RESPONSE
// ==========================================
// EXPLANATION: This class holds the actual slice of data retrieved from the repository,
// along with metadata about the overall dataset (which is crucial for UI rendering like page numbers).
public class Page<T> {

  private final List<T> content;
  private final int pageNumber;
  private final int pageSize;
  private final long totalElements;

  public Page(List<T> content, int pageNumber, int pageSize, long totalElements) {
    this.content = content;
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.totalElements = totalElements;
  }


  public int getTotalPages() {
    return (int) Math.ceil((double) totalElements / pageSize);
  }

  public List<T> getContent() {
    return content;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public int getPageSize() {
    return pageSize;
  }

  public long getTotalElements() {
    return totalElements;
  }
}
