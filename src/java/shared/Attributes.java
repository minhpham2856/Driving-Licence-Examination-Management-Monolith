package shared;

public final class Attributes {

    private Attributes() {
    }

    // Session attributes shared across modules
    public static final class Session {

        public static final String USER = "user";
        public static final String USER_PROFILE = "userProfile";
        public static final String SUCCESS_MESSAGE = "successMessage";
        public static final String ERROR_MESSAGE = "errorMessage";
        public static final String REGISTRATION_USERNAME = "registrationUsername";
        public static final String REGISTRATION_PASSWORD = "registrationPassword";
    }

    // Request attributes for page data and one-hop flash
    public static final class Request {

        public static final String ERROR = "error";
        public static final String SUCCESS = "success";
        public static final String MESSAGE = "message";
        public static final String MESSAGE_TYPE = "messageType";
        public static final String LICENCES = "licences";
        public static final String SEARCH_QUERY = "searchQuery";
        public static final String SORT_BY = "sortBy";
        public static final String SORT_DIR = "sortDir";
        public static final String CANDIDATE = "candidate";
        public static final String CANDIDATES = "candidates";
        public static final String PAGE_URL = "pageUrl";
        public static final String BACK_URL = "backUrl";
    }

    // Examiner session and request context
    public static final class Examiner {

        public static final String SCHEDULE = "examinerSchedule";
        public static final String ACTIVE_EXAM_ID = "activeExamId";
        public static final String EXAM_SECTION = "examSection";
        public static final String HAS_ACTIVE_EXAM = "examinerHasActiveExam";
        public static final String EXAM_MESSAGE = "examinerExamMessage";
        public static final String IS_THEORY = "isTheory";
        public static final String EXAM_SECTION_NAME = "examSectionName";
    }

    // Exam staff session and request context
    public static final class ExamStaff {

        public static final String SELECTED_EXAM_ID = "selectedExamId";
        public static final String CURRENT_EXAM = "currentExam";
        public static final String CANDIDATE_QUEUE = "candidateQueue";
    }
}
