$files = Get-ChildItem src/java/controller/examiner/*.java

foreach ($f in $files) {
    $content = Get-Content $f -Raw
    
    # getCandidateCallData
    $content = [regex]::Replace($content, 'viewDataService\.attachToRequest\(request,\s*sessionId,\s*sbd,\s*search\);', 'java.util.Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd, search); for(java.util.Map.Entry<String, Object> e : data.entrySet()) request.setAttribute(e.getKey(), e.getValue());')
    $content = [regex]::Replace($content, 'viewDataService\.attachToRequest\(request,\s*sessionId,\s*sbd\);', 'java.util.Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd); for(java.util.Map.Entry<String, Object> e : data.entrySet()) request.setAttribute(e.getKey(), e.getValue());')
    
    # getAuditLogsData
    $content = [regex]::Replace($content, 'viewDataService\.attachAuditLogs\(request,\s*sessionId,\s*pageParam,\s*searchQuery\);', 'java.util.Map<String, Object> data = viewDataService.getAuditLogsData(sessionId, pageParam, searchQuery); for(java.util.Map.Entry<String, Object> e : data.entrySet()) request.setAttribute(e.getKey(), e.getValue());')
    $content = [regex]::Replace($content, 'viewDataService\.attachAuditLogs\(request,\s*sessionId,\s*pageParam\);', 'java.util.Map<String, Object> data = viewDataService.getAuditLogsData(sessionId, pageParam); for(java.util.Map.Entry<String, Object> e : data.entrySet()) request.setAttribute(e.getKey(), e.getValue());')
    
    # getPaperAnswersData
    $content = [regex]::Replace($content, 'viewDataService\.attachPaperAnswers\(request,\s*sessionId,\s*sbd,\s*request\.getContextPath\(\)\);', 'java.util.Map<String, Object> ansData = viewDataService.getPaperAnswersData(sessionId, sbd, request.getContextPath()); for(java.util.Map.Entry<String, Object> e : ansData.entrySet()) request.setAttribute(e.getKey(), e.getValue());')
    
    # getScoreEntryData
    $content = [regex]::Replace($content, 'viewDataService\.attachScoreEntry\(request,\s*sessionId,\s*sbd\);', 'java.util.Map<String, Object> data = viewDataService.getScoreEntryData(sessionId, sbd); for(java.util.Map.Entry<String, Object> e : data.entrySet()) request.setAttribute(e.getKey(), e.getValue());')
    
    # getResultDetailsEditData
    $content = [regex]::Replace($content, 'viewDataService\.attachResultDetailsEdit\(request,\s*sessionId,\s*sbd\);', 'java.util.Map<String, Object> data = viewDataService.getResultDetailsEditData(sessionId, sbd); for(java.util.Map.Entry<String, Object> e : data.entrySet()) request.setAttribute(e.getKey(), e.getValue());')
    
    # getViolationData
    $content = [regex]::Replace($content, 'viewDataService\.attachViolation\(request,\s*sessionId,\s*sbd\);', 'java.util.Map<String, Object> data = viewDataService.getViolationData(sessionId, sbd); for(java.util.Map.Entry<String, Object> e : data.entrySet()) request.setAttribute(e.getKey(), e.getValue());')
    
    # getDevicesData
    $content = [regex]::Replace($content, 'viewDataService\.attachDevices\(request,\s*sessionId,\s*search\);', 'java.util.Map<String, Object> data = viewDataService.getDevicesData(sessionId, search); for(java.util.Map.Entry<String, Object> e : data.entrySet()) request.setAttribute(e.getKey(), e.getValue());')
    
    Set-Content -Path $f -Value $content -Encoding UTF8
    
    # fix bom
    python -c "
import sys
filepath = r'$($f.FullName)'
with open(filepath, 'rb') as f2:
    content2 = f2.read()
if content2.startswith(b'\xef\xbb\xbf'):
    content2 = content2[3:]
with open(filepath, 'wb') as f2:
    f2.write(content2)
"
}
