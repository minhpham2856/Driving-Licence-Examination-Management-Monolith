import java.nio.file.*;
import java.nio.charset.*;
import java.util.regex.*;
import java.io.File;

public class MojibakeFixer {
    public static void main(String[] args) throws Exception {
        Files.walk(Paths.get("src/java"))
             .filter(Files::isRegularFile)
             .filter(p -> p.toString().endsWith(".java"))
             .forEach(p -> {
                 try {
                     String content = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                     if (content.matches("(?s).*[ÃÄÆá].*")) {
                         // Find words containing mojibake triggers
                         Pattern pattern = Pattern.compile("[\\\\w\\\\p{Punct}À-ÿœŠšŸŽžƒˆ˜€‚„…†‡‰‹›‘’“”•–—™]*[ÃÄÆá][\\\\w\\\\p{Punct}À-ÿœŠšŸŽžƒˆ˜€‚„…†‡‰‹›‘’“”•–—™]*");
                         Matcher m = pattern.matcher(content);
                         StringBuffer sb = new StringBuffer();
                         boolean changed = false;
                         while (m.find()) {
                             String word = m.group();
                             try {
                                 // Try CP1252 conversion
                                 byte[] bytes = word.getBytes("windows-1252");
                                 String decoded = new String(bytes, StandardCharsets.UTF_8);
                                 // If decoded is valid and changed
                                 if (!decoded.contains("\uFFFD") && !decoded.equals(word) && !decoded.isEmpty()) {
                                     m.appendReplacement(sb, Matcher.quoteReplacement(decoded));
                                     changed = true;
                                 } else {
                                     m.appendReplacement(sb, Matcher.quoteReplacement(word));
                                 }
                             } catch (Exception e) {
                                 // Cannot encode in CP1252 (e.g. contains real Vietnamese chars)
                                 m.appendReplacement(sb, Matcher.quoteReplacement(word));
                             }
                         }
                         m.appendTail(sb);
                         if (changed) {
                             Files.write(p, sb.toString().getBytes(StandardCharsets.UTF_8));
                             System.out.println("Fixed mixed file: " + p);
                         }
                     }
                 } catch (Exception ex) {}
             });
    }
}
