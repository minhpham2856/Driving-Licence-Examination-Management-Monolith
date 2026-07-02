<<<<<<<< Updated upstream:src/java/dto/registrant/RegistrantProfileContext.java
package dto.registrant;

import enums.registrant.ProfileRegistrationStatus;
import model.user.Profile;
========
package model.registrant;

import constant.ProfileRegistrationStatus;
>>>>>>>> Stashed changes:src/java/model/registrant/RegistrantProfileContext.java
import java.util.Collections;
import java.util.List;

/** Snapshot hồ sơ thí sinh + tài liệu + trạng thái đăng ký — dùng chung giữa các service registrant. */
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
        return profile != null ? profile.getId() : 0;
    }

    public boolean hasProfile() {
        return profile != null && profile.getId() > 0;
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
