package service.impl;

import dto.exam.ExamRegistrationDTO;
import service.AllocationRegistrationService;
import service.ExamRegistrationService;

public class AllocationRegistrationServiceImpl implements AllocationRegistrationService {

    private final ExamRegistrationService registrationService = new ExamRegistrationServiceImpl();

    @Override
    public void quickCompleteProcedure(ExamRegistrationDTO profile, int registrationId) {
        if (profile == null || registrationId <= 0) {
            return;
        }
        String photoPath = "assets/imgs/candidates/" + profile.getSbd() + "_captured.png";
        registrationService.updatePhoto(registrationId, photoPath);
        registrationService.updatePayment(registrationId, true);
        registrationService.updatePresent(registrationId, true);
        profile.setPhotoUrl(photoPath);
        profile.setIsPaymentCompleted(true);
        profile.setIsPresent(true);
    }
}
