package com.dcb.jpa;


// ==========================================
// 2. PAGINATION REQUEST
// ==========================================
// EXPLANATION: This class encapsulates the pagination request details from the client.
// It calculates the offset automatically based on the page number and size.
public class Pageable {

  private final int pageNumber; // 0-indexed page number
  private final int pageSize; //Number of records per page

  public Pageable(int pageNumber, int pageSize) {
    if (pageNumber < 0) {
      throw new IllegalArgumentException("Page Number cannot be negative");

    }
    if (pageSize < 1) {
      throw new IllegalArgumentException("PageSize should not be less than 1");
    }
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;

  }

  public int getPageNumber() {
    return pageNumber;
  }

  public int getPageSize() {
    return pageSize;
  }

  // EXPLANATION: Offset is the starting index for the data slice.
  // Example: Page 2 (index 1) with size 10 means the offset is 10.
  public int getOffset(){
    return pageNumber * pageSize;
  }
}
