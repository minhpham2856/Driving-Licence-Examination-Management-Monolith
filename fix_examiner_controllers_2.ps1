$files = Get-ChildItem src/java/controller/examiner/*.java

foreach ($f in $files) {
    $content = Get-Content $f -Raw
    
    $content = [regex]::Replace($content, 'viewDataService\.attachToRequest\(request,\s*sessionId,\s*sbd,\s*null\);', 'java.util.Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd, null); for(java.util.Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());')
    $content = [regex]::Replace($content, 'viewDataService\.attachAuditLogs\(request,\s*sessionId,\s*pageParam,\s*null\);', 'java.util.Map<String, Object> data = viewDataService.getAuditLogsData(sessionId, pageParam, null); for(java.util.Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());')
    $content = [regex]::Replace($content, 'viewDataService\.attachDevices\(request,\s*sessionId,\s*null\);', 'java.util.Map<String, Object> data = viewDataService.getDevicesData(sessionId, null); for(java.util.Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());')
    $content = [regex]::Replace($content, 'java\.util\.Map\.Entry<String,\s*Object>\s*e\s*:', 'java.util.Map.Entry<String, Object> mapEntry :')
    
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
