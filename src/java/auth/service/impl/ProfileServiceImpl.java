package auth.service.impl;

import auth.dao.ProfileDAO;
import auth.dao.UserDAO;
import auth.dto.ServiceResult;
import auth.dto.AccountDTO;
import auth.dto.UpdateProfileDTO;
import auth.dto.UserDTO;
import auth.service.ProfileService;
import auth.dao.impl.ProfileDAOImpl;
import auth.dao.impl.UserDAOImpl;
import auth.util.ValidationUtil;
import shared.enums.ErrorType;
import shared.model.Profile;
import shared.model.User;

public class ProfileServiceImpl implements ProfileService {

    private final ProfileDAO profileDAO;
    private final UserDAO userDAO;

    public ProfileServiceImpl() {
        this.profileDAO = new ProfileDAOImpl();
        this.userDAO = new UserDAOImpl();
    }

    @Override
    public Profile getByUserId(int userId) {
        if (userId <= 0) {
            return null;
        }
        return profileDAO.getByUserId(userId);
    }

    @Override
    public AccountDTO getAccount(int userId) {
        AccountDTO account = new AccountDTO();
        if (userId <= 0) {
            return account;
        }
        User user = userDAO.getById(userId);
        if (user != null) {
            account.setUser(UserDTO.fromUser(user));
        }
        account.setProfile(profileDAO.getByUserId(userId));
        return account;
    }

    @Override
    public ServiceResult<Profile> updateProfile(int userId, UpdateProfileDTO input) {
        if (userId <= 0) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Phiên đăng nhập không hợp lệ.");
        }
        if (input == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Thiếu dữ liệu cập nhật.");
        }

        String username = trimToNull(input.getUsername());
        String email = trimToNull(input.getEmail());
        String fullName = trimToNull(input.getFullName());
        String phone = trimToNull(input.getPhoneNumber());
        String govId = trimToNull(input.getGovernmentIdNumber());
        String address = trimToNull(input.getAddress());

        if (username == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Tên đăng nhập không được để trống.");
        }
        if (username.length() < 3 || username.length() > 50) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Tên đăng nhập phải từ 3–50 ký tự.");
        }
        if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Tên đăng nhập chỉ gồm chữ, số, dấu chấm, gạch dưới hoặc gạch ngang.");
        }
        if (email == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Email không được để trống.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Địa chỉ email không hợp lệ.");
        }
        if (fullName == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Họ và tên không được để trống.");
        }
        if (phone == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số điện thoại không được để trống.");
        }
        if (!phone.matches("^0\\d{9,10}$")) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số điện thoại không hợp lệ.");
        }
        if (govId == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số căn cước không được để trống.");
        }
        if (!ValidationUtil.isValidCccd(govId)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số căn cước phải gồm đúng 12 chữ số.");
        }

        User existingUser = userDAO.getById(userId);
        if (existingUser == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Không tìm thấy tài khoản.");
        }

        Profile existing = profileDAO.getByUserId(userId);
        if (existing == null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Chưa có hồ sơ cá nhân gắn với tài khoản này.");
        }

        User usernameOwner = userDAO.getByUsername(username);
        if (usernameOwner != null && usernameOwner.getUserId() != userId) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Tên đăng nhập đã được sử dụng.");
        }

        User emailOwner = userDAO.getByEmail(email);
        if (emailOwner != null && emailOwner.getUserId() != userId) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Email đã được sử dụng.");
        }

        Profile phoneOwner = profileDAO.getByPhoneNo(phone);
        if (phoneOwner != null && phoneOwner.getProfileId() != existing.getProfileId()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số điện thoại đã được sử dụng.");
        }

        Profile govOwner = profileDAO.getByGovIdNo(govId);
        if (govOwner != null && govOwner.getProfileId() != existing.getProfileId()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số căn cước đã được sử dụng.");
        }

        boolean credentialsChanged = !username.equals(existingUser.getUsername())
                || !email.equalsIgnoreCase(existingUser.getEmail() != null ? existingUser.getEmail() : "");
        if (credentialsChanged && !userDAO.updateCredentials(userId, username, email)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "Không lưu được thông tin tài khoản. Vui lòng thử lại.");
        }

        existing.setFullName(fullName);
        existing.setPhoneNumber(phone);
        existing.setGovernmentIdNumber(govId);
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
