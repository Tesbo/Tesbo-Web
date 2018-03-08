package Execution;

import ReportBuilder.GetJsonData;
import framework.GetConfiguration;
import framework.ReportParser;
import framework.SuiteParser;
import framework.TestExecutor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestExecutionBuilder {

    public static JSONObject mainObj = new JSONObject();

    public static void main(String[] args) throws Exception {
        TestExecutionBuilder builder = new TestExecutionBuilder();
        ReportParser report = new ReportParser();
        long startTimeSuite = System.currentTimeMillis();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy|MM|dd HH:mm:ss");
        builder.mainObj.put("startTime", dtf.format(LocalDateTime.now()));

        builder.buildExecution();

        long stopTimeSuite = System.currentTimeMillis();
        builder.mainObj.put("endTime", dtf.format(LocalDateTime.now()));
        long elapsedTimeSuite = stopTimeSuite - startTimeSuite;

        builder.mainObj.put("totalTimeTaken", elapsedTimeSuite);
        System.out.println("Main : " + builder.mainObj);


        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        report.generateReportDir();
        report.writeJsonFile(builder.mainObj, builder.getbuildReportName());


    }


    public String getbuildReportName() {
        String newName;
        GetJsonData data = new GetJsonData();

        File buildHistory = new File("./htmlReport/Build History");

        JSONArray suiteFileList = new JSONArray();

        try (Stream<Path> paths = Files.walk(Paths.get(buildHistory.getAbsolutePath()))) {

            suiteFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {

        }

        if (suiteFileList.size() > 0) {

            File lastModifiedfile = data.getLastModifiedJsonFile(buildHistory.getAbsolutePath());

            String getFile = lastModifiedfile.getName();


            String getCount[] = getFile.split("_")[1].split(".json");

            newName = "buildResult_" + (Integer.parseInt(getCount[0]) + 1);

        } else {

            newName = "buildResult_1";
        }


        return newName;
    }


    public JSONArray buildExecutionQueueByTag() throws Exception {
        SuiteParser suiteParser = new SuiteParser();
        GetConfiguration config = new GetConfiguration();
        JSONArray completeTestObjectArray = new JSONArray();

        for (String tag : config.getTags()) {
            JSONObject testNameWithSuites = suiteParser.getTestNameByTag(tag);
            for (Object suiteName : testNameWithSuites.keySet()) {
                for (Object testName : ((JSONArray) testNameWithSuites.get(suiteName))) {
                    for (String browser : config.getBrowsers()) {
                        JSONObject completestTestObject = new JSONObject();
                        completestTestObject.put("testName", testName);
                        completestTestObject.put("tag", tag);
                        completestTestObject.put("suiteName", suiteName);
                        completestTestObject.put("browser", browser);
                        completeTestObjectArray.add(completestTestObject);
                    }
                }
            }
        }

        for (Object a : completeTestObjectArray) {
            /**
             * this use for troubleshoot
             */
            //System.out.println(a);
        }

        return completeTestObjectArray;
    }

    public void parallelBuilder(JSONArray testExecutionQueue) throws Exception {

        GetConfiguration config = new GetConfiguration();
        JSONObject parallelConfig = config.getParallel();
        int threadCount = 0;

        if (parallelConfig.get("status").toString().equals("true")) {
            threadCount = Integer.parseInt(parallelConfig.get("count").toString());
        } else {
            threadCount = 1;
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        System.out.println(testExecutionQueue.size());
        for (int i = 0; i < testExecutionQueue.size(); i++) {
            Runnable worker = new TestExecutor((JSONObject) testExecutionQueue.get(i));
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }


    /**
     * @return
     * @Description : run by suite name.
     */
    public JSONArray buildExecutionQueueBySuite() throws Exception {
        SuiteParser suiteParser = new SuiteParser();
        GetConfiguration config = new GetConfiguration();
        JSONArray completeTestObjectArray = new JSONArray();

        for (String suite : config.getSuite()) {
            JSONObject testNameWithSuites = suiteParser.getTestNameBySuite(suite);
            for (Object suiteName : testNameWithSuites.keySet()) {
                for (Object testName : ((JSONArray) testNameWithSuites.get(suiteName))) {
                    for (String browser : config.getBrowsers()) {
                        JSONObject completestTestObject = new JSONObject();
                        completestTestObject.put("testName", testName);
                        completestTestObject.put("suiteName", suiteName);
                        completestTestObject.put("browser", browser);
                        completeTestObjectArray.add(completestTestObject);
                    }
                }
            }
        }

        for (Object a : completeTestObjectArray) {
            /**
             * this use for troubleshoot
             */
            //System.out.println(a);
        }

        return completeTestObjectArray;
    }

    public void buildExecution() throws Exception {
        GetConfiguration config = new GetConfiguration();
        ReportParser report = new ReportParser();

        int tagSuiteCount = 0;

        /**
         * @Discription : Run by tag method.
         */
        try {
            ArrayList<String> taglist = config.getTags();
            if (taglist.isEmpty() || taglist.get(0).equals("")) {
                System.err.println("Please enter 'Tag' name.");
            } else {
                parallelBuilder(buildExecutionQueueByTag());
                report.reportForTag(mainObj);
            }
        } catch (NullPointerException ne) {
            tagSuiteCount++;
        }

        /**
         *@Discription : Run by suit Name.
         */
        try {
            ArrayList<String> suitelist = config.getSuite();
            if (suitelist.isEmpty() || suitelist.get(0).equals("")) {
                System.err.println("Please enter 'suite' name.");
            } else {
                parallelBuilder(buildExecutionQueueBySuite());
                report.reportForSuit(mainObj);
            }
        } catch (NullPointerException ne) {
            tagSuiteCount++;
        }

        if (tagSuiteCount == 2) {
            System.err.println("Please enter 'Tag name' or 'Suite name' to run test.");
        }
    }

}
