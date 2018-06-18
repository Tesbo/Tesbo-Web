package DataCollector;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BuildReportDataObject {


    static JSONArray browserArray = new JSONArray();
    JSONObject mainReportObject = new JSONObject();

    public void addDataInMainObject(String browser, String suite, String testName, JSONObject testResultObject) {

        checkForTheBrowser(browser);
        checkForTheSuite(browser, suite);
        checkForTheTest(browser, suite, testResultObject);

        mainReportObject.put("browser", browserArray);

        System.out.println(mainReportObject);
    }


    public void checkForTheBrowser(String browserName) {

        JSONObject browserObject = new JSONObject();
        JSONObject suiteObject = new JSONObject();
        JSONArray suiteArray = new JSONArray();

        if (browserArray.size() == 0) {

            suiteObject.put("suits", suiteArray);
            browserObject.put(browserName, suiteObject);
            browserArray.add(browserObject);

        } else {

            synchronized (this) {
                int size = browserArray.size();
                System.out.println(size);
                boolean browserFlag = false;
                for (int i = 0; i < size; i++) {
                    try {

                        JSONObject suiteTempObject = (JSONObject) ((JSONObject) browserArray.get(i)).get(browserName);

                        if (suiteTempObject.size() > 0) {
                            browserFlag = true;
                            break;
                        }

                    } catch (NullPointerException e) {
                    }
                }

                if (!browserFlag) {
                    suiteObject.put("suits", suiteArray);
                    browserObject.put(browserName, suiteObject);
                    browserArray.add(browserObject);


                }

            }
        }

    }


    public JSONObject getBrowserObject(String browserName) {
        JSONObject suiteObject = null;
        int size = browserArray.size();
        System.out.println(size);
        boolean browserFlag = false;

        for (int i = 0; i < size; i++) {
            try {

                suiteObject = (JSONObject) ((JSONObject) browserArray.get(i)).get(browserName);

                if (suiteObject.size() > 0) {
                    break;
                }

            } catch (NullPointerException e) {
            }
        }
        return suiteObject;
    }

    public JSONObject setBrowserObject(String browserName, JSONArray suiteArray) {
        JSONObject suiteObject = null;
        int size = browserArray.size();
        JSONObject browserObject = new JSONObject();

        System.out.println(size);
        boolean browserFlag = false;

        int i;
        for (i = 0; i < size; i++) {
            try {
                suiteObject = (JSONObject) ((JSONObject) browserArray.get(i)).get(browserName);

                if (suiteObject.size() > 0) {
                    browserFlag = true;
                    break;
                }

            } catch (NullPointerException e) {
            }
        }
        if (!browserFlag) {

            suiteObject.put("suits", suiteArray);
            browserObject.put(browserName, suiteObject);
            browserArray.set(i, browserObject);

        }

        return suiteObject;
    }


    public void checkForTheSuite(String browserName, String suite) {

        System.out.println(getBrowserObject(browserName));
        JSONObject suiteObject = getBrowserObject(browserName);

        JSONArray suitesArray = (JSONArray) suiteObject.get("suits");
        JSONArray testArray = new JSONArray();
        JSONObject individualSuiteObject = new JSONObject();


        if (suitesArray.size() == 0) {

            individualSuiteObject.put("suiteName", suite);
            individualSuiteObject.put("tests", testArray);
            individualSuiteObject.put("totalTime", "");
            individualSuiteObject.put("totalFailed", "");
            individualSuiteObject.put("totalPassed", "");
            suitesArray.add(individualSuiteObject);

        } else {

            synchronized (this) {
                int size = suitesArray.size();
                System.out.println("SuiteArray" + size);
                boolean suiteFlag = false;
                for (int i = 0; i < size; i++) {
                    try {

                        JSONObject suiteTempObject = (JSONObject) ((JSONObject) suitesArray.get(i));

                        System.out.println(((JSONObject) suitesArray.get(i)).get("suiteName"));

                        System.out.println("Suite Count" + ((JSONObject) suitesArray.get(i)).size());

                        if (suiteTempObject.get("suiteName").equals(suite)) {
                            suiteFlag = true;
                            break;
                        }

                    } catch (NullPointerException e) {
                    }
                }

                if (!suiteFlag) {
                    individualSuiteObject.put("tests", testArray);
                    individualSuiteObject.put("suiteName", suite);
                    individualSuiteObject.put("totalTime", "");
                    individualSuiteObject.put("totalFailed", "");
                    individualSuiteObject.put("totalPassed", "");

                    suitesArray.add(individualSuiteObject);
                }
            }

        }
        setBrowserObject(browserName, suitesArray);
    }




    public void checkForTheTest(String browserName, String suite, JSONObject testResult) {


        JSONObject suiteObject = getBrowserObject(browserName);

        JSONArray suitesArray = (JSONArray) suiteObject.get("suits");

        JSONArray newSuiteArray = new JSONArray();

        int size = suitesArray.size();

        double totalPassed = 0, totalFailed = 0;
        double totalTime = 0.0;
        JSONObject tempSuiteObject = null;
        int suiteCount = 0;
        JSONArray testArray = null;


        //lopping for finding suite
        for (int i = 0; i < size; i++) {


            tempSuiteObject = (JSONObject) suitesArray.get(i);
            String suiteName = tempSuiteObject.get("suiteName").toString();


            if (suiteName.equals(suite)) {
                testArray = (JSONArray) tempSuiteObject.get("tests");

                testArray.add(testResult);
                suiteCount = i;

            }




        }

        try {
            tempSuiteObject.put("totalTime", totalTime);
            tempSuiteObject.put("totalFailed", totalFailed);
            tempSuiteObject.put("totalPassed", totalPassed);
            tempSuiteObject.put("tests", testArray);
            newSuiteArray.set(suiteCount, tempSuiteObject);
        } catch (Exception e) {
            newSuiteArray.add(tempSuiteObject);

        }



        setBrowserObject(browserName, newSuiteArray);


    }


}



