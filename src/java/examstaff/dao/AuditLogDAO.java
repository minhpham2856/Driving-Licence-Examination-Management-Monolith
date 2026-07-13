package examstaff.dao;


import shared.model.Audit;
import examstaff.dto.user.AuditDTO;

import examstaff.dto.staff.StaffProcedureKpiDTO;

import java.util.List;

/**
 * DAO cho thao tÃ¡c vá»›i nháº­t kÃ½ kiá»ƒm tra (AuditLog) trong há»‡ thá»‘ng.
 * Cung cáº¥p cÃ¡c phÆ°Æ¡ng thá»©c ghi nháº­t kÃ½, truy váº¥n nháº­t kÃ½ theo ngÆ°á»i dÃ¹ng,
 * ngÃ y thÃ¡ng, ká»³ thi, há»— trá»£ phÃ¢n trang vÃ  thá»‘ng kÃª KPI cho cÃ¡n bá»™.
 */
public interface AuditLogDAO {

    /**
     * Ghi má»™t báº£n ghi nháº­t kÃ½ kiá»ƒm tra má»›i.
     *
     * @param log Ä‘á»‘i tÆ°á»£ng Audit chá»©a thÃ´ng tin nháº­t kÃ½
     * @return true náº¿u ghi thÃ nh cÃ´ng
     */
    boolean insert(Audit log);

    /**
     * Láº¥y danh sÃ¡ch nháº­t kÃ½ cá»§a ngÆ°á»i dÃ¹ng trong ngÃ y hÃ´m nay.
     *
     * @param userId mÃ£ ngÆ°á»i dÃ¹ng
     * @return danh sÃ¡ch AuditDTO
     */
    List<AuditDTO> getLogsByUserToday(int userId);

    /**
     * Láº¥y táº¥t cáº£ nháº­t kÃ½ trong ngÃ y hÃ´m nay.
     *
     * @return danh sÃ¡ch táº¥t cáº£ AuditDTO trong ngÃ y
     */
    List<AuditDTO> getAllLogsToday();

    /**
     * Láº¥y danh sÃ¡ch nháº­t kÃ½ cá»§a ngÆ°á»i dÃ¹ng theo ngÃ y cá»¥ thá»ƒ.
     *
     * @param userId mÃ£ ngÆ°á»i dÃ¹ng
     * @param dateStr ngÃ y cáº§n lá»c (Ä‘á»‹nh dáº¡ng yyyy-MM-dd)
     * @return danh sÃ¡ch AuditDTO
     */
    List<AuditDTO> getLogsByUserAndDate(int userId, String dateStr);

    /**
     * Láº¥y táº¥t cáº£ nháº­t kÃ½ theo ngÃ y cá»¥ thá»ƒ.
     *
     * @param dateStr ngÃ y cáº§n lá»c (Ä‘á»‹nh dáº¡ng yyyy-MM-dd)
     * @return danh sÃ¡ch AuditDTO
     */
    List<AuditDTO> getAllLogsByDate(String dateStr);

