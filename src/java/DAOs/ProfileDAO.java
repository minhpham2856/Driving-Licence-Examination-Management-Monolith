package DAOs;

import Models.Profile;

/**
 * DAO cho thao tác với hồ sơ cá nhân (Profile) trong hệ thống.
 * Cung cấp các phương thức truy vấn hồ sơ theo mã, số CMND/CCCD, số điện thoại,
 * cùng với thêm mới và cập nhật thông tin hồ sơ.
 */
public interface ProfileDAO {

    /**
     * Lấy thông tin hồ sơ theo mã.
     *
     * @param id mã hồ sơ
     * @return Profile model, hoặc null nếu không tìm thấy
     */
    Profile getById(int id);

    /**
     * Lấy thông tin hồ sơ theo số CMND/CCCD.
     *
     * @param govIdNo số CMND/CCCD
     * @return Profile model, hoặc null nếu không tìm thấy
     */
    Profile getByGovIdNo(String govIdNo);

    /**
     * Lấy thông tin hồ sơ theo số điện thoại.
     *
     * @param phoneNo số điện thoại
     * @return Profile model, hoặc null nếu không tìm thấy
     */
    Profile getByPhoneNo(String phoneNo);

    /**
     * Thêm mới một hồ sơ cá nhân.
     *
     * @param profile đối tượng Profile chứa thông tin hồ sơ
     * @return true nếu thêm thành công
     */
    boolean insert(Profile profile);

    /**
     * Cập nhật thông tin hồ sơ cá nhân.
     *
     * @param profile đối tượng Profile chứa thông tin cập nhật
     * @return true nếu cập nhật thành công
     */
    boolean update(Profile profile);
}
