package service;

import dto.SessionDTO;
import model.Session;
import java.util.List;

public interface SessionQueryService {
    SessionDTO toDto(int sessionId);
    SessionDTO toDto(Session session);
    List<SessionDTO> toDtoList(List<Session> sessions);
}
