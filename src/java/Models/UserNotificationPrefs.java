package Models;

/**
 * Tùy chọn thông báo & bảo mật của tài khoản {@link User}.
 * Map 1-1 với các cột smsNotify, emailNotify, twoFactorEnabled trên bảng [User].
 */
public class UserNotificationPrefs {

    private boolean smsNotify;
    private boolean emailNotify;
    private boolean twoFactorEnabled;

    public UserNotificationPrefs() {
        // Mặc định trùng constraint DB khi chưa có bản ghi cũ
        this.smsNotify = true;
        this.emailNotify = true;
        this.twoFactorEnabled = false;
    }

    public UserNotificationPrefs(boolean smsNotify, boolean emailNotify, boolean twoFactorEnabled) {
        this.smsNotify = smsNotify;
        this.emailNotify = emailNotify;
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public boolean isSmsNotify() {
        return smsNotify;
    }

    public void setSmsNotify(boolean smsNotify) {
        this.smsNotify = smsNotify;
    }

    public boolean isEmailNotify() {
        return emailNotify;
    }

    public void setEmailNotify(boolean emailNotify) {
        this.emailNotify = emailNotify;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }
}
