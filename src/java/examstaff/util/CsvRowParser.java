package examstaff.util;

import java.util.ArrayList;
import java.util.List;

/** Tách một dòng CSV có hỗ trợ trường trong dấu ngoặc kép. */
public final class CsvRowParser {

    private CsvRowParser() {
    }

    public static String[] parseLine(String line) {
        if (line == null) {
            return new String[0];
        }
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        fields.add(current.toString().trim());
        return fields.toArray(new String[0]);
    }
}