    /**
     * Láº¥y danh sÃ¡ch nháº­t kÃ½ cá»§a ngÆ°á»i dÃ¹ng theo ngÃ y cÃ³ phÃ¢n trang.
     *
     * @param userId   mÃ£ ngÆ°á»i dÃ¹ng
     * @param dateStr  ngÃ y cáº§n lá»c (Ä‘á»‹nh dáº¡ng yyyy-MM-dd)
     * @param page     sá»‘ trang (báº¯t Ä‘áº§u tá»« 1)
     * @param pageSize sá»‘ lÆ°á»£ng báº£n ghi trÃªn má»—i trang
     * @return danh sÃ¡ch AuditDTO theo trang
     */
    List<AuditDTO> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize);

    /**
     * Láº¥y táº¥t cáº£ nháº­t kÃ½ theo ngÃ y cÃ³ phÃ¢n trang.
     *
     * @param dateStr  ngÃ y cáº§n lá»c (Ä‘á»‹nh dáº¡ng yyyy-MM-dd)
     * @param page     sá»‘ trang (báº¯t Ä‘áº§u tá»« 1)
     * @param pageSize sá»‘ lÆ°á»£ng báº£n ghi trÃªn má»—i trang
     * @return danh sÃ¡ch AuditDTO theo trang
     */
    List<AuditDTO> getAllLogsByDatePaginated(String dateStr, int page, int pageSize);

    /**
     * Äáº¿m sá»‘ lÆ°á»£ng nháº­t kÃ½ cá»§a ngÆ°á»i dÃ¹ng theo ngÃ y.
     *
     * @param userId  mÃ£ ngÆ°á»i dÃ¹ng
     * @param dateStr ngÃ y cáº§n lá»c (Ä‘á»‹nh dáº¡ng yyyy-MM-dd)
     * @return sá»‘ lÆ°á»£ng báº£n ghi nháº­t kÃ½
     */
    int getLogsCountByUserAndDate(int userId, String dateStr);

    /**
     * Äáº¿m tá»•ng sá»‘ nháº­t kÃ½ theo ngÃ y.
     *
     * @param dateStr ngÃ y cáº§n lá»c (Ä‘á»‹nh dáº¡ng yyyy-MM-dd)
     * @return sá»‘ lÆ°á»£ng báº£n ghi nháº­t kÃ½
     */
    int getAllLogsCountByDate(String dateStr);

    /**
     * Láº¥y chá»‰ sá»‘ KPI thá»§ tá»¥c cá»§a cÃ¡n bá»™ (sá»‘ thÃ­ sinh Ä‘Ã£ cÃ³ áº£nh + thanh toÃ¡n do cÃ¡n bá»™ Ä‘Ã³ thu).
     *
     * @param userId     mÃ£ cÃ¡n bá»™
     * @param filterDate ngÃ y lá»c (Ä‘á»‹nh dáº¡ng yyyy-MM-dd) hoáº·c null Ä‘á»ƒ láº¥y táº¥t cáº£
     * @return StaffProcedureKpiDTO chá»©a thÃ´ng tin KPI
     */
    StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate);

    /**
     * Láº¥y danh sÃ¡ch nháº­t kÃ½ theo ká»³ thi cÃ³ phÃ¢n trang.
     *
     * @param sessionId mÃ£ ká»³ thi
     * @param page      sá»‘ trang (báº¯t Ä‘áº§u tá»« 1)
     * @param pageSize  sá»‘ lÆ°á»£ng báº£n ghi trÃªn má»—i trang
     * @return danh sÃ¡ch AuditDTO theo trang
     */
    List<AuditDTO> getLogsForSessionPaginated(int sessionId, int page, int pageSize);

    /**
     * Äáº¿m sá»‘ lÆ°á»£ng nháº­t kÃ½ theo ká»³ thi.
     *
     * @param sessionId mÃ£ ká»³ thi
     * @return sá»‘ lÆ°á»£ng báº£n ghi nháº­t kÃ½
     */
    int getLogsCountForSession(int sessionId);

    /**
     * Láº¥y danh sÃ¡ch nháº­t kÃ½ theo ká»³ thi cÃ³ phÃ¢n trang vÃ  tÃ¬m kiáº¿m.
     *
     * @param sessionId   mÃ£ ká»³ thi
     * @param page        sá»‘ trang (báº¯t Ä‘áº§u tá»« 1)
     * @param pageSize    sá»‘ lÆ°á»£ng báº£n ghi trÃªn má»—i trang
     * @param searchQuery tá»« khÃ³a tÃ¬m kiáº¿m
     * @return danh sÃ¡ch AuditDTO theo trang
     */
    List<AuditDTO> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery);

    /**
     * Äáº¿m sá»‘ lÆ°á»£ng nháº­t kÃ½ theo ká»³ thi cÃ³ tÃ¬m kiáº¿m.
     *
     * @param sessionId   mÃ£ ká»³ thi
     * @param searchQuery tá»« khÃ³a tÃ¬m kiáº¿m
     * @return sá»‘ lÆ°á»£ng báº£n ghi nháº­t kÃ½
     */
    int getLogsCountForSession(int sessionId, String searchQuery);

    /**
     * Láº¥y danh sÃ¡ch nháº­t kÃ½ vi pháº¡m theo ká»³ thi vá»›i giá»›i háº¡n sá»‘ lÆ°á»£ng.
     *
     * @param sessionId mÃ£ ká»³ thi
     * @param limit     sá»‘ lÆ°á»£ng tá»‘i Ä‘a báº£n ghi tráº£ vá»
     * @return danh sÃ¡ch AuditDTO cÃ¡c vi pháº¡m
     */
    List<AuditDTO> getViolationLogsForSession(int sessionId, int limit);
}

