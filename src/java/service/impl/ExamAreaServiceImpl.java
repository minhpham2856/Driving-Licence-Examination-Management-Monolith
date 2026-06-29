package service.impl;
import dto.*;
import model.*;

import dao.ExamAreaDAO;
import dao.impl.ExamAreaDAOImpl;
import model.ExamArea;
import service.ExamAreaService;

import java.util.List;

public class ExamAreaServiceImpl implements ExamAreaService {
    
    private final ExamAreaDAO dao = new ExamAreaDAOImpl();

    @Override
    public ExamArea getById(int id) {
        return dao.getById(id);
    }

    @Override
    public List<ExamArea> search(String keyword, String type) {
        return dao.search(keyword, type);
    }

    @Override
    public List<ExamArea> getActiveTheoryRooms() { return dao.getActiveTheoryRooms(); }

    @Override
    public int countAll() {
        return dao.countAll();
    }

    @Override
    public SaveResult save(ExamArea area, int adminUserId) {
        if (area.getAreaName() == null || area.getAreaName().trim().isEmpty()) {
            return new SaveResult(false, "Vui lÃ²ng nháº­p tÃªn khu vá»±c thi.", area.getExamAreaId());
        }
        if (area.getAreaType() == null || area.getAreaType().trim().isEmpty()) {
            return new SaveResult(false, "Vui lÃ²ng chá»n loáº¡i khu vá»±c.", area.getExamAreaId());
        }
        if (area.getLocation() == null || area.getLocation().trim().isEmpty()) {
            return new SaveResult(false, "Vui lÃ²ng nháº­p Ä‘á»‹a chá»‰ khu vá»±c.", area.getExamAreaId());
        }
        if (area.getCapacity() <= 0) {
            return new SaveResult(false, "Sá»©c chá»©a pháº£i lá»›n hÆ¡n 0.", area.getExamAreaId());
        }

        boolean isEdit = area.getExamAreaId() > 0;
        if (isEdit) {
            
            boolean ok = dao.update(area);
            if (ok) {
                return new SaveResult(true, "da cap nhat khu vuc \"" + area.getAreaName() + "\".", area.getExamAreaId());
            } else {
                return new SaveResult(false, "cap nhat khu vuc that bai", area.getExamAreaId());
            }
        } else {
            
            
            int newId = dao.insert(area);
            boolean ok = newId > 0;
            if (ok) {
                return new SaveResult(true, "ÄÃ£ thÃªm khu vá»±c \"" + area.getAreaName() + "\".", newId);
            } else {
                return new SaveResult(false, "them khu vuc that bai", 0);
            }
        }
    }

    @Override
    public DeleteResult delete(int id, int adminUserId) {
        ExamArea area = dao.getById(id);
        if (area == null) {
            return new DeleteResult(false, "Khu vá»±c khÃ´ng tá»“n táº¡i.");
        }
        boolean ok = id > 0 && dao.delete(id);
        if (ok) {
            return new DeleteResult(true, "ÄÃ£ xÃ³a khu vá»±c thi.");
        } else {
            return new DeleteResult(false, "KhÃ´ng thá»ƒ xÃ³a khu vá»±c nÃ y (cÃ³ thá»ƒ Ä‘ang Ä‘Æ°á»£c sá»­ dá»¥ng bá»Ÿi phÃ²ng/thiáº¿t bá»‹/ká»³ thi).");
        }
    }
}

