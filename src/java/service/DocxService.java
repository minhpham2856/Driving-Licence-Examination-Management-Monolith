package service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface DocxService {

    void render(String templateClasspath, Map<String, Object> placeholders, OutputStream out) throws IOException;
}
