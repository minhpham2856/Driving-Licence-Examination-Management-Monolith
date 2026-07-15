/**
 * Helper nội bộ BLL theo subdomain — không gọi từ servlet/controller.
 * <p>
 * Luồng đọc code: controller → consolidator ({@code examstaff.service.impl.*}) → support subdomain.
 * Mỗi subpackage là một bounded context nghiệp vụ staff sát hạch.
 * </p>
 * Subpackages:
 * <ul>
 *   <li>{@code call} — gọi số, hàng đợi, CallBoard</li>
 *   <li>{@code procedure} — thủ tục hồ sơ / ảnh / thu phí</li>
 *   <li>{@code allocation} — phân phòng/sân theo giai đoạn</li>
 *   <li>{@code assign} — phân công sát hạch viên</li>
 *   <li>{@code view} — dashboard, dossier, báo cáo, ảnh</li>
 *   <li>{@code audit} — nhật ký / xuất audit staff</li>
 *   <li>{@code shared} — quy tắc kỳ thi / hạng bằng dùng chung</li>
 * </ul>
 */
package examstaff.service.impl.support;
