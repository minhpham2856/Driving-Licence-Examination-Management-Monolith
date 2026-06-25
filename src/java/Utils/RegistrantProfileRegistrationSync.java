package Utils;



import Constants.ProfileRegistrationStatus;

import DAO.RegistrantDAO;

import Models.ProfileRegistrationSyncResult;

import Models.RegistrantDocumentView;

import java.util.List;
import java.util.Map;

import java.util.logging.Level;

import java.util.logging.Logger;



/**

 * Workflow hồ sơ tài liệu: {@code ExamRegistration.RegistrationStatus} là nguồn sự thật.

 * Document.Notes chỉ lưu metadata / lý do từ chối — không quyết định trạng thái hiển thị.

 */

public final class RegistrantProfileRegistrationSync {



    private static final Logger LOG = Logger.getLogger(RegistrantProfileRegistrationSync.class.getName());



    private RegistrantProfileRegistrationSync() {

    }



    /** Đọc RegistrationStatus hiện có — không ghi đè DB. */

    public static ProfileRegistrationSyncResult load(int profileId, RegistrantDAO registrantDAO) {

        String status = registrantDAO.findProfileDocumentRegistrationStatus(profileId);

        ProfileRegistrationSyncResult result = new ProfileRegistrationSyncResult();

        result.setExpectedStatus(status);

        result.setActualStatus(status);

        result.setAligned(true);

        result.setUpdated(false);

        return result;

    }



    /** @deprecated Dùng {@link #load} — giữ tên cũ để không đổi mọi call site. */

    public static ProfileRegistrationSyncResult reconcile(int profileId,

            List<RegistrantDocumentView> docs,

            RegistrantDAO registrantDAO) {

        return load(profileId, registrantDAO);

    }



    public static String reconcileStatus(int profileId, List<RegistrantDocumentView> docs,

            RegistrantDAO registrantDAO) {

        return load(profileId, registrantDAO).getExpectedStatus();

    }



    /** Ghi RegistrationStatus + Notes mô tả (theo trạng thái và số tài liệu đã tải). */

    public static boolean updateRegistrationStatus(int profileId, String status,

            List<RegistrantDocumentView> docs, RegistrantDAO registrantDAO) {

        if (profileId <= 0 || status == null || status.isBlank()) {

            return false;

        }

        String notes = deriveNotesFromRegistrationStatus(docs, status.trim());

        boolean written = registrantDAO.syncProfileDocumentRegistration(profileId, status.trim(), notes);

        if (!written) {

            LOG.log(Level.WARNING, "Không cập nhật RegistrationStatus profile {0} → {1}",

                    new Object[] { profileId, status });

        }

        return written;

    }



    public static String deriveNotesFromRegistrationStatus(List<RegistrantDocumentView> allDocs, String status) {

        Map<String, RegistrantDocumentView> slots = buildRequiredSlots(allDocs);

        int uploaded = countUploadedRequired(slots);

        int requiredTotal = RegistrantDocumentStatusHelper.REQUIRED_TYPES.length;



        return switch (status) {

            case ProfileRegistrationStatus.APPROVED -> String.format(

                    "Ban quản lý đã duyệt hồ sơ (%d/%d giấy tờ bắt buộc đã tải).", uploaded, requiredTotal);

            case ProfileRegistrationStatus.PENDING -> String.format(

                    "Thí sinh đã gửi yêu cầu duyệt (%d/%d giấy tờ bắt buộc đã tải).", uploaded, requiredTotal);

            case ProfileRegistrationStatus.REJECTED -> "Có giấy tờ bị từ chối — vui lòng bổ sung và gửi duyệt lại.";

            case ProfileRegistrationStatus.DRAFT -> uploaded > 0

                    ? String.format("Đang bổ sung hồ sơ (%d/%d giấy tờ bắt buộc).", uploaded, requiredTotal)

                    : "Chưa tải giấy tờ bắt buộc.";

            default -> "";

        };

    }



    private static Map<String, RegistrantDocumentView> buildRequiredSlots(List<RegistrantDocumentView> allDocs) {

        Map<String, RegistrantDocumentView> slots = new java.util.LinkedHashMap<>();

        for (String type : RegistrantDocumentStatusHelper.REQUIRED_TYPES) {

            RegistrantDocumentView empty = new RegistrantDocumentView();

            empty.setDocumentType(type);

            slots.put(type, empty);

        }

        if (allDocs == null) {

            return slots;

        }

        for (RegistrantDocumentView doc : allDocs) {

            if (doc.getDocumentType() != null && slots.containsKey(doc.getDocumentType())) {

                slots.put(doc.getDocumentType(), doc);

            }

        }

        return slots;

    }



    private static int countUploadedRequired(Map<String, RegistrantDocumentView> slots) {

        int count = 0;

        for (RegistrantDocumentView doc : slots.values()) {

            if (doc.getDocumentUrl() != null && !doc.getDocumentUrl().isBlank()) {

                count++;

            }

        }

        return count;

    }

}


