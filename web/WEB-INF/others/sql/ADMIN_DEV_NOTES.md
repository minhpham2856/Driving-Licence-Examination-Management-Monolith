# Ghi chú triển khai Admin — Zone / Phòng thi / Thiết bị

Tài liệu này dành cho dev admin triển khai CRUD sau khi DDL/DML và model dùng chung đã merge.
**Không thuộc phạm vi implement hiện tại** của luồng staff/examiner.

## Phụ thuộc

- Schema: `DDL_DLEM_DB.sql` — bảng `ExamZone`, `ExamArea` (FK `ExamZoneId`, `AreaType`), `ExamDevice`
- Model: `model.ExamZone`, `model.ExamArea`, `enums.ExamAreaType`
- Session ca thi: `Session.IsMorningSession` (staff/examiner đã dùng `shiftLabel`)

## Mapping URL ↔ Entity

| Sidebar (admin) | URL | Entity | Servlet đề xuất |
|-----------------|-----|--------|-----------------|
| Khu vực thi | `/admin/exam-area` | `ExamZone` | `ExamZoneServlet` (refactor từ `ExamAreaServlet`) |
| Phòng thi | `/admin/exam-room` | `ExamArea` | `ExamRoomServlet` (mới, wire `exam-room.jsp`) |
| Máy thi / Thiết bị | `/admin/exam-computer` | `ExamDevice` | Giữ `ExamDeviceServlet`; form cascade Zone → Area |

## Layer cần tạo

```
controller/admin/ExamZoneServlet.java    -- CRUD ExamZone
controller/admin/ExamRoomServlet.java    -- CRUD ExamArea (FK ExamZoneId)
service/ExamZoneService.java + impl      -- validation ZoneName, Location
service/ExamAreaService.java             -- bổ sung filter theo zoneId, ExamAreaType enum
dao/ExamZoneDAO.java + impl              -- CRUD + search
```

`ExamAreaDAOImpl` hiện đã map `ExamZoneId` và JOIN `ExamZone` ở `getById()` — admin dev có thể tái sử dụng.

## JSP cần sửa

| File | Việc cần làm |
|------|----------------|
| `web/views/admin/exam-area.jsp` | Form/list **ExamZone** (`ZoneName`, `Location`, `IsActive`) |
| `web/views/admin/exam-area-form.jsp` | Form Zone; link "Xem phòng thi" → `/admin/exam-room?zoneId=` |
| `web/views/admin/exam-room.jsp` | CRUD **ExamArea**: `AreaName`, `AreaType` (4 loại), `Capacity`, `Location`, dropdown `ExamZoneId` |
| `web/views/admin/exam-computer.jsp` | Dropdown Zone → lọc Area → gán `ExamAreaId`; nhãn "Thiết bị thi" |

## Validation gợi ý

- **ExamZone:** `ZoneName`, `Location` bắt buộc; không xóa zone còn `ExamArea` con
- **ExamArea:** `ExamZoneId` bắt buộc; `Capacity` bắt buộc; `AreaType` ∈ `ExamAreaType` (Phòng thủ tục | Phòng thi | Sân thi)
- **ExamDevice:** `DeviceType` ∈ enum `DeviceType`; `ExamAreaId` phải khớp loại (VD: Máy tính → `Phòng thi`; Mô tô → `Sân thi`)

## Trung tâm loại 3 (A1, A, B1)

- A1/A: thiết bị **Máy tính** (phòng thi) + **Mô tô** (sân thi)
- B1: **Máy tính** + **Xe con** (sân/đường thi tùy cấu hình seed)

## Audit

Dùng `AuditEntity` hiện có; có thể thêm `EXAM_ZONE` nếu cần log riêng khu vực.

## Lưu ý `ExamAreaServlet` hiện tại

Servlet `/admin/exam-area` vẫn map nhầm sang `ExamArea` (legacy). Dev admin refactor sang `ExamZoneServlet` và chuyển CRUD phòng sang `ExamRoomServlet` — **không đổi schema** trừ khi có yêu cầu mới.
