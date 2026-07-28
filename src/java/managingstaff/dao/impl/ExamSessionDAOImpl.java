package managingstaff.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import managingstaff.dao.ExamSessionDAO;
import managingstaff.dto.ExamRegistrationDTO;
import managingstaff.dto.SessionDTO;
import shared.dbconnection.DBContext;
import shared.util.TentativeExamDatePolicy;

public class ExamSessionDAOImpl extends DBContext implements ExamSessionDAO {
    private static final String SELECT = """
        SELECT e.ExamId,e.ExamCode,CAST(e.ExamDate AS date) ExamDate,
               CAST(e.StartTime AS time) StartTime,CAST(e.EndTime AS time) EndTime,
               e.[Status],e.CentreName,e.LicenceId,l.LicenceClass,
               COALESCE(e.SourceExamDateId,0) SourceExamDateId,
               0 ExamAreaId,N'' AreaName,0 Capacity,ISNULL(s.SectionType,N'Lý thuyết') SectionType,
               (SELECT COUNT(*) FROM ExamEnrollment ee WHERE ee.ExamId=e.ExamId) RegisteredCount
        FROM Exam e JOIN Licence l ON l.LicenceId=e.LicenceId
        OUTER APPLY (SELECT TOP 1 es.SectionType FROM ExamSection es WHERE es.ExamId=e.ExamId ORDER BY es.ExamSectionId) s
        WHERE l.LicenceClass IN ('A1','A','B1')
        """;

    @Override public List<SessionDTO> getActiveSessions(){return query(SELECT+" AND e.[Status] NOT IN ('Cancelled',N'Đã hủy') AND CAST(e.ExamDate AS date)>=CAST(GETDATE() AS date) ORDER BY e.ExamDate");}
    @Override public List<SessionDTO> getAllSessions(){return query(SELECT+" ORDER BY e.ExamDate DESC,e.ExamId DESC");}
    @Override public boolean updateStatus(int id,String status){try(PreparedStatement ps=getConnection().prepareStatement("UPDATE Exam SET [Status]=? WHERE ExamId=?")){ps.setString(1,status);ps.setInt(2,id);return ps.executeUpdate()>0;}catch(SQLException e){throw dbError(e);}}
    @Override public SessionDTO findById(int id){List<SessionDTO> r=query(SELECT+" AND e.ExamId=?",List.of(id));return r.isEmpty()?null:r.get(0);}

