package service.impl;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import dao.CandidateAnswerDAO;
import dao.QuestionDAO;
import dao.TheoryPaperDAO;
import dao.impl.CandidateAnswerDAOImpl;
import dao.impl.QuestionDAOImpl;
import dao.impl.TheoryPaperDAOImpl;
import dto.ExaminerCandidateRowDTO;
import dto.ExaminerExportContext;
import dto.ExaminerExportPayload;
import dto.XmlExportTable;
import model.CandidateAnswer;
import model.Question;
import model.TheoryPaper;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import service.DocxService;
import service.ExaminerDataService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DocxServiceImpl implements DocxService {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");
    private static final int BLOCK_A_FROM = 1;
    private static final int BLOCK_A_TO = 20;
    private static final int BLOCK_B_FROM = 21;
    private static final int BLOCK_B_TO = 35;
    private static final String TEMPLATE_DIR = "/docx-template/examiner/";

    private static Configure poiTlConfig() {
        return Configure.builder().buildGramer("<<", ">>").build();
    }

    private final ExaminerDataService examinerDataService = new ExaminerDataServiceImpl();
    private final TheoryPaperDAO theoryPaperDAO = new TheoryPaperDAOImpl();
    private final CandidateAnswerDAO candidateAnswerDAO = new CandidateAnswerDAOImpl();
    private final QuestionDAO questionDAO = new QuestionDAOImpl();

    @Override
    public void render(String templateClasspath, Map<String, Object> placeholders, OutputStream out)
            throws IOException {
        try (InputStream in = DocxServiceImpl.class.getResourceAsStream(templateClasspath)) {
            if (in == null) {
                throw new IOException("Cannot find: " + templateClasspath);
            }
            Map<String, Object> safe = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                safe.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
            }
            XWPFTemplate.compile(in, poiTlConfig()).render(safe).write(out);
        }
    }

    public void renderBb1Theory(ExaminerExportContext ctx, int sbd, OutputStream out) throws IOException {
        ExaminerCandidateRowDTO candidate = findCandidateRow(ctx, sbd);
        String template = pickTemplate("BB1", candidate.getLicenceClass());
        if (template == null) {
            throw new IOException("Không tìm thấy mẫu BB1.");
        }
        render(template, buildBb1Placeholders(ctx, candidate), out);
    }

    public void renderBb2Layout(ExaminerExportContext ctx, int sbd, OutputStream out) throws IOException {
        ExaminerCandidateRowDTO candidate = findCandidateRow(ctx, sbd);
        String template = pickTemplate("BB2", candidate.getLicenceClass());
        if (template == null) {
            throw new IOException("Không tìm thấy mẫu BB2.");
        }
        render(template, buildBb2Placeholders(ctx, candidate), out);
    }

    public void renderBb3Road(ExaminerExportContext ctx, int sbd, OutputStream out) throws IOException {
        ExaminerCandidateRowDTO candidate = findCandidateRow(ctx, sbd);
        String template = pickTemplate("BB3", candidate.getLicenceClass());
        if (template == null) {
            throw new IOException("Không tìm thấy mẫu BB3.");
        }
        render(template, buildBb3Placeholders(ctx, candidate), out);
    }

    public void renderTableExport(ExaminerExportPayload payload, OutputStream out) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(14);
            titleRun.setText(payload.excelSheetName());

            if (payload.metadata() != null) {
                for (Map.Entry<String, Object> entry : payload.metadata().entrySet()) {
                    appendLine(document, entry.getKey() + ": " + format(entry.getValue()));
                }
            }
            if (payload.excelPreambleRows() != null) {
                for (List<Object> row : payload.excelPreambleRows()) {
                    StringBuilder line = new StringBuilder();
                    for (int i = 0; i < row.size(); i++) {
                        if (i > 0) {
                            line.append(" | ");
                        }
                        line.append(format(row.get(i)));
                    }
                    appendLine(document, line.toString());
                }
            }
            for (XmlExportTable table : payload.tables()) {
                appendLine(document, "");
                XWPFTable xwpfTable = document.createTable();
                XWPFTableRow headerRow = xwpfTable.getRow(0);
                for (int i = 0; i < table.headers().size(); i++) {
                    if (i > 0) {
                        headerRow.addNewTableCell();
                    }
                    headerRow.getCell(i).setText(table.headers().get(i));
                }
                for (List<Object> row : table.rows()) {
                    XWPFTableRow dataRow = xwpfTable.createRow();
                    for (int i = 0; i < row.size(); i++) {
                        dataRow.getCell(i).setText(format(row.get(i)));
                    }
                }
            }
            document.write(out);
        }
    }

    private Map<String, Object> buildBb1Placeholders(ExaminerExportContext ctx, ExaminerCandidateRowDTO candidate) {
        Map<String, Object> data = baseCandidatePlaceholders(ctx, candidate);
        data.put("A", buildTheoryAnswerBlock(candidate.getEnrollmentId(), BLOCK_A_FROM, BLOCK_A_TO));
        data.put("B", buildTheoryAnswerBlock(candidate.getEnrollmentId(), BLOCK_B_FROM, BLOCK_B_TO));
        data.put("SCORE", format(candidate.getScoreTheory()));
        boolean passed = "Đạt".equalsIgnoreCase(stringValue(candidate.getResultLabel()))
                || candidate.isPassed();
        data.put("P", passed ? "X" : "");
        data.put("F", passed ? "" : "X");
        return data;
    }

    private Map<String, Object> buildBb2Placeholders(ExaminerExportContext ctx, ExaminerCandidateRowDTO candidate) {
        Map<String, Object> data = baseCandidatePlaceholders(ctx, candidate);
        data.put("VNO", format(candidate.getVehicleName()));
        data.put("TIME", format(candidate.getExamDate()));
        data.put("RAND1", "");
        data.put("RAND2", "");
        data.put("RAND3", "");
        data.put("A", format(candidate.getExamScore()));
        return data;
    }

    private Map<String, Object> buildBb3Placeholders(ExaminerExportContext ctx, ExaminerCandidateRowDTO candidate) {
        Map<String, Object> data = buildBb2Placeholders(ctx, candidate);
        data.put("A", format(candidate.getScoreOnRoad()));
        return data;
    }

    private Map<String, Object> baseCandidatePlaceholders(ExaminerExportContext ctx, ExaminerCandidateRowDTO candidate) {
        Map<String, Object> data = new LinkedHashMap<>();
        TheoryPaper paper = loadTheoryPaper(candidate);
        String sessionName = ctx.slot() != null ? format(ctx.slot().getSessionName()) : "-";

        data.put("DEPT", "TP. HÀ NỘI");
        data.put("FNAME", format(candidate.getFullName()));
        data.put("EXAM", sessionName);
        data.put("PIC", "");
        data.put("DOB", format(candidate.getDob()));
        data.put("DATE", format(candidate.getExamDate()));
        data.put("IDNO", format(candidate.getGovernmentId()));
        data.put("START", formatTime(paper != null ? paper.getStartedAt() : null));
        data.put("CLASS", format(candidate.getLicenceClass()));
        data.put("END", formatTime(paper != null ? paper.getSubmittedAt() : null));
        data.put("CNO", format(candidate.getSbd()));
        data.put("TAKENO", "1");
        return data;
    }

    private ExaminerCandidateRowDTO findCandidateRow(ExaminerExportContext ctx, int sbd) throws IOException {
        ExaminerCandidateRowDTO row = examinerDataService.getCandidateViewRow(
                ctx.sessionId(), sbd, ctx.isTheory(), ctx.sectionName());
        if (row == null) {
            throw new IOException("Không tìm thấy thí sinh SBD " + sbd);
        }
        return row;
    }

    // Select template based on licence class
    private String pickTemplate(String documentCode, String licenceClass) {
        String cls = normalizeLicenceClass(licenceClass);
        if ("BB1".equals(documentCode)) {
            if ("A1".equals(cls) || "A".equals(cls) || "B1".equals(cls)) {
                return TEMPLATE_DIR + "BB1(A1-A-B1).docx";
            }
            return TEMPLATE_DIR + "BB1(B-C1-C-D1-D2-D).docx";
        }
        if ("BB2".equals(documentCode)) {
            if ("A1".equals(cls) || "A".equals(cls)) {
                return TEMPLATE_DIR + "BB2(A1-A).docx";
            }
            if ("B1".equals(cls)) {
                return TEMPLATE_DIR + "BB2(B1).docx";
            }
            return TEMPLATE_DIR + "BB2(B-C1-C-D1-D2-D).docx";
        }
        if ("BB3".equals(documentCode)) {
            return TEMPLATE_DIR + "BB3(B-C1-C-D1-D2-D).docx";
        }
        return null;
    }

    private static String normalizeLicenceClass(String licenceClass) {
        if (licenceClass == null || licenceClass.isBlank()) {
            return "B2";
        }
        String normalized = licenceClass.trim().toUpperCase(Locale.ROOT);
        if ("B".equals(normalized)) {
            return "B2";
        }
        return normalized;
    }

    private TheoryPaper loadTheoryPaper(ExaminerCandidateRowDTO candidate) {
        if (candidate.getEnrollmentId() <= 0) {
            return null;
        }
        return theoryPaperDAO.getByExamEnrollmentId(candidate.getEnrollmentId());
    }

    // Ghep dap an ly thuyet theo khoi cau (A: 1-20, B: 21-35).
    private String buildTheoryAnswerBlock(int enrollmentId, int fromQuestionNo, int toQuestionNo) {
        if (enrollmentId <= 0) {
            return "-";
        }
        TheoryPaper paper = theoryPaperDAO.getByExamEnrollmentId(enrollmentId);
        if (paper == null) {
            return "-";
        }
        List<CandidateAnswer> answers = candidateAnswerDAO.findByTheoryPaperId(paper.getTheoryPaperId());
        if (answers.isEmpty()) {
            return "-";
        }

        List<Integer> questionIds = new ArrayList<>();
        for (CandidateAnswer answer : answers) {
            questionIds.add(answer.getQuestionId());
        }
        Map<Integer, Integer> questionNoById = new HashMap<>();
        for (Question question : questionDAO.findByIds(questionIds)) {
            questionNoById.put(question.getQuestionId(), question.getQuestionNumber());
        }

        Map<Integer, String> answerByNo = new HashMap<>();
        for (CandidateAnswer answer : answers) {
            Integer questionNo = questionNoById.get(answer.getQuestionId());
            if (questionNo == null || questionNo < fromQuestionNo || questionNo > toQuestionNo) {
                continue;
            }
            String letter = answer.getAnswer();
            if (letter == null || letter.isBlank()) {
                letter = "-";
            }
            answerByNo.put(questionNo, letter.trim());
        }
        if (answerByNo.isEmpty()) {
            return "-";
        }
        List<Integer> questionNos = new ArrayList<>(answerByNo.keySet());
        Collections.sort(questionNos);
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < questionNos.size(); i++) {
            if (i > 0) {
                text.append(' ');
            }
            int no = questionNos.get(i);
            text.append(no).append('.').append(answerByNo.get(no));
        }
        return text.toString();
    }

    private String formatTime(Timestamp value) {
        if (value == null) {
            return "-";
        }
        synchronized (TIME_FMT) {
            return TIME_FMT.format(value);
        }
    }

    private static void appendLine(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text == null ? "" : text);
    }

    private String format(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString().trim();
        return text.isEmpty() ? "" : text;
    }

    private static String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
