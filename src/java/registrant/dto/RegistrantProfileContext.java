package registrant.dto;

import registrant.enums.ProfileRegistrationStatus;
import shared.model.Profile;
import java.util.Collections;
import java.util.List;

/**
 * DTO snapshot hồ sơ thí sinh — gom Profile, danh sách tài liệu và kết quả đồng bộ trạng thái.
 * Dùng chung giữa upload, register-exam và profile qua RegistrantProfileSupport.loadContext.
 */
public final class RegistrantProfileContext {

    private final Profile profile;
    private final List<RegistrantDocumentView> documents;
    private final ProfileRegistrationSyncResult syncResult;

    public RegistrantProfileContext(Profile profile,
            List<RegistrantDocumentView> documents,
            ProfileRegistrationSyncResult syncResult) {
        this.profile = profile;
        this.documents = documents != null ? documents : Collections.emptyList();
        this.syncResult = syncResult;
    }

    public Profile getProfile() {
        return profile;
    }

    public int getProfileId() {
        return profile != null ? profile.getProfileId() : 0;
    }

    /** True nếu snapshot đã có hồ sơ Profile hợp lệ (profileId > 0). */
    public boolean hasProfile() {
        return profile != null && profile.getProfileId() > 0;
    }

    public List<RegistrantDocumentView> getDocuments() {
        return documents;
    }

    public ProfileRegistrationSyncResult getSyncResult() {
        return syncResult;
    }

    public String getRegistrationStatus() {
        if (syncResult != null && syncResult.getExpectedStatus() != null) {
            return syncResult.getExpectedStatus();
        }
        return ProfileRegistrationStatus.DRAFT;
    }
}
