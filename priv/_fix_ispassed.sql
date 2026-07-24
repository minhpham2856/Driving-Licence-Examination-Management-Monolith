UPDATE er
SET er.IsPassed = CASE WHEN es.Score >= 80 THEN 1 ELSE 0 END
FROM ExamResult er
JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
WHERE sec.SectionType = NN'Thực hành trong hình';

SELECT TOP 10 c.CandidateNumber, er.IsPassed, es.Score
FROM ExamEnrollment ee
JOIN Candidate c ON c.CandidateId = ee.CandidateId
JOIN ExamResult er ON er.ExamEnrollmentId = ee.ExamEnrollmentId
JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
WHERE sec.SectionType = NN'Thực hành trong hình' AND es.Score >= 90
ORDER BY c.CandidateNumber;