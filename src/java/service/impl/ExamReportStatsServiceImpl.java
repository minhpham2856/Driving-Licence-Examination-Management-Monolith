package service.impl;

import dao.view.ReportInfractionViewDAO;
import dao.view.impl.ReportInfractionViewDAOImpl;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.ExamReportStatsDTO;
import service.ExamReportStatsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExamReportStatsServiceImpl implements ExamReportStatsService {

    private final ReportInfractionViewDAO infractionViewDAO = new ReportInfractionViewDAOImpl();

    @Override
    public ExamReportStatsDTO computeStats(List<ExamRegistrationDTO> candidates) {
        ExamReportStatsDTO stats = new ExamReportStatsDTO();
        List<ExamRegistrationDTO> qList = candidates != null ? candidates : List.of();
        stats.setTotalCandidates(qList.size());

        int passedCount = 0;
        int failedCount = 0;
        int absentCount = 0;
        int examCompletedCount = 0;
        int theoryCount = 0;
        int theoryPassed = 0;
        int theoryFailed = 0;
        int practicalCount = 0;
        int practicalPassed = 0;
        int practicalFailed = 0;
        int roadCount = 0;
        int roadPassed = 0;
        int roadFailed = 0;

        Map<String, LicenseAgg> licenseMap = new LinkedHashMap<>();
        for (ExamRegistrationDTO reg : qList) {
            String lic = normalizeLicense(reg.getLicenseCode());
            LicenseAgg agg = licenseMap.computeIfAbsent(lic, k -> new LicenseAgg(lic));
            agg.registered++;
            if (reg.isAbsent()) {
                absentCount++;
            }
            String tPass = reg.getTheoryPassed();
            if ("passed".equalsIgnoreCase(tPass)) {
                theoryCount++;
                theoryPassed++;
            } else if ("failed".equalsIgnoreCase(tPass)) {
                theoryCount++;
                theoryFailed++;
            }
            String pPass = reg.getPracticalPassed();
            if ("passed".equalsIgnoreCase(pPass)) {
                practicalCount++;
                practicalPassed++;
            } else if ("failed".equalsIgnoreCase(pPass)) {
                practicalCount++;
                practicalFailed++;
            }
            String rPass = reg.getRoadTestPassed();
            if ("passed".equalsIgnoreCase(rPass)) {
                roadCount++;
                roadPassed++;
            } else if ("failed".equalsIgnoreCase(rPass)) {
                roadCount++;
                roadFailed++;
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
        stats.setExamCompletedCount(examCompletedCount);
        stats.setPassRate(examCompletedCount > 0 ? ((double) passedCount / examCompletedCount) * 100.0 : 0.0);
        stats.setTheoryCount(theoryCount);
        stats.setTheoryPassed(theoryPassed);
        stats.setTheoryFailed(theoryFailed);
        stats.setPracticalCount(practicalCount);
        stats.setPracticalPassed(practicalPassed);
        stats.setPracticalFailed(practicalFailed);
        stats.setRoadCount(roadCount);
        stats.setRoadPassed(roadPassed);
        stats.setRoadFailed(roadFailed);

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

        LicenseAgg a1 = sumLicenseBucket(licenseMap, "A1");
        LicenseAgg a = sumLicenseBucket(licenseMap, "A");
        LicenseAgg b1 = licenseMap.getOrDefault("B1", new LicenseAgg("B1"));
        stats.setA1Count(a1.registered);
        stats.setA1Completed(a1.completed);
        stats.setA1Passed(a1.passed);
        stats.setA1Failed(a1.failed);
        stats.setACount(a.registered);
        stats.setACompleted(a.completed);
        stats.setAPassed(a.passed);
        stats.setAFailed(a.failed);
        stats.setB1Count(b1.registered);
        stats.setB1Completed(b1.completed);
        stats.setB1Passed(b1.passed);
        stats.setB1Failed(b1.failed);
        stats.setInfractions(infractionViewDAO.findTopInfractions(3));
        return stats;
    }

    private static String normalizeLicense(String licenseCode) {
        if (licenseCode == null || licenseCode.isBlank()) {
            return "N/A";
        }
        return licenseCode.trim().toUpperCase(Locale.ROOT);
    }

    private static LicenseAgg sumLicenseBucket(Map<String, LicenseAgg> map, String... codes) {
        LicenseAgg total = new LicenseAgg(codes[0]);
        for (String code : codes) {
            LicenseAgg part = map.get(code);
            if (part == null) {
                continue;
            }
            total.registered += part.registered;
            total.completed += part.completed;
            total.passed += part.passed;
            total.failed += part.failed;
        }
        return total;
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
