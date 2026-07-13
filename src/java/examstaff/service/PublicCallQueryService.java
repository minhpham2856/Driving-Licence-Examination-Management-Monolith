package examstaff.service;

import examstaff.dto.PublicCallSnapshotDTO;
import examstaff.dto.view.CallBoardState;

public interface PublicCallQueryService {

    PublicCallSnapshotDTO loadSnapshot(int examId, String webRootPath, CallBoardState board);
}
