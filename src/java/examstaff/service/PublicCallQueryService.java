package examstaff.service;

import examstaff.dto.PublicCallSnapshotDTO;
import examstaff.dto.view.CallBoardState;

public interface PublicCallQueryService {

    PublicCallSnapshotDTO loadSnapshot(int sessionId, String webRootPath, CallBoardState board);
}

