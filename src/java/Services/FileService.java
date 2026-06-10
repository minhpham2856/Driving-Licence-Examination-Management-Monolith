package Services;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface FileService {

    void exportToExcel(String sheetName, List<String> headers, List<List<Object>> rows, OutputStream out)
            throws IOException;
}
