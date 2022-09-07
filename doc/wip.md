# Tika work and notes

* https://tika.apache.org/
* https://cwiki.apache.org/confluence/display/tika/TikaEval
* corpora-dev@tika.apache.org
* https://issues.apache.org/jira/projects/TIKA/

* https://www.youtube.com/watch?v=OoEHQUlu16w

# Scratchpad

Base64.getDecoder().decode(

Base64.getUrlEncoder().encodeToString(

java.lang.NoSuchMethodError: 'java.lang.String org.apache.commons.compress.archivers.ArchiveStreamFactory.detect(java.io.InputStream)'

	at org.apache.tika.detect.zip.DefaultZipContainerDetector.detectArchiveFormat(DefaultZipContainerDetector.java:124)
	at org.apache.tika.detect.zip.DefaultZipContainerDetector.detect(DefaultZipContainerDetector.java:170)
	at org.apache.tika.detect.CompositeDetector.detect(CompositeDetector.java:85)
	at org.apache.tika.parser.AutoDetectParser.parse(AutoDetectParser.java:142)
	at org.freeeed.main.FileProcessorTest.doTestTikaExtractText(FileProcessorTest.java:89)
	at org.freeeed.main.FileProcessorTest.testTikaExtractText(FileProcessorTest.java:81)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at junit.framework.TestCase.runTest(TestCase.java:176)
	at junit.framework.TestCase.runBare(TestCase.java:141)
	at junit.framework.TestResult$1.protect(TestResult.java:122)
	at junit.framework.TestResult.runProtected(TestResult.java:142)
	at junit.framework.TestResult.run(TestResult.java:125)
	at junit.framework.TestCase.run(TestCase.java:129)
	at junit.framework.TestSuite.runTest(TestSuite.java:252)
	at junit.framework.TestSuite.run(TestSuite.java:247)
	at org.junit.internal.runners.JUnit38ClassRunner.run(JUnit38ClassRunner.java:86)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:69)
	at com.intellij.rt.junit.IdeaTestRunner$Repeater$1.execute(IdeaTestRunner.java:38)
	at com.intellij.rt.execution.junit.TestsRepeater.repeat(TestsRepeater.java:11)
	at com.intellij.rt.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:35)
	at com.intellij.rt.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:235)
	at com.intellij.rt.junit.JUnitStarter.main(JUnitStarter.java:54)


 <exclusions>
        <exclusion>  <!-- declare the exclusion here -->
          <groupId>sample.ProjectB</groupId>
          <artifactId>Project-B</artifactId>
        </exclusion>
      </exclusions> 
