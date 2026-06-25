package dao;


import model.exam.ExamSection;
import java.util.List;

/**
 * DAO cho thao tác với phần thi (ExamSection) trong hệ thống.
 * Cung cấp các phương thức tra cứu phần thi theo mã, tên,
 * và lấy danh sách phần thi theo kỳ thi.
 */
public interface ExamSectionDAO {

    /**
     * Lấy thông tin phần thi theo mã.
     *
     * @param examSectionId mã phần thi
     * @return ExamSection model, hoặc null nếu không tìm thấy
     */
    ExamSection findById(int examSectionId);

    /**
     * Lấy danh sách tất cả phần thi.
     *
     * @return danh sách tất cả ExamSection
     */
    List<ExamSection> findAll();

    /**
     * Lấy danh sách phần thi theo mã ca thi.
     *
     * @param sessionId mã ca thi
     * @return danh sách ExamSection thuộc ca thi
     */
    List<ExamSection> findBySessionId(int sessionId);
}
