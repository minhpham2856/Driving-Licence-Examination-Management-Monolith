<<<<<<<< Updated upstream:src/java/util/registrant/DocumentUrlResolver.java
package util.registrant;

import dto.staff.ManagingStaffApprovalView;
import dto.registrant.RegistrantDocumentView;
========
package util;

import model.staff.ManagingStaffApprovalView;
import model.registrant.RegistrantDocumentView;
>>>>>>>> Stashed changes:src/java/util/DocumentUrlResolver.java
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Chuẩn hóa tham chiếu DocumentUrl (Cloudinary / legacy local) thành URL xem được trên trình duyệt. */
public final class DocumentUrlResolver {

    private static final Logger LOG = Logger.getLogger(DocumentUrlResolver.class.getName());

    private DocumentUrlResolver() {
    }

    public static void resolveViewUrls(List<RegistrantDocumentView> docs, HttpServletRequest request) {
        if (docs == null) {
            return;
        }
        for (RegistrantDocumentView doc : docs) {
            if (doc != null) {
                doc.setDocumentUrl(resolveViewUrl(doc.getDocumentUrl(), request));
            }
        }
    }

    public static void resolveApprovalViewUrls(ManagingStaffApprovalView view, HttpServletRequest request) {
        if (view == null) {
            return;
        }
        Map<String, RegistrantDocumentView> slots = view.getDocumentsByType();
        if (slots != null) {
            for (RegistrantDocumentView doc : slots.values()) {
                if (doc != null) {
                    doc.setDocumentUrl(resolveViewUrl(doc.getDocumentUrl(), request));
                }
            }
        }
        resolveViewUrls(view.getOtherDocuments(), request);
    }

    public static String resolveViewUrl(String storedRef, HttpServletRequest request) {
        if (storedRef == null || storedRef.isBlank()) {
            return storedRef;
        }
        if (storedRef.startsWith("http://") || storedRef.startsWith("https://")) {
            return storedRef;
        }
        if (CloudinaryDocumentStorage.isCloudinaryRef(storedRef)) {
            CloudinaryDocumentStorage.CloudinaryRef ref = CloudinaryDocumentStorage.parseRef(storedRef);
            if (ref == null) {
                return storedRef;
            }
            String signed = CloudinaryDocumentStorage.signedDeliveryUrl(ref.resourceType, ref.publicId);
            return signed != null ? signed : storedRef;
        }
        if (request != null) {
            return RegistrantUploadStorage.normalizePublicUrl(request, storedRef);
        }
        return storedRef;
    }

    public static void deleteStoredRef(ServletContext ctx, String storedRef) {
        if (storedRef == null || storedRef.isBlank()) {
            return;
        }
        if (CloudinaryDocumentStorage.isCloudinaryRef(storedRef)) {
            try {
                CloudinaryDocumentStorage.destroy(storedRef);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Không xóa được tệp Cloudinary: " + storedRef, ex);
            }
            return;
        }
        if (ctx != null) {
            RegistrantUploadStorage.deleteStoredFile(ctx, storedRef);
        }
    }
}
