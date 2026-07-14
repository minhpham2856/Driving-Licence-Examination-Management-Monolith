package examstaff.service;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.AllocationStageViewDTO;
import examstaff.util.ExamRegistrationSort;

import java.util.List;

/**
 * Xây dựng view danh sách phân phòng theo giai đoạn (LT/TH) kèm lọc và phân trang.
 */
public interface AllocationStageViewService {

    /**
     * Ghép danh sách thí sinh phân phòng theo stage, bộ lọc và sắp xếp.
     *
     * @param candidates   danh sách nguồn
     * @param stage        giai đoạn (ví dụ lý thuyết / thực hành)
     * @param resultFilter lọc kết quả đỗ/trượt (có thể rỗng)
     * @param searchQuery  từ khóa tìm kiếm (có thể rỗng)
     * @param page         trang hiện tại
     * @param pageSize     kích thước trang
     * @param sortSpec     quy tắc sắp xếp
     * @param areaFilterId lọc theo khu vực (null = không lọc)
     * @return DTO view phân phòng theo stage
     */
    AllocationStageViewDTO buildView(List<ExamRegistrationDTO> candidates, String stage,
            String resultFilter, String searchQuery, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec, Integer areaFilterId);
}
