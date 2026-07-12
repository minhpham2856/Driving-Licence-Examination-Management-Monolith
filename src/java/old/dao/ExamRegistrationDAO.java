package dao;

import model.ExamRegistration;

public interface ExamRegistrationDAO {

    ExamRegistration getById(int examRegistrationId);

    int getLatestIdByProfileAndLicence(int profileId, int licenceId);

    int add(ExamRegistration registration);

    boolean update(ExamRegistration registration);

    boolean updateStatusWithReviewNote(int examRegistrationId, String status, String message, int actorUserId);
}
