package service;

import dto.ExaminerSlotDTO;
import dto.UserDTO;
import model.ExaminerSchedule;
import model.User;
import java.util.List;

public interface ExaminerSlotQueryService {
    List<ExaminerSlotDTO> toDtoList(List<ExaminerSchedule> schedules);
    ExaminerSlotDTO toDto(ExaminerSchedule schedule);
    List<UserDTO> toUserDtoList(List<User> users);
}
