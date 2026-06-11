package Models;

import java.util.List;

/** Kết quả phân trang danh sách lịch thi thí sinh. */
public class ExamListPage {

    private List<MyExamRowView> items;
    private int page;
    private int pageSize;
    private int totalItems;
    private int totalPages;

    public List<MyExamRowView> getItems() {
        return items;
    }

    public void setItems(List<MyExamRowView> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < totalPages;
    }
}