    @Override public List<SessionDTO> findPage(String tab,List<Integer> years,int page,int size){
        List<Object> p=new ArrayList<>();String where=tabWhere(tab,years,p);
        p.add(Math.max(0,(page-1)*size));p.add(size);
        return query(SELECT+where+" ORDER BY e.ExamDate DESC,e.ExamId DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",p);
    }
    @Override public int count(String tab,List<Integer> years){
        List<Object> p=new ArrayList<>();String where=tabWhere(tab,years,p);
        String sql="SELECT COUNT(*) FROM Exam e JOIN Licence l ON l.LicenceId=e.LicenceId WHERE l.LicenceClass IN ('A1','A','B1')"+where;
        try(PreparedStatement ps=getConnection().prepareStatement(sql)){bind(ps,p);try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getInt(1):0;}}catch(SQLException e){throw dbError(e);}
    }
    @Override public List<Integer> findAvailableYears(){
        List<Integer> r=new ArrayList<>();try(PreparedStatement ps=getConnection().prepareStatement("SELECT DISTINCT YEAR(ExamDate) y FROM Exam WHERE ExamDate IS NOT NULL ORDER BY y DESC");ResultSet rs=ps.executeQuery()){while(rs.next())r.add(rs.getInt(1));return r;}catch(SQLException e){throw dbError(e);}
    }

    @Override
    public int create(SessionDTO s) {
        Connection c = getConnection();
        try {
            c.setAutoCommit(false);
            loadPoliceSource(c, s);
            validateFuture(s);
            validateAvailableDate(c, s.getExamDate(), 0);

            int examId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO Exam(ExamCode,ExamDate,StartTime,EndTime,[Status],CentreName,LicenceId,SourceExamDateId) "
                    + "VALUES(?,?,?,NULL,N'Chưa diễn ra',?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, buildCode(c, s));
                ps.setDate(2, s.getExamDate());
                ps.setTimestamp(3, startTimestamp(s));
                ps.setString(4, s.getCentreName());
                ps.setInt(5, s.getLicenceId());
                ps.setInt(6, s.getSourceExamDateId());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Không lấy được mã phiên thi");
                    examId = keys.getInt(1);
                }
            }

            int theorySectionId = createSection(c, examId, s.getLicenceId(), "Lý thuyết");
            int practicalSectionId = createSection(
                    c, examId, s.getLicenceId(), "Thực hành trong hình");

            copyOfficialCandidates(c, s, examId, theorySectionId, practicalSectionId);
            c.commit();
            return examId;
        } catch (SQLException e) {
            rollback(c);
            throw dbError(e);
        } catch (RuntimeException e) {
            rollback(c);
            throw e;
        } finally {
            auto(c);
        }
    }

    @Override
    public boolean update(SessionDTO s) {
        SessionDTO old = findById(s.getId());
        if (old == null || !old.isEditable()) {
            throw new IllegalArgumentException("Chỉ được sửa phiên chưa thi.");
        }

        // Ngày thi và hạng GPLX đã được CSGT ban hành nên không nhận lại từ form.
        s.setExamDate(old.getExamDate());
        s.setLicenceId(old.getLicenceId());
        s.setLicenseCode(old.getLicenseCode());
        s.setSourceExamDateId(old.getSourceExamDateId());
        validateFuture(s);

        Connection c = getConnection();
        try {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE Exam SET StartTime=?,EndTime=NULL,CentreName=? "
                    + "WHERE ExamId=? AND CAST(ExamDate AS date)>CAST(GETDATE() AS date) "
                    + "AND [Status] NOT IN ('Cancelled',N'Đã hủy')")) {
                ps.setTimestamp(1, startTimestamp(s));
                ps.setString(2, s.getCentreName());
                ps.setInt(3, s.getId());
                if (ps.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Phiên không còn được phép sửa.");
                }
            }
            c.commit();
            return true;
        } catch (SQLException e) {
            rollback(c);
            throw dbError(e);
        } finally {
            auto(c);
        }
    }

    @Override public boolean cancel(int id){try(PreparedStatement ps=getConnection().prepareStatement("UPDATE Exam SET [Status]=N'Đã hủy' WHERE ExamId=? AND CAST(ExamDate AS date)>CAST(GETDATE() AS date) AND [Status] NOT IN ('Cancelled',N'Đã hủy')")){ps.setInt(1,id);return ps.executeUpdate()>0;}catch(SQLException e){throw dbError(e);}}

    @Override public List<ExamRegistrationDTO> getCandidates(int id){List<ExamRegistrationDTO> r=new ArrayList<>();String sql="SELECT c.CandidateNumber,c.FullName,c.DateOfBirth,c.GovernmentIdNumber,c.PhoneNumber,c.Email,CASE WHEN c.TakeTheory=0 AND c.TakeLayout=1 THEN N'PRACTICAL_ONLY' ELSE N'FULL_EXAM' END ExamParticipationType FROM ExamEnrollment ee JOIN Candidate c ON c.CandidateId=ee.CandidateId WHERE ee.ExamId=? ORDER BY TRY_CONVERT(int,c.CandidateNumber),c.CandidateNumber";try(PreparedStatement ps=getConnection().prepareStatement(sql)){ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()){while(rs.next()){ExamRegistrationDTO x=new ExamRegistrationDTO();try{x.setCandidateNo(Integer.parseInt(rs.getString(1)));}catch(Exception ignored){}x.setFullName(rs.getString(2));x.setDateOfBirth(rs.getDate(3));x.setGovIdNo(rs.getString(4));x.setPhoneNo(rs.getString(5));x.setEmail(rs.getString(6));x.setExamParticipationType(rs.getString(7));r.add(x);}}return r;}catch(SQLException e){throw dbError(e);}}

    private List<SessionDTO> query(String sql){return query(sql,List.of());}
    private List<SessionDTO> query(String sql,List<?> p){List<SessionDTO> r=new ArrayList<>();try(PreparedStatement ps=getConnection().prepareStatement(sql)){bind(ps,p);try(ResultSet rs=ps.executeQuery()){while(rs.next()){SessionDTO x=new SessionDTO();x.setId(rs.getInt("ExamId"));x.setSessionName(rs.getString("ExamCode"));x.setExamDate(rs.getDate("ExamDate"));x.setShiftStartTime(rs.getTime("StartTime"));x.setShiftEndTime(rs.getTime("EndTime"));x.setStatus(rs.getString("Status"));x.setCentreName(rs.getString("CentreName"));x.setLicenceId(rs.getInt("LicenceId"));x.setLicenseCode(rs.getString("LicenceClass"));x.setSourceExamDateId(rs.getInt("SourceExamDateId"));x.setAreaId(rs.getInt("ExamAreaId"));x.setAreaName(rs.getString("AreaName"));x.setMaxCandidates(rs.getInt("Capacity"));x.setExamTypeName(rs.getString("SectionType"));x.setRegisteredCount(rs.getInt("RegisteredCount"));applyLifecycle(x);r.add(x);}}return r;}catch(SQLException e){throw dbError(e);}}
    private static void applyLifecycle(SessionDTO x){LocalDate d=x.getExamDate().toLocalDate(),today=LocalDate.now();boolean cancelled="Đã hủy".equals(x.getStatus())||"Cancelled".equalsIgnoreCase(x.getStatus());x.setEditable(d.isAfter(today)&&!cancelled);if(!cancelled)x.setStatus(d.isAfter(today)?"Chưa thi":d.isBefore(today)?"Đã thi":"Đang thi");}
    private static String tabWhere(String tab,List<Integer> years,List<Object> p){String cancelled="e.[Status] IN ('Cancelled',N'Đã hủy')";String c=switch(tab==null?"upcoming":tab){case"ongoing"->"CAST(e.ExamDate AS date)=CAST(GETDATE() AS date) AND NOT ("+cancelled+")";case"completed"->"CAST(e.ExamDate AS date)<CAST(GETDATE() AS date) AND NOT ("+cancelled+")";case"cancelled"->cancelled;default->"CAST(e.ExamDate AS date)>CAST(GETDATE() AS date) AND NOT ("+cancelled+")";};StringBuilder w=new StringBuilder(" AND ").append(c);if(("completed".equals(tab)||"cancelled".equals(tab))&&years!=null&&!years.isEmpty()){w.append(" AND YEAR(e.ExamDate) IN (").append(String.join(",",Collections.nCopies(years.size(),"?"))).append(")");p.addAll(years);}return w.toString();}
    private static void validateFuture(SessionDTO s){if(s.getExamDate()==null||!s.getExamDate().toLocalDate().isAfter(LocalDate.now()))throw new IllegalArgumentException("Ngày thi phải sau ngày hôm nay.");if(s.getShiftStartTime()==null)throw new IllegalArgumentException("Vui lòng chọn giờ bắt đầu.");}
    private static void validateAvailableDate(Connection c,java.sql.Date date,int excludedExamId){
        try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM Exam WHERE CAST(ExamDate AS date)=? AND ExamId<>? AND [Status] NOT IN ('Cancelled',N'Đã hủy')")){
            ps.setDate(1,date);ps.setInt(2,excludedExamId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next())throw new IllegalArgumentException("Ngày này đã có một phiên thi chính thức. Vui lòng chọn ngày khác.");
            }
        }catch(SQLException e){throw dbError(e);}
    }
    private static void loadPoliceSource(Connection c, SessionDTO s) {
        if (s.getSourceExamDateId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn danh sách CSGT đã duyệt.");
        }
        String sql = """
                SELECT ed.ExamDate,ed.LicenceId,l.LicenceClass,
                       (SELECT COUNT(*) FROM OfficialExamCandidate o
                        WHERE o.ExamDateId=ed.ExamDateId) OfficialCandidateCount
                FROM ExamDates ed
                JOIN Licence l ON l.LicenceId=ed.LicenceId
                WHERE ed.ExamDateId=?
                  AND ed.PoliceStatus=N'COMPLETED'
                  AND ed.Status<>N'Cancelled'
                  AND NOT EXISTS(
                      SELECT 1 FROM Exam e WHERE e.SourceExamDateId=ed.ExamDateId
                  )
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getSourceExamDateId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Danh sách CSGT chưa được ban hành, đã hủy hoặc đã được tạo phiên thi.");
                }
                s.setExamDate(rs.getDate("ExamDate"));
                s.setLicenceId(rs.getInt("LicenceId"));
                s.setLicenseCode(rs.getString("LicenceClass"));
                int officialCount = rs.getInt("OfficialCandidateCount");
                if (officialCount < 1
                        || officialCount > TentativeExamDatePolicy.MAX_REGISTRATIONS) {
                    throw new IllegalArgumentException("Danh sách chính thức phải có từ 1 đến "
                            + TentativeExamDatePolicy.MAX_REGISTRATIONS
                            + " thí sinh. Hiện có " + officialCount + " thí sinh.");
                }
            }
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    private static int createSection(Connection c, int examId, int licenceId,
            String sectionType) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO ExamSection(SectionType,LicenceId,DurationMinutes,ExamId) "
                + "VALUES(?,?,1,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sectionType);
            ps.setInt(2, licenceId);
            ps.setInt(3, examId);
            ps.executeUpdate();
            return generatedId(ps, "Không lấy được mã phần thi " + sectionType);
        }
    }

    private static void copyOfficialCandidates(Connection c, SessionDTO session,
            int examId, int theorySectionId, int practicalSectionId) throws SQLException {
        List<OfficialCandidateRow> rows = new ArrayList<>();
        String sourceSql = """
                SELECT o.CandidateNumber,o.FullName,o.DateOfBirth,
                       o.PhoneNumber,o.Email,o.GovernmentIdNumber,o.SourceUnitCode,o.SourceUnitName,
                       o.ExamParticipationType,er.ProfileId,COALESCE(er.IsRetake,0) IsRetake,
                       COALESCE(p.Sex,0) Sex,p.Address,portrait.DocumentUrl PhotoImageUrl
                FROM OfficialExamCandidate o
                LEFT JOIN ExamRegistration er ON er.ExamRegistrationId=o.ExamRegistrationId
                LEFT JOIN Profile p ON p.ProfileId=er.ProfileId
                OUTER APPLY (
                    SELECT TOP 1 d.DocumentUrl
                    FROM Document d
                    JOIN DocumentType dt ON dt.DocumentTypeId=d.DocumentTypeId
                    WHERE d.ProfileId=p.ProfileId
                      AND (UPPER(dt.[Type]) LIKE '%PORTRAIT%' OR dt.[Type] LIKE N'%chân dung%')
                    ORDER BY d.DocumentId DESC
                ) portrait
                WHERE o.ExamDateId=?
                ORDER BY TRY_CONVERT(int,o.CandidateNumber),o.CandidateNumber,o.OfficialExamCandidateId
                """;
        try (PreparedStatement ps = c.prepareStatement(sourceSql)) {
            ps.setInt(1, session.getSourceExamDateId());
            try (ResultSet rs = ps.executeQuery()) {
                int fallbackNumber = 1;
                while (rs.next()) {
                    String candidateNumber = rs.getString("CandidateNumber");
                    if (candidateNumber == null || candidateNumber.isBlank()) {
                        candidateNumber = String.format("%03d", fallbackNumber);
                    }
                    fallbackNumber++;
                    int profileId = rs.getInt("ProfileId");
                    rows.add(new OfficialCandidateRow(
                            rs.wasNull() ? null : profileId,
                            candidateNumber,
                            rs.getString("FullName"),
                            rs.getDate("DateOfBirth"),
                            rs.getString("PhoneNumber"),
                            rs.getString("Email"),
                            rs.getString("GovernmentIdNumber"),
                            rs.getBoolean("Sex"),
                            rs.getString("Address"),
                            rs.getString("PhotoImageUrl"),
                            rs.getString("SourceUnitCode"),
                            rs.getString("SourceUnitName"),
                            rs.getString("ExamParticipationType"),
                            rs.getBoolean("IsRetake")));
                }
            }
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Danh sách CSGT chưa có thí sinh để tạo phiên thi.");
        }

        String candidateSql = """
                INSERT INTO Candidate
                  (CandidateNumber,FullName,DateOfBirth,PhoneNumber,Email,Sex,
                   GovernmentIdNumber,Address,TakeTheory,TakeLayout,TakeNo,
                   ReasonForTaking,PhotoImageUrl,SourceUnitCode,SourceUnitName)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        String officialRegistrationSql = """
                INSERT INTO ExamRegistration
                  (RegistrationStatus,Notes,ProfileId,LicenceId,IsRetake)
                VALUES(N'OfficialScheduled',CONCAT(N'#EXAM_ID#',?,N'#'),?,?,?)
                """;
        String enrollmentSql = """
                INSERT INTO ExamEnrollment(CandidateId,ExamId,ExamRegistrationId)
                VALUES(?,?,?)
                """;
        String sectionSql = """
                INSERT INTO ExamEnrollmentSection(ExamEnrollmentId,ExamSectionId,[Status])
                VALUES(?,?,'Pending')
                """;
        try (PreparedStatement candidatePs = c.prepareStatement(candidateSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement officialRegistrationPs = c.prepareStatement(
                     officialRegistrationSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement enrollmentPs = c.prepareStatement(enrollmentSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement sectionPs = c.prepareStatement(sectionSql)) {
            for (OfficialCandidateRow row : rows) {
                candidatePs.setString(1, row.candidateNumber);
                candidatePs.setString(2, row.fullName);
                candidatePs.setDate(3, row.dateOfBirth);
                candidatePs.setString(4, row.phoneNumber);
                candidatePs.setString(5, row.email);
                candidatePs.setBoolean(6, row.sex);
                candidatePs.setString(7, row.governmentIdNumber);
                candidatePs.setString(8, row.address);
                candidatePs.setBoolean(9, row.takeTheory());
                candidatePs.setBoolean(10, row.takePractical());
                candidatePs.setInt(11, row.isRetake() ? 2 : 1);
                candidatePs.setString(12, row.examReason(session.getLicenseCode()));
                candidatePs.setString(13, row.photoImageUrl);
                candidatePs.setString(14, row.sourceUnitCode);
                candidatePs.setString(15, row.sourceUnitName);
                candidatePs.executeUpdate();
                int candidateId = generatedId(candidatePs, "Không tạo được thí sinh "
                        + row.governmentIdNumber);

                Integer officialRegistrationId = null;
                if (row.profileId != null) {
                    officialRegistrationPs.setInt(1, examId);
                    officialRegistrationPs.setInt(2, row.profileId);
                    officialRegistrationPs.setInt(3, session.getLicenceId());
                    officialRegistrationPs.setBoolean(4, row.isRetake());
                    officialRegistrationPs.executeUpdate();
                    officialRegistrationId = generatedId(officialRegistrationPs,
                            "Không tạo được đăng ký kỳ thi chính thức cho " + row.governmentIdNumber);
                }

                enrollmentPs.setInt(1, candidateId);
                enrollmentPs.setInt(2, examId);
                if (officialRegistrationId == null) enrollmentPs.setNull(3, Types.INTEGER);
                else enrollmentPs.setInt(3, officialRegistrationId);
                enrollmentPs.executeUpdate();
                int enrollmentId = generatedId(enrollmentPs, "Không ghi danh được thí sinh "
                        + row.governmentIdNumber);

                if (row.takeTheory()) {
                    sectionPs.setInt(1, enrollmentId);
                    sectionPs.setInt(2, theorySectionId);
                    sectionPs.executeUpdate();
                }

                if (row.takePractical()) {
                    sectionPs.setInt(1, enrollmentId);
                    sectionPs.setInt(2, practicalSectionId);
                    sectionPs.executeUpdate();
                }

            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE RegistrationDates SET IsActive=0 "
                + "WHERE ExamDateId=? AND IsActive=1")) {
            ps.setInt(1, session.getSourceExamDateId());
            ps.executeUpdate();
        }
    }

    private static int generatedId(PreparedStatement ps, String message) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) return keys.getInt(1);
        }
        throw new SQLException(message);
    }

    private static final class OfficialCandidateRow {
        private final Integer profileId;
        private final String candidateNumber;
        private final String fullName;
        private final java.sql.Date dateOfBirth;
        private final String phoneNumber;
        private final String email;
        private final String governmentIdNumber;
        private final boolean sex;
        private final String address;
        private final String photoImageUrl;
        private final String sourceUnitCode;
        private final String sourceUnitName;
        private final String examParticipationType;
        private final boolean isRetake;

        private OfficialCandidateRow(Integer profileId, String candidateNumber,
                String fullName, java.sql.Date dateOfBirth, String phoneNumber, String email,
                String governmentIdNumber, boolean sex, String address, String photoImageUrl,
                String sourceUnitCode, String sourceUnitName, String examParticipationType,
                boolean isRetake) {
            this.profileId = profileId;
            this.candidateNumber = candidateNumber;
            this.fullName = fullName;
            this.dateOfBirth = dateOfBirth;
            this.phoneNumber = phoneNumber;
            this.email = email;
            this.governmentIdNumber = governmentIdNumber;
            this.sex = sex;
            this.address = address;
            this.photoImageUrl = photoImageUrl;
            this.sourceUnitCode = sourceUnitCode;
            this.sourceUnitName = sourceUnitName;
            this.examParticipationType = examParticipationType;
            this.isRetake = isRetake;
        }

        private boolean takeTheory() {
            return !"PRACTICAL_ONLY".equalsIgnoreCase(examParticipationType);
        }

        private boolean takePractical() {
            return true;
        }

        private boolean isRetake() {
            return isRetake || "PRACTICAL_ONLY".equalsIgnoreCase(examParticipationType);
        }

        private String examReason(String licenceClass) {
            return takeTheory()
                    ? "Thi lý thuyết và thực hành hạng " + licenceClass
                    : "Thi lại thực hành hạng " + licenceClass;
        }
    }
    private static java.sql.Timestamp startTimestamp(SessionDTO s){return java.sql.Timestamp.valueOf(s.getExamDate().toLocalDate().atTime(s.getShiftStartTime().toLocalTime()));}
    private static String buildCode(Connection c,SessionDTO s)throws SQLException{String prefix=s.getLicenseCode().toUpperCase()+"-"+s.getExamDate().toString().replace("-","");try(PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM Exam WHERE ExamCode LIKE ?")){ps.setString(1,prefix+"%");try(ResultSet rs=ps.executeQuery()){int n=rs.next()?rs.getInt(1):0;return n==0?prefix:prefix+"-"+(n+1);}}}
    private static void bind(PreparedStatement ps,List<?> p)throws SQLException{for(int i=0;i<p.size();i++)ps.setObject(i+1,p.get(i));}
    private static RuntimeException dbError(SQLException e){return new IllegalStateException("Không thể xử lý dữ liệu phiên thi: "+e.getMessage(),e);}
    private static void rollback(Connection c){try{c.rollback();}catch(Exception ignored){}}
    private static void auto(Connection c){try{c.setAutoCommit(true);}catch(Exception ignored){}}
}
