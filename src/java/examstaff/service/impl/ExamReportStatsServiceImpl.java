package examstaff.service.impl;

import examstaff.dao.view.ReportInfractionViewDAO;
import examstaff.dao.view.impl.ReportInfractionViewDAOImpl;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.ExamReportStatsDTO;
import examstaff.service.ExamReportStatsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Implementation: tính thống kê kết quả kỳ thi cho báo cáo. */
public class ExamReportStatsServiceImpl implements ExamReportStatsService {

    private final ReportInfractionViewDAO infractionViewDAO = new ReportInfractionViewDAOImpl();

    /**
     * Tổng hợp chỉ số báo cáo (số thí sinh, đỗ/trượt, …) theo danh sách đã lọc.
     *
     * @param candidates danh sách thí sinh trong báo cáo
     * @param examId     mã kỳ thi
     * @return DTO thống kê báo cáo
     */
    @Override
    public ExamReportStatsDTO computeStats(List<ExamRegistrationDTO> candidates, int examId) {
        ExamReportStatsDTO stats = new ExamReportStatsDTO();
        List<ExamRegistrationDTO> qList = candidates != null ? candidates : List.of();
        stats.setTotalCandidates(qList.size());

        int passedCount = 0;
        int failedCount = 0;
        int absentCount = 0;
        int suspendedCount = 0;
        int examCompletedCount = 0;
        int theoryCount = 0;
        int theoryPassed = 0;
        int theoryFailed = 0;
        int practicalCount = 0;
        int practicalPassed = 0;
        int practicalFailed = 0;

        Map<String, LicenseAgg> licenseMap = new LinkedHashMap<>();
        for (ExamRegistrationDTO reg : qList) {
            String lic = normalizeLicense(reg.getLicenseCode());
            LicenseAgg agg = licenseMap.computeIfAbsent(lic, k -> new LicenseAgg(lic));
            agg.registered++;
            if (reg.isSuspended()) {
                suspendedCount++;
                continue;
            }
            if (reg.isAbsent() && !reg.isSuspended()) {
                absentCount++;
                continue;
            }
            String tPass = reg.getTheoryPassed();
            if (!reg.skipsTheory()) {
                if ("passed".equalsIgnoreCase(tPass)) {
                    theoryCount++;
                    theoryPassed++;
                } else if ("failed".equalsIgnoreCase(tPass)) {
                    theoryCount++;
                    theoryFailed++;
                }
            }
            if (!reg.skipsPractical()) {
                String pPass = reg.getPracticalPassed();
                if ("passed".equalsIgnoreCase(pPass)) {
                    practicalCount++;
                    practicalPassed++;
                } else if ("failed".equalsIgnoreCase(pPass)) {
                    practicalCount++;
                    practicalFailed++;
                }
            }
            if (!reg.isExamFinished()) {
                continue;
            }
            examCompletedCount++;
            agg.completed++;
            if (reg.isFinalPass()) {
                passedCount++;
                agg.passed++;
            } else {
                failedCount++;
                agg.failed++;
            }
        }

        stats.setPassedCount(passedCount);
        stats.setFailedCount(failedCount);
        stats.setAbsentCount(absentCount);
        stats.setSuspendedCount(suspendedCount);
        stats.setExamCompletedCount(examCompletedCount);
        stats.setPassRate(examCompletedCount > 0 ? ((double) passedCount / examCompletedCount) * 100.0 : 0.0);
        stats.setTheoryCount(theoryCount);
        stats.setTheoryPassed(theoryPassed);
        stats.setTheoryFailed(theoryFailed);
        stats.setPracticalCount(practicalCount);
        stats.setPracticalPassed(practicalPassed);
        stats.setPracticalFailed(practicalFailed);

        List<Map<String, Object>> licenseStats = new ArrayList<>();
        for (LicenseAgg agg : licenseMap.values()) {
            Map<String, Object> row = new HashMap<>();
            row.put("code", agg.code);
            row.put("registered", agg.registered);
            row.put("completed", agg.completed);
            row.put("passed", agg.passed);
            row.put("failed", agg.failed);
            licenseStats.add(row);
        }
        stats.setLicenseStats(licenseStats);
        stats.setInfractions(infractionViewDAO.findTopInfractions(examId, 3));
        return stats;
    }

    /** Chuẩn hoá mã hạng bằng; blank thành N/A. */
    private static String normalizeLicense(String licenseCode) {
        if (licenseCode == null || licenseCode.isBlank()) {
            return "N/A";
        }
        return licenseCode.trim().toUpperCase(Locale.ROOT);
    }

    private static final class LicenseAgg {
        final String code;
        int registered;
        int completed;
        int passed;
        int failed;

        LicenseAgg(String code) {
            this.code = code;
        }
    }
}
