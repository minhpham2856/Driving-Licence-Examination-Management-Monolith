/**
 * Helper nội bộ BLL theo subdomain — không gọi từ servlet/controller.
 * <p>
 * Luồng đọc code: controller → consolidator ({@code examstaff.service.impl.*}) → support subdomain.
 * Mỗi subpackage là một bounded context nghiệp vụ staff sát hạch.
 *
 * Kiến trúc phân lớp:
 * <pre>
 *   Servlet / JSP
 *        ▼
 *   Service consolidator (examstaff.service.impl.*)
 *        ▼
 *   support.&lt;subdomain&gt;.*  — logic nghiệp vụ chi tiết, có thể inject/test
 *        ▼
 *   DAO / util / shared model
 * </pre>
 *
 * Subpackages:
 * - {@code call} — gọi số, hàng đợi, CallBoard
 * - {@code procedure} — thủ tục hồ sơ / ảnh / thu phí
 * - {@code allocation} — phân phòng/sân theo giai đoạn
 * - {@code assign} — phân công sát hạch viên
 * - {@code view} — dashboard, dossier, báo cáo, ảnh
 * - {@code audit} — nhật ký / xuất audit staff
 * - {@code shared} — quy tắc kỳ thi / hạng bằng dùng chung
 *
 * Quy ước:
 * Class {@code *Rules} / {@code *Helper} thường là pure (không HTTP/DAO);
 * {@code *ServiceImpl} orchestrate và gọi DAO qua interface hoặc impl mặc định.
 */
package examstaff.service.impl.support;
