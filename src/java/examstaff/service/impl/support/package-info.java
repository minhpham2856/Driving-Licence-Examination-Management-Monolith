/**
 * Helper nội bộ BLL theo subdomain — không gọi từ servlet/controller.
 * <p>
 * Luồng đọc code: controller → consolidator (examstaff.service.impl.*) → support subdomain.
 * Mỗi subpackage là một bounded context nghiệp vụ staff sát hạch.
 *
 * Kiến trúc phân lớp:
 * <pre>
 *   Servlet / JSP
 *        ▼
 *   Service consolidator (examstaff.service.impl.*)
 *        ▼
 *   support.<subdomain>.*  — logic nghiệp vụ chi tiết, có thể inject/test
 *        ▼
 *   DAO / util / shared model
 * </pre>
 *
 * Subpackages:
 * - call — gọi số, hàng đợi, CallBoard
 * - procedure — thủ tục hồ sơ / ảnh / thu phí
 * - allocation — phân phòng/sân theo giai đoạn
 * - assign — phân công sát hạch viên
 * - view — dashboard, dossier, báo cáo, ảnh
 * - audit — nhật ký / xuất audit staff
 * - shared — quy tắc kỳ thi / hạng bằng dùng chung
 *
 * Quy ước:
 * Class *Rules / *Helper thường là pure (không HTTP/DAO);
 * *ServiceImpl orchestrate và gọi DAO qua interface hoặc impl mặc định.
 */
package examstaff.service.impl.support;
