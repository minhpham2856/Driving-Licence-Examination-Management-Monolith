package controller.staff.exam;

import dbconnection.DBContext;

import dto.exam.ExamRegistrationDTO;

import jakarta.servlet.http.HttpServletRequest;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Locale;

import java.util.Map;

public final class ReportStatsHelper {

    private ReportStatsHelper() {

    }

    // populate report attributes
    public static void populateReportAttributes(HttpServletRequest request, List<ExamRegistrationDTO> qList) {

        int totalCandidates = qList != null ? qList.size() : 0;

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

        if (qList != null) {

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

        }

        double passRate = examCompletedCount > 0

                ? ((double) passedCount / examCompletedCount) * 100.0

                : 0.0;

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

        request.setAttribute("totalCandidates", totalCandidates);

        request.setAttribute("examCompletedCount", examCompletedCount);

        request.setAttribute("passedCount", passedCount);

        request.setAttribute("failedCount", failedCount);

        request.setAttribute("absentCount", absentCount);

        request.setAttribute("passRate", passRate);

        request.setAttribute("licenseStats", licenseStats);

        LicenseAgg a1 = sumLicenseBucket(licenseMap, "A1");
        LicenseAgg a = sumLicenseBucket(licenseMap, "A");
        LicenseAgg b1 = licenseMap.getOrDefault("B1", new LicenseAgg("B1"));

        request.setAttribute("a1Count", a1.registered);
        request.setAttribute("a1Completed", a1.completed);
        request.setAttribute("a1Passed", a1.passed);
        request.setAttribute("a1Failed", a1.failed);

        request.setAttribute("aCount", a.registered);
        request.setAttribute("aCompleted", a.completed);
        request.setAttribute("aPassed", a.passed);
        request.setAttribute("aFailed", a.failed);

        request.setAttribute("b1Count", b1.registered);
        request.setAttribute("b1Completed", b1.completed);
        request.setAttribute("b1Passed", b1.passed);
        request.setAttribute("b1Failed", b1.failed);

        request.setAttribute("theoryCount", theoryCount);

        request.setAttribute("theoryPassed", theoryPassed);

        request.setAttribute("theoryFailed", theoryFailed);

        request.setAttribute("practicalCount", practicalCount);

        request.setAttribute("practicalPassed", practicalPassed);

        request.setAttribute("practicalFailed", practicalFailed);

        request.setAttribute("roadCount", roadCount);

        request.setAttribute("roadPassed", roadPassed);

        request.setAttribute("roadFailed", roadFailed);

        request.setAttribute("infractions", loadTopInfractions());

    }
    // normalize license

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

    // Tai top infractions
    }

    private static List<Map<String, Object>> loadTopInfractions() {

        List<Map<String, Object>> infractions = new ArrayList<>();

        try (Connection conn = new DBContext().getConnection();

             PreparedStatement ps = conn.prepareStatement(

                     "select top 3 sd.[Reason] as deductionReason, count(*) as countVal "

                             + "from Score_Deduction sdd "

                             + "join ScoreDeduction sd on sd.ScoreDeductionId = sdd.ScoreDeductionId "

                             + "group by sd.[Reason] "

                             + "order by countVal desc")) {

            try (ResultSet rs = ps.executeQuery()) {

                int totalInfractions = 0;

                while (rs.next()) {

                    Map<String, Object> map = new HashMap<>();

                    map.put("reason", rs.getString("deductionReason"));

                    int cnt = rs.getInt("countVal");

                    map.put("count", cnt);

                    totalInfractions += cnt;

                    infractions.add(map);

                }

                for (Map<String, Object> map : infractions) {

                    int cnt = (int) map.get("count");

                    double pct = totalInfractions > 0 ? ((double) cnt / totalInfractions) * 100.0 : 0.0;

                    map.put("percentage", pct);

                }

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return infractions;

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
