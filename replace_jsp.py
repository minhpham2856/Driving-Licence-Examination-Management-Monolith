import os
import re

path = r'web/views/examiner/components/candidate-list.jsp'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace status checks in table rows
content = re.sub(r'<c:when test="\$\{c\.status == \'done\'\}"><span class="examiner-tag examiner-tag--done">\$\{c\.statusLabel\}</span></c:when>\s*<c:when test="\$\{c\.status == \'awaiting\'\}"><span class="examiner-tag examiner-tag--awaiting">\$\{c\.statusLabel\}</span></c:when>\s*<c:when test="\$\{c\.status == \'testing\'\}"><span class="examiner-tag examiner-tag--testing">\$\{c\.statusLabel\}</span></c:when>\s*<c:when test="\$\{c\.status == \'absent\'\}"><span class="examiner-tag examiner-tag--fail">\$\{c\.statusLabel\}</span></c:when>\s*<c:when test="\$\{c\.status == \'suspended\'\}"><span class="examiner-tag examiner-tag--suspended">\$\{c\.statusLabel\}</span></c:when>\s*<c:otherwise><span class="examiner-tag examiner-tag--pending">\$\{c\.statusLabel\}</span></c:otherwise>', 
r'''<c:when test=""><span class="examiner-tag examiner-tag--done"></span></c:when>
                                                <c:when test=""><span class="examiner-tag examiner-tag--awaiting"></span></c:when>
                                                <c:when test=""><span class="examiner-tag examiner-tag--testing"></span></c:when>
                                                <c:otherwise><span class="examiner-tag examiner-tag--pending"></span></c:otherwise>''', content)

# 2nd block
content = re.sub(r'<c:when test="\$\{c\.status == \'done\'\}"><span class="examiner-tag examiner-tag--done">\$\{c\.statusLabel\}</span></c:when>\s*<c:when test="\$\{c\.status == \'awaiting\'\}"><span class="examiner-tag examiner-tag--awaiting">\$\{c\.statusLabel\}</span></c:when>\s*<c:when test="\$\{c\.status == \'testing\'\}"><span class="examiner-tag examiner-tag--testing">\$\{c\.statusLabel\}</span></c:when>\s*<c:when test="\$\{c\.status == \'absent\'\}"><span class="examiner-tag examiner-tag--fail">\$\{c\.statusLabel\}</span></c:when>\s*<c:when test="\$\{c\.status == \'suspended\'\}"><span class="examiner-tag examiner-tag--suspended">\$\{c\.statusLabel\}</span></c:when>\s*<c:otherwise><span class="examiner-tag examiner-tag--pending">\$\{c\.statusLabel\}</span></c:otherwise>', 
r'''<c:when test=""><span class="examiner-tag examiner-tag--done"></span></c:when>
                                            <c:when test=""><span class="examiner-tag examiner-tag--awaiting"></span></c:when>
                                            <c:when test=""><span class="examiner-tag examiner-tag--testing"></span></c:when>
                                            <c:otherwise><span class="examiner-tag examiner-tag--pending"></span></c:otherwise>''', content)

# Action eligible fields replacements
content = content.replace('', '')
content = content.replace('', 'false')
content = content.replace('', 'false')
content = content.replace('', 'false')
content = content.replace('', '')
content = content.replace('', '')
content = content.replace('', '')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Done")
