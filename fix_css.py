import os
import re

web_dir = 'web'

replacements = [
    (re.compile(r'assets/css/forgot-password\.css'), r'assets/css/landing/forgot-password.css'),
    (re.compile(r'assets/css/login\.css'), r'assets/css/landing/login.css'),
    (re.compile(r'assets/css/register\.css'), r'assets/css/landing/register.css'),
    (re.compile(r'assets/css/landing\.css'), r'assets/css/landing/landing.css'),
    (re.compile(r'assets/css/license-categories\.css'), r'assets/css/landing/license-categories.css'),
    (re.compile(r'assets/css/process\.css'), r'assets/css/landing/process.css'),
    (re.compile(r'assets/css/exam-results\.css'), r'assets/css/exam/exam-results.css'),
]

for root, _, files in os.walk(web_dir):
    for file in files:
        if file.endswith('.jsp'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original = content
            for regex, repl in replacements:
                content = regex.sub(repl, content)
                
            if content != original:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Updated {filepath}")
