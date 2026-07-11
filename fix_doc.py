import os

path = r'src/java/service/impl/DocumentServiceImpl.java'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('c.getSex(),', 'c.getSex() != null ? c.getSex().getValue() : "",')
content = content.replace('c.getStatusLabel(),', 'c.getSectionStatus() != null ? c.getSectionStatus().getValue() : "",')
content = content.replace('c.isAbsent() ? "Có" : "Không"', '"Không"')
content = content.replace('c.isAbsent() ? "CA3" : "KhA''ng"', '"Không"')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
