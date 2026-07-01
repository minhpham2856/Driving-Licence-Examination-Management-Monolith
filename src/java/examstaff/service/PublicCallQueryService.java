package examstaff.service;

import examstaff.dto.PublicCallSnapshotDTO;
import examstaff.model.view.CallBoardState;

public interface PublicCallQueryService {

    PublicCallSnapshotDTO loadSnapshot(int sessionId, String webRootPath, CallBoardState board);
}
