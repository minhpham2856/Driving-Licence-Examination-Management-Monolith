/**
 * Support phân bổ thí sinh theo phòng/sân và stage view.
 *
 * Luồng end-to-end:
 * <pre>
 *   AllocationServlet (nhiều URL theo stage)
 *        │  resolve stage từ servlet path
 *        ▼
 *   AllocationService
 *        ├── AllocationStageViewServiceImpl  — lọc/paging/sort → AllocationStageViewDTO
 *        │         └── AllocationStageHelper — waiting/theory/practical/pass/fail/suspended
 *        ├── AllocationActionServiceImpl     — allocateRoom / allocatePracticalRoom / auto
 *        │         ├── ExamEnrollmentSectionSupport (DAO) — UPDATE phòng LT/TH
 *        │         └── ExaminerAllocationServiceImpl — auto theo phòng đã có SHV
 *        └── AllocationPassRules             — điểm đạt để vào stage results
 * </pre>
 *
 * Stage URL ↔ ý nghĩa:
 * - /examstaff/allocation — overview (có thể auto-allocate)
 * - ...-waiting — chưa vào LT/TH
 * - ...-theory / ...-practical — đã/đang phân phòng LT hoặc sân TH
 * - ...-results-pass|fail|suspended — lọc theo kết quả
 * <p>Phụ thuộc subdomain support.assign để biết phòng/sân nào đã có sát hạch viên
 * trước khi gán thí sinh.
 */
package examstaff.service.impl.support.allocation;
