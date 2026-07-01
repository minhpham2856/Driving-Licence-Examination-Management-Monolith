package examstaff.service;

import dto.exam.ExamRegistrationDTO;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface CandidatePhotoService {

    void normalizeQueue(String webRoot, List<ExamRegistrationDTO> queue);

    void normalizePhotoPaths(String webRoot, List<ExamRegistrationDTO> queue);

    boolean hasPhotoRecord(ExamRegistrationDTO reg);

    boolean resolveCapturedPhoto(String webRoot, ExamRegistrationDTO reg);

    boolean photoFileExists(String webRoot, String photoUrl);

    File findPhotoFile(String webRoot, String photoUrl);

    void writePhotoFile(String webRoot, String fileName, byte[] imageBytes) throws IOException;

    String toWebPhotoPath(String fileName);

    String extractFileName(String photoUrl);

    File resolveUploadDir(String webRoot);
}
