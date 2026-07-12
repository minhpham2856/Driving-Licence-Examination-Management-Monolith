package examstaff.service;

import examstaff.dto.PublicCallSnapshotDTO;
import examstaff.model.view.CallBoardState;

public interface PublicCallQueryService {

    PublicCallSnapshotDTO loadSnapshot(int examId, String webRootPath, CallBoardState board);
}
