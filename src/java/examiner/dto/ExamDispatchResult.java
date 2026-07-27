package examiner.dto;

import shared.enums.SectionType;

// Examiner module exam dispatch result.
public class ExamDispatchResult {

    private int examAreaId;
    private SectionType sectionType;
    private Integer promotedCandidateNumber;
    private int queueLoad;

    public int getExamAreaId() {
        return examAreaId;
    }

    public void setExamAreaId(int examAreaId) {
        this.examAreaId = examAreaId;
    }

    public SectionType getSectionType() {
        return sectionType;
    }

    public void setSectionType(SectionType sectionType) {
        this.sectionType = sectionType;
    }

    public Integer getPromotedCandidateNumber() {
        return promotedCandidateNumber;
    }

    public void setPromotedCandidateNumber(Integer promotedCandidateNumber) {
        this.promotedCandidateNumber = promotedCandidateNumber;
    }

    public int getQueueLoad() {
        return queueLoad;
    }

    public void setQueueLoad(int queueLoad) {
        this.queueLoad = queueLoad;
    }
}
