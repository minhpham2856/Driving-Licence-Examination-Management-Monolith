# Re-download Selenium + JUnit jars into test/lib (run from repo root).
$dest = Join-Path $PSScriptRoot "."
New-Item -ItemType Directory -Force -Path $dest | Out-Null
$v = "4.27.0"
$base = "https://repo1.maven.org/maven2"
$paths = @(
  "org/seleniumhq/selenium/selenium-api/$v/selenium-api-$v.jar",
  "org/seleniumhq/selenium/selenium-json/$v/selenium-json-$v.jar",
  "org/seleniumhq/selenium/selenium-chrome-driver/$v/selenium-chrome-driver-$v.jar",
  "org/seleniumhq/selenium/selenium-chromium-driver/$v/selenium-chromium-driver-$v.jar",
  "org/seleniumhq/selenium/selenium-remote-driver/$v/selenium-remote-driver-$v.jar",
  "org/seleniumhq/selenium/selenium-http/$v/selenium-http-$v.jar",
  "org/seleniumhq/selenium/selenium-manager/$v/selenium-manager-$v.jar",
  "org/seleniumhq/selenium/selenium-os/$v/selenium-os-$v.jar",
  "org/seleniumhq/selenium/selenium-support/$v/selenium-support-$v.jar",
  "com/google/guava/guava/33.3.1-jre/guava-33.3.1-jre.jar",
  "net/bytebuddy/byte-buddy/1.15.10/byte-buddy-1.15.10.jar",
  "org/apache/commons/commons-exec/1.4.0/commons-exec-1.4.0.jar",
  "dev/failsafe/failsafe/3.3.2/failsafe-3.3.2.jar",
  "com/google/errorprone/error_prone_annotations/2.28.0/error_prone_annotations-2.28.0.jar",
  "com/google/j2objc/j2objc-annotations/3.0.0/j2objc-annotations-3.0.0.jar",
  "org/checkerframework/checker-qual/3.43.0/checker-qual-3.43.0.jar",
  "com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar",
  "org/seleniumhq/selenium/selenium-java/$v/selenium-java-$v.jar",
  "junit/junit/4.13.2/junit-4.13.2.jar",
  "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
)
foreach ($p in $paths) {
  $file = Split-Path $p -Leaf
  $out = Join-Path $dest $file
  Write-Output "GET $file"
  Invoke-WebRequest -Uri "$base/$p" -OutFile $out
}
