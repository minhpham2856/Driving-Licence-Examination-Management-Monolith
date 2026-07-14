package auth.service.impl;

import auth.dao.ProfileDAO;
import auth.dao.UserDAO;
import auth.dao.impl.ProfileDAOImpl;
import auth.dao.impl.UserDAOImpl;
import auth.dto.ServiceResult;
import auth.dto.StaffAccountViewDTO;
import auth.dto.UpdateProfileDTO;
import auth.dto.UserDTO;
import auth.service.ProfileService;
import shared.enums.ErrorType;
import shared.model.Profile;
import shared.model.User;

public class ProfileServiceImpl implements ProfileService {

    private final ProfileDAO profileDAO;
    private final UserDAO userDAO;

    public ProfileServiceImpl() {
        this(new ProfileDAOImpl(), new UserDAOImpl());
    }

    public ProfileServiceImpl(ProfileDAO profileDAO, UserDAO userDAO) {
        this.profileDAO = profileDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Profile getByUserId(int userId) {
        if (userId <= 0) {
            return null;
        }
        return profileDAO.getByUserId(userId);
    }

    @Override
    public StaffAccountViewDTO getAccountView(int userId) {
        StaffAccountViewDTO view = new StaffAccountViewDTO();
        if (userId <= 0) {
            return view;
        }
        User user = userDAO.getById(userId);
        if (user != null) {
            view.setUser(UserDTO.fromUser(user));
        }
        view.setProfile(profileDAO.getByUserId(userId));
        return view;
    }

    @Override
    public ServiceResult<Profile> updateMyProfile(int userId, UpdateProfileDTO input) {
        if (userId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Phiên đăng nhập không hợp lệ.");
        }
        if (input == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiếu dữ liệu cập nhật.");
        }

        String fullName = trimToNull(input.getFullName());
        String phone = trimToNull(input.getPhoneNumber());
        String address = trimToNull(input.getAddress());

        if (fullName == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Họ và tên không được để trống.");
        }
        if (phone == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số điện thoại không được để trống.");
        }
        if (!phone.matches("^0\\d{9,10}$")) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Số điện thoại không hợp lệ (10–11 số, bắt đầu bằng 0).");
        }

        Profile existing = profileDAO.getByUserId(userId);
        if (existing == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Chưa có hồ sơ cá nhân gắn với tài khoản này.");
        }

        Profile phoneOwner = profileDAO.getByPhoneNo(phone);
        if (phoneOwner != null && phoneOwner.getProfileId() != existing.getProfileId()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số điện thoại đã được sử dụng.");
        }

        existing.setFullName(fullName);
        existing.setPhoneNumber(phone);
        existing.setAddress(address);
        if (input.getDateOfBirth() != null) {
            existing.setDateOfBirth(input.getDateOfBirth());
        }
        if (input.getSex() != null) {
            existing.setSex(input.getSex());
        }

        if (!profileDAO.update(existing)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "Không lưu được hồ sơ. Vui lòng thử lại.");
        }
        return ServiceResult.ok(existing, "Đã cập nhật thông tin cá nhân.");
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
