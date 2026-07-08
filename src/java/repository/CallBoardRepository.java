package repository;

import model.view.CallBoardState;

import java.util.Map;

/** Lưu trữ trạng thái bảng gọi — triển khai hạ tầng (ServletContext) nằm ngoài service. */
public interface CallBoardRepository {

    CallBoardState getState(int examSessionId);

    void saveState(int examSessionId, CallBoardState state);

    void setActiveSessionId(int examSessionId);

    Integer getActiveSessionId();
}
