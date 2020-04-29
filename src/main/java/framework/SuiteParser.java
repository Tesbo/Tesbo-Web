package framework;

import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Exception.*;
import org.json.simple.JSONObject;

public class SuiteParser {
    TesboLogger tesboLogger = new TesboLogger();
    private static final Logger log = LogManager.getLogger(SuiteParser.class);

    /**
     * @auther: Ankit Mistry
     * @lastModifiedBy:
     * @param fileName : File name with extension e.g. smoke.suite
     * @return whole file content as String buffer
     */
    public StringBuffer readSuiteFile(String fileName) {

        GetConfiguration configuration = new GetConfiguration();
        BufferedReader br = null;
        FileReader fr = null;
        StringBuffer tests = new StringBuffer();

        try {
            fr = new FileReader(configuration.getSuitesDirectory() + "/" + fileName+".suite");
            br = new BufferedReader(fr);
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                tests.append(sCurrentLine + "\n");
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        } finally {

            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (Exception ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                tesboLogger.testFailed(sw.toString());
                log.error(sw.toString());
            }
        }
        return tests;
    }

    /**
     * @auther: Ankit Mistry
     * @lastModifiedBy:
     * @param testsFile
     * @return
     */
    public JSONArray getTestNameFromSuiteFile(StringBuffer testsFile) {

        String[] allLines = testsFile.toString().split("[\\r\\n]+");
        JSONArray testName = new JSONArray();
        for (int i = 0; i < allLines.length; i++) {
            if(!allLines[i].equals("")) {
                String comment = String.valueOf(allLines[i].charAt(0)) + String.valueOf(allLines[i].charAt(1));
                if ((allLines[i].toLowerCase().startsWith("test")) && (!comment.equals("//"))) {
                    if (!allLines[i].trim().startsWith("Test: ")) {
                        String errorMsg="Please write valid keyword or step for this \"" + allLines[i] + "\" on suite file";
                        log.error(errorMsg);
                        throw new TesboException(errorMsg);
                    }
                    testName.add(allLines[i].trim());
                } else {
                    if (!comment.equals("//")) {
                        log.error("Define only test name in '.suite' file");
                        throw new TesboException("Define only test name in '.suite' file");
                    }
                }
            }

        }

        validationForDuplicateTestInSuiteFile(testName);
        return testName;
    }

    public void validationForDuplicateTestInSuiteFile(JSONArray testNameList)
    {
        for(int i=0;i<testNameList.size();i++){
            for(int j=i+1;j<testNameList.size();j++){
                if(testNameList.get(i).equals(testNameList.get(j))){
                    String errorMsg="'"+testNameList.get(i)+"' test is define multiple time on same suite file";
                    log.error(errorMsg);
                    throw new TesboException(errorMsg);
                }
            }
        }
    }

    /**
     * @auther: Ankit Mistry
     * @lastModifiedBy:
     * @param suiteFileName
     * @return
     */
    public JSONObject getTestsFileNameUsingTestName(String suiteFileName) {

        JSONArray tampSuiteTestNameList=new JSONArray();
        StringBuffer suiteFile= readSuiteFile(suiteFileName);
        JSONArray suiteTestNameList=getTestNameFromSuiteFile(suiteFile);

        tampSuiteTestNameList.addAll(suiteTestNameList);
        JSONObject testNameWithTestsFileName=new JSONObject();
        TestsFileParser testsFileParser=new TestsFileParser();
        GetConfiguration getConfiguration=new GetConfiguration();
        JSONArray testsFileNameList= testsFileParser.getTestFiles(getConfiguration.getTestsDirectory());



        //=======================================================================

        int i=1;
        for(Object suiteTestName:suiteTestNameList){
            JSONArray testList=new JSONArray();
            String testFileName = "";
            for(Object testsFileName:testsFileNameList){
                File name = new File(testsFileName.toString());
                StringBuffer testsFileDetails = testsFileParser.readTestsFile(name.getName());
                JSONArray testName= testsFileParser.getTestNameByTestsFile(testsFileDetails);
                for(Object test:testName) {
                    if(test.toString().equals(suiteTestName.toString().split(":")[1].trim())) {
                        boolean isExistInTampSuiteTestNameList=false;
                        for(Object tampSuiteTest:tampSuiteTestNameList){
                            if(suiteTestName.equals(tampSuiteTest)){
                                isExistInTampSuiteTestNameList=true;
                                break;
                            }
                        }
                        if(!isExistInTampSuiteTestNameList){
                            String errorMsg="'"+suiteTestName+"' test is found multiple time on tests file";
                            log.error(errorMsg);
                            throw new TesboException(errorMsg);
                        }
                        testFileName=name.getName()+"_"+i++;
                        testList.add(test.toString().trim());
                        tampSuiteTestNameList.remove(suiteTestName);
                    }

                }

            }
            if(!testList.isEmpty()) {
                testNameWithTestsFileName.put(testFileName, testList);
            }
        }

        //=======================================================================

        if(!tampSuiteTestNameList.isEmpty()){
            String testList="";
            for(Object test:tampSuiteTestNameList){
                if(testList.equals("")){ testList=test.toString();
                }else { testList=testList+", "+test.toString(); }
            }
            String errorMsg="'"+testList+"' test is not found in any tests file";
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
        return testNameWithTestsFileName;
    }

    /**
     * @auther: Ankit Mistry
     * @lastModifiedBy:
     * @param directory
     * @return give all the file inside a directory
     */
    public JSONArray getSuiteFiles(String directory)  {

        JSONArray suiteFileList = new JSONArray();
        boolean flag=false;
        String file=null;
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {

            suiteFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
            for(Object testsFilePath:suiteFileList) {
                String[] testsFile=testsFilePath.toString().split("\\.");
                if (testsFile.length == 2 && (!testsFile[1].equalsIgnoreCase("suite"))) {
                        flag=true;
                        if(file==null)
                            file="'."+testsFile[1]+"'";
                        else
                            file+=", '."+testsFile[1]+"'";
                }
            }
            if(flag){
                String errorMsg=file+" file found in suite directory";
                log.error(errorMsg);
                tesboLogger.errorLog(errorMsg);
                throw (new NoSuchFileException(""));
            }
        } catch (Exception e) {
            if(flag){
                log.error("Message : Please create only '.suite' file in tests directory.");
                tesboLogger.testFailed("Message : Please create only '.suite' file in tests directory.");
            }
            else {
                String errorMsg="'" + directory + "' no files found on your location.";
                log.error("Message : Please Enter valid directory path.");
                log.error(errorMsg);
                tesboLogger.testFailed("Message : Please Enter valid directory path.");
                tesboLogger.testFailed(errorMsg);
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                tesboLogger.testFailed(sw.toString());
                log.error(sw.toString());
            }
            try {
                throw e;
            } catch (IOException ie) {
                StringWriter sw = new StringWriter();
                ie.printStackTrace(new PrintWriter(sw));
                tesboLogger.testFailed(sw.toString());
                log.error(sw.toString());
            }
        }
        return suiteFileList;
    }
}
