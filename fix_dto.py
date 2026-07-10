import os
import re

files_to_check = [
    r"src\java\service\impl\PhotoServiceImpl.java",
    r"src\java\service\impl\ExamViewServiceImpl.java",
    r"src\java\service\impl\CallServiceImpl.java"
]

for file_path in files_to_check:
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Replacements
    content = re.sub(r'(\w+)\.getCandidate\(\)\.getPhotoImageUrl\(\)', r'\1.getPhotoUrl()', content)
    content = re.sub(r'(\w+)\.getCandidate\(\)\.setPhotoImageUrl\((.*?)\)', r'\1.setPhotoUrl(\2)', content)
    content = re.sub(r'(\w+)\.getCandidate\(\)\.getCandidateId\(\)', r'\1.getId()', content)
    content = re.sub(r'(\w+)\.getEnrollment\(\)\.getExamEnrollmentId\(\)', r'\1.getExamEnrollmentId()', content)
    content = re.sub(r'(\w+)\.getEnrollment\(\)\.getExamDeviceId\(\)', r'\1.getExamDeviceId()', content)
    content = re.sub(r'(\w+)\.getEnrollment\(\) != null \? (\w+)\.getEnrollment\(\)\.getExamEnrollmentId\(\) : 0', r'\1.getExamEnrollmentId()', content)
    content = re.sub(r'(\w+)\.getEnrollment\(\) != null \? (\w+)\.getEnrollment\(\)\.getExamDeviceId\(\) : null', r'\1.getExamDeviceId()', content)
    content = re.sub(r'reg == null \|\| reg\.getEnrollment\(\) == null', r'reg == null', content)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
print('Done!')
