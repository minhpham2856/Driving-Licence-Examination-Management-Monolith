package service;

import dto.examstaff.PublicCallSnapshotDTO;
import model.view.CallBoardState;
import repository.CallBoardRepository;

public interface PublicCallQueryService {

    PublicCallSnapshotDTO loadSnapshot(int sessionId, String webRootPath, CallBoardState board);
}
