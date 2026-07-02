package dao;

import model.user.Profile;

public interface ProfileDAO {

    Profile getById(int id);

    Profile getByGovIdNo(String govIdNo);

    Profile getByPhoneNo(String phoneNo);

    boolean insert(Profile profile);

    boolean update(Profile profile);

    /** Lấy hồ sơ cá nhân theo UserId của tài khoản đăng nhập. */
    Profile getByUserId(int userId);
}
