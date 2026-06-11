package DAO;

import Models.LicenseType;
import java.util.List;

public interface LicenseTypeDAO {

    List<LicenseType> findAll();

    LicenseType findByCode(String licenseCode);
}
