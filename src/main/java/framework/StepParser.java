package framework;

import RandomLibrary.RandLibrary;
import Selenium.Commands;
import logger.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import Exception.TesboException;

import static org.assertj.core.api.Assertions.assertThat;

public class StepParser {


    Logger logger = new Logger();
    DataDrivenParser dataDrivenParser = new DataDrivenParser();
    public static String screenShotURL=null;

    public String parseStep(WebDriver driver, JSONObject test, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();

        if (!step.toLowerCase().contains("{") && !step.toLowerCase().contains("}") && !step.toLowerCase().contains("print") && !step.toString().contains("random"))
            logger.stepLog(step.replace("@",""));


        //Clicks
        if (step.toLowerCase().contains("click") && !(step.toLowerCase().contains("right") || step.toLowerCase().contains("double") || step.toLowerCase().contains("and hold") || step.toLowerCase().contains("from list")) ) {

            if (step.toLowerCase().contains("from list")) {
                clickOnElementFromList(driver,test,step);
            }
            else if(step.toLowerCase().contains("offset")){
                clickOnOffset(driver,test,step);
            }
            else {
                cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).click();
            }
        }

        // Click And Hold
        if (step.toLowerCase().contains("click") && step.toLowerCase().contains("and hold")) {
            /**
             * Step: click and hold @element1
             * And
             * Step: click @element1 and hold
             */
            cmd.clickAndHold(driver,cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))));
        }

        //Right Click
        if (step.toLowerCase().contains("right click")) {
            cmd.rightClick(driver, cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))));
        }

        //Double Click
        if (step.toLowerCase().contains("double click")) {

            cmd.doubleClick(driver, cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))));
        }

        //Print
        if (step.toLowerCase().contains("print")) {
            /**
             * Step: print "User should redirect into the login page "
             * Step: print @elementName
             */
            logger.stepLog(printStep(driver,step,test));
        }

        //Capture Screenshot
        if (step.toLowerCase().contains("capture screenshot")) {
            /**
             * Step: Capture Screenshot of @elementName
             */
            if(step.toLowerCase().contains("screenshot of") && step.toLowerCase().contains("@")) {
                screenShotURL = cmd.screenshotElement(driver, cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))),test.get("suiteName").toString(), test.get("testName").toString());
                logger.stepLog("Screenshot: " + screenShotURL);
            }
            /**
             * Step: capture screenshot
             */
            else if(step.toLowerCase().contains("screenshot")) {
                screenShotURL = cmd.captureScreenshot(driver, test.get("suiteName").toString(), test.get("testName").toString());
                logger.stepLog("Screenshot: " + screenShotURL);
            }
        }

        //clear cookies and cache
        if (step.toLowerCase().contains("clear cookies") || step.toLowerCase().contains("clear cache")) {
            cmd.deleteAllCookies(driver);
        }

        //Press Key
        if (step.toLowerCase().contains("press")) {
            pressKey(driver, test, step);
        }

        //Sendkeys
        if (step.toLowerCase().contains("enter") && (!step.toLowerCase().contains("press"))) {
            if (step.toLowerCase().contains("random")) {step= randomStepParse(driver,test,step); }
            else {
                cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).sendKeys(parseTextToEnter(test, step));
            }
        }

        //Upload File
        if (step.toLowerCase().contains("upload") && step.toLowerCase().contains("file")) {
            /*
            Step: Upload File 'filePath' @element
            */
            cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).sendKeys(parseTextToEnter(test, step));
        }

        //Get Page Source
        if (step.toLowerCase().contains("get page") && step.toLowerCase().contains("source")) {
            /*
            Step: Get Page Source
            */
            cmd.getPageSource(driver);
        }

        // Get URL
        if (step.toLowerCase().contains("url")) {
            driver.get(parseTextToEnter(test, step));
        }

        //Switch
        if (step.toLowerCase().contains("switch")) {
            switchFunction(driver, test, step);
        }

        //navigate
        if (step.toLowerCase().contains("navigate")) {
            navigateFunction(driver, step);
        }

        //scroll
        if (step.toLowerCase().contains("scroll")) {
            scrollFunction(driver, test.get("suiteName").toString(), step);
        }

        //pause
        if (step.toLowerCase().contains("pause")) {
            pauseFunction(driver, test.get("suiteName").toString(), step);
        }

        //select
        if (step.toLowerCase().contains("select")) {
            selectFunction(driver, test, step);
        }

        //Window Minimize, maximize and resize
        if (step.toLowerCase().contains("window") && !(step.toLowerCase().contains("close"))) {
            if (step.toLowerCase().contains("resize")) {
                windowResize(step,driver);
            }
            if (step.toLowerCase().contains("minimize")) {
                windowMinimize(driver);
            }
            if (step.toLowerCase().contains("maximize")) {
                windowMaximize(driver);
            }
        }

        //Mouse Hover
        if (step.toLowerCase().contains("mouse hover")) {
            /**
             * Step: Mouse Hover @element
             */
            cmd.mouseHover(driver,cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))));
        }

        //Drag and Drop
        if (step.toLowerCase().contains("drag") && step.toLowerCase().contains("and drop")) {
            /**
             * Step: Drag and Drop @Element_1 to @element_2 ,
             * Step: Drag and Drop @ele1 to ele2
             * and
             * Step: Drag @Element_1 to @Element2 and drop
             */
            // Not working
            dragAndDropElement(driver,test,step);
        }

        //Clear
        if (step.toLowerCase().contains("clear") && !(step.toLowerCase().contains("cookies") | step.toLowerCase().contains("cache"))) {
            cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).clear();
        }

        //Close Window
        if (step.toLowerCase().contains("close window")) {
            /**
             * Step : close window.
             */
            cmd.closeWindow(driver);
        }

        logger.testPassed("Passed");
        return step;

    }
    public String randomStepParse(WebDriver driver, JSONObject test, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();
        RandLibrary randLibrary = new RandLibrary();
        String textToEnter="";
        if (step.toLowerCase().contains("firstname")) {textToEnter=randLibrary.firstName(); }

        if (step.toLowerCase().contains("email")) {textToEnter=randLibrary.eMail(); }

        if (step.toLowerCase().contains("username")){textToEnter=randLibrary.userName(); }

        if (step.toLowerCase().contains("mobilenumber")){textToEnter=randLibrary.number(); }

        if (step.toLowerCase().contains("lastname")){textToEnter=randLibrary.LastName(); }

        if (step.toLowerCase().contains("age")){textToEnter=randLibrary.AgeAdult(); }

        if (step.toLowerCase().contains("birthday")){textToEnter= randLibrary.Birthday(); }

        if (step.toLowerCase().contains("debitcard")){textToEnter=randLibrary.DebitCardNo(); }

        if (step.toLowerCase().contains("expirydate")){textToEnter=randLibrary.ExpiryDate(); }

        if (step.toLowerCase().contains("cvv")){textToEnter=randLibrary.cvvNo(); }

        if (step.toLowerCase().contains("country")){textToEnter=randLibrary.Country(); }

        if (step.toLowerCase().contains("city")){textToEnter=randLibrary.city(); }

        if (step.toLowerCase().contains("postcode")){textToEnter=randLibrary.postcode(); }

        if (step.toLowerCase().contains("street")){textToEnter=randLibrary.street(); }

        if (step.toLowerCase().contains("emoji")){textToEnter=randLibrary.emoji(); }

        if (step.toLowerCase().contains("lorem")){textToEnter=randLibrary.lorem(); }

        if (step.toLowerCase().contains("maritalstatus")){textToEnter=randLibrary.maritalStatus(); }

        if (step.toLowerCase().contains("gender")){textToEnter=randLibrary.gender(); }

        if (step.toLowerCase().contains("state")){textToEnter=randLibrary.state(); }

        if (step.toLowerCase().contains("fulladdress")){textToEnter=randLibrary.fullAddress(); }

        if (step.toLowerCase().contains("domain")){textToEnter=randLibrary.internetDomain(); }

        if (step.toLowerCase().contains("gstno")){textToEnter=randLibrary.GSTNo(); }

        if (step.toLowerCase().contains("panno")){textToEnter=randLibrary.PANNo(); }

        if (step.toLowerCase().contains("companyname")){textToEnter=randLibrary.companyName(); }

        if (step.toLowerCase().contains("fullname")){textToEnter=randLibrary.fullName(); }

        if (step.toLowerCase().contains("password")){textToEnter=randLibrary.password(); }

        if (step.toLowerCase().contains("idno")){textToEnter=randLibrary.IDNo(); }

        if (step.toLowerCase().contains("passport")){textToEnter=randLibrary.passport(); }

        if (step.toLowerCase().contains("houseno")){textToEnter=randLibrary.houseNo(); }

        if (step.toLowerCase().contains("bankacno")){textToEnter=randLibrary.bankACNo(); }

        if (step.toLowerCase().contains("cardtype")){textToEnter=randLibrary.cardname(); }

       /* if (step.toLowerCase().contains("number"))
        { cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).sendKeys(randLibrary.RandomNo()); }

        if (step.toLowerCase().contains("alpha"))
        { cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).sendKeys(randLibrary.RandomAlpha()); }

        if (step.toLowerCase().contains("numAlpha"))
        { cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).sendKeys(randLibrary.RandomNoAlpha()); }
*/
        String stepText="";
        if(!textToEnter.equals("")){
            int startPoint = 0;
            int endPoint = 0;
            startPoint = step.indexOf("'") + 1;
            endPoint = step.lastIndexOf("'");
            stepText = step.substring(startPoint, endPoint);
            logger.stepLog(step.replace(stepText, textToEnter).replace("@",""));
            if (step.toLowerCase().contains("birthday")){
                cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).sendKeys((CharSequence) textToEnter);

            }
            else {
                cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).sendKeys(textToEnter);
            }
        }
        if(!stepText.equals("")) {
            return step.replace(stepText, textToEnter);
        }
        else {
            return step;
        }
    }

    public void switchFunction(WebDriver driver, JSONObject test, String step) throws Exception {

        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();


        try {
            //Step :  switch to active Element
            if (step.toLowerCase().contains("active element")) {
                cmd.switchToActiveElement(driver);
            }

            /**
             * Switch to alert.
             */
            if (step.toLowerCase().contains("alert")) {
                /**
                 * accept identify.
                 * Step : Switch to alert then accept
                 */
                if (step.toLowerCase().contains("accept")) {
                    cmd.switchAlertAccept(driver);
                }
                /**
                 * close and cancel identify.
                 * Step : Switch to alert then close.
                 * Step : Switch to alert then cancel.
                 */
                else if (step.toLowerCase().contains("close") || step.toLowerCase().contains("cancel")) {
                    cmd.switchAlertDismiss(driver);
                }
                /**
                 * verify text identify.
                 * Step : Switch to alert then verify text with 'text'.
                 */
                else if (step.toLowerCase().contains("verify text")) {
                    String alertText = cmd.switchAlertRead(driver);
                    assertThat(alertText).containsIgnoringCase(parseTextToEnter(test, step));
                }
                /**
                 * enter identify.
                 * Step : Switch to alert then enter 'Text'.
                 */
                else if (step.toLowerCase().contains("enter")) {
                    cmd.switchAlertSendKey(driver, parseTextToEnter(test, step));
                }
            }


            //Step :  switch to default content
            if (step.toLowerCase().contains("default content")) {
                cmd.switchToDefaultContent(driver);
            }

            /**
             * Switch to frame
             */
            if (step.toLowerCase().contains("frame")) {
                //using identify.
                if (step.toLowerCase().contains("using")) {
                    //Step : Switch to frame using id 'FrameID'.
                    if (step.toLowerCase().contains("id")) {
                        cmd.switchFrame(driver, parseTextToEnter(test, step));
                    }
                    //Step : Switch to frame using name 'FrameName'.
                    else if (step.toLowerCase().contains("name")) {
                        cmd.switchFrame(driver, parseTextToEnter(test, step));
                    }
                }
                /**
                 * parent or main identify.
                 * Step : Switch to parent frame.
                 * Step : Switch to main frame.
                 */
                else if (step.toLowerCase().contains("parent") || step.toLowerCase().contains("main")) {
                    cmd.switchMainFrame(driver);
                }
                /**
                 * element identify.
                 * Step : Switch to frame @WebElement.
                 */
                else if (parseElementName(step) != null) {
                    try {
                        cmd.switchFrameElement(driver, cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))));
                    } catch (NullPointerException e) {
                        logger.errorLog("No element found.");
                        throw e;
                    }
                }
            }

            /**
             * Switch to window.
             */
            if (step.toLowerCase().contains("window")) {
                /**
                 * Step : Switch to new window.
                 */
                if (step.toLowerCase().contains("new")) {
                    cmd.switchNewWindow(driver);
                }
                /**
                 * Step : Switch to main window.
                 * Step : Switch to parent window.
                 */
                else if (step.toLowerCase().contains("main") || step.toLowerCase().contains("parent")) {
                    cmd.switchMainWindow(driver);
                }

            }

        } catch (Exception e) {
            logger.testFailed("Step Failed");
            throw e;
        }
    }

    public void navigateFunction(WebDriver driver, String step) {
        Commands cmd = new Commands();
        /**
         * back identify.
         * Step : Navigate to back
         */
        if (step.toLowerCase().contains("back")) {
            cmd.navigateBack(driver);
        }

        /**
         * forward identify.
         * Step : Navigate to forward
         */
        else if (step.toLowerCase().contains("forward")) {
            cmd.navigateForward(driver);
        }
        /**
         * refresh identify.
         * Step : Navigate refresh
         */
        else if (step.toLowerCase().contains("refresh")) {
            cmd.navigateRefresh(driver);
        }
    }

    public void scrollFunction(WebDriver driver, String suiteName, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();

        /**
         * 'Bottom' identify.
         * Step: Scroll to bottom.
         */
        if (step.toLowerCase().contains("bottom")) {
            cmd.scrollBottom(driver);
        }
        /**
         * 'top' identify.
         * Step: Scroll to top.
         */
        else if (step.toLowerCase().contains("top")) {
            cmd.scrollTop(driver);
        }
        /**
         * 'top' identify.
         * Step: Scroll to Horizontal
         */
        else if (step.toLowerCase().contains("horizontal")) {
            cmd.scrollHorizontal(driver);
        }
        /**
         * number identify.
         * Step: Scroll to coordinate (50,100)
         */
        else if (step.toLowerCase().contains("coordinate")) {
            try {
                String x = parseNumverToEnter(step, 0);
                String y = parseNumverToEnter(step, 1);
                cmd.scrollToCoordinate(driver, x, y);
            } catch (NullPointerException e) {
                logger.testFailed("No coordinate found");
            }
        }
        /**
         * element identify.
         * Step: Scroll to @element
         */
        else if (parseElementName(step) != "") {
            try {
                cmd.scrollToElement(driver, cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))));
            } catch (NullPointerException e) {
                logger.testFailed("No element found");
                throw e;
            } catch (Exception e) {
                throw e;
            }
        }
    }

    public void pauseFunction(WebDriver driver, String suiteName, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();

        /**
         * 'disappear' identify.
         * Step : pause until @element is disappear
         */
        if (step.toLowerCase().contains("disappear")) {
            try {
                cmd.pauseElementDisappear(driver, cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))));
            } catch (Exception e) {
            }
        }
        /**
         * 'clickable' identify.
         * Step : pause until element is clickable
         */
        else if (step.toLowerCase().contains("clickable")) {
            try {
                cmd.pauseElementClickable(driver, cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))));
            } catch (Exception e) {
            }
        }
        /**
         * 'display' identify.
         * Step : pause until @Submit_Btn is display
         */
        else if (step.toLowerCase().contains("display")) {
            try {
                cmd.pauseElementDisplay(driver, locator.getLocatorValue(suiteName, parseElementName(step)));
            } catch (Exception e) {
            }
        }
        /**
         * 'sec' identify.
         * Step : pause for 5sec
         */
        else if (step.toLowerCase().contains("sec")) {
            cmd.pause(Integer.parseInt(parseNumverToEnter(step, 0)));
        }
    }

    public void selectFunction(WebDriver driver, JSONObject test, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();

        if (step.toLowerCase().contains("deselect")) {
            /**
             * 'all' identify.
             * Step : Deselect all from @element
             */
            if (step.toLowerCase().contains("all")) {
                cmd.deselectAll(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))));
            }
            /**
             * 'text' identify.
             * Step : Deselect using text 'Text' from @element
             */
            else if (step.toLowerCase().contains("text")) {
                cmd.deselectText(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))), parseTextToEnter(test, step));
            }
            /**
             * 'index' identify.
             * Step : Deselect using index 1 from @element
             */
            else if (step.toLowerCase().contains("index")) {
                cmd.deselectIndex(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))), Integer.parseInt(parseNumverToEnter(step, 0)));
            }
            /**
             * 'value' identify.
             * Step : Deselect using value 'Text' from @element
             */
            else if (step.toLowerCase().contains("value")) {
                cmd.deselectValue(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))), parseTextToEnter(test, step));
            }
        } else {
            /**
             * 'text' identify.
             * Step : Select using text 'Text' from @element
             */
            if (step.toLowerCase().contains("text")) {
                cmd.selectText(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))), parseTextToEnter(test, step));
            }
            /**
             * 'index' identify.
             * Step : Select using index 1 from @element
             */
            else if (step.toLowerCase().contains("index")) {
                cmd.selectIndex(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))), Integer.parseInt(parseNumverToEnter(step, 0)));
            }
            /**
             * 'value' identify.
             * Step : Select using value 'Text' form @element
             */
            else if (step.toLowerCase().contains("value")) {
                cmd.selectValue(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))), parseTextToEnter(test, step));
            }
        }
    }

    public String parseElementName(String step) {

        String[] stepWordList = step.split(":|\\s+");

        String elementName = "";

        for (String word : stepWordList) {
            if (word.contains("@")) {
                elementName = word.substring(1);
            }

        }
        return elementName;
    }

    /**
     * @param test
     * @param step
     * @return
     * @auther :
     * @lastModifiedBy: Ankit Mistry
     */
    public String parseTextToEnter(JSONObject test, String step) {
        String textToEnter = "";

        int startPoint = 0;
        int endPoint = 0;

        if (step.contains("{") && step.contains("}")) {
            startPoint = step.indexOf("{") + 1;
            endPoint = step.lastIndexOf("}");
            String headerName = step.substring(startPoint, endPoint);
            boolean isDetaSet=false;
            try {
                if (headerName.contains("DataSet.")) {
                    isDetaSet=true;
                    try {
                        String dataSet[]=headerName.split("\\.");
                        if(dataSet.length==3) {
                            textToEnter = dataDrivenParser.getGlobalDataValue(test.get("suiteName").toString(), dataSet[1], dataSet[2]);
                            logger.stepLog(step.replace(headerName, textToEnter).replaceAll("[{,}]","'").replace("@",""));
                        }
                        else{
                            throw new TesboException("Please enter DataSet in: '"+step+"'");
                        }

                    } catch (StringIndexOutOfBoundsException e) {
                        throw e;
                    }
                }
                else if(headerName.contains("Dataset.") || headerName.contains("dataSet.") || headerName.contains("dataset.")){
                    throw new TesboException("Please enter valid DataSet in: '"+step+"'");
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.testFailed(sw.toString());
                throw e;
            }

            if(!isDetaSet) {
                try {
                    if (test.get("dataType").toString().equalsIgnoreCase("excel")) {
                        try {
                            textToEnter = dataDrivenParser.getcellValuefromExcel(dataDrivenParser.getExcelUrl(test.get("suiteName").toString(), test.get("dataSetName").toString()), headerName, (Integer) test.get("row"), Integer.parseInt(dataDrivenParser.SheetNumber(test.get("suiteName").toString(), test.get("testName").toString())));
                            if (textToEnter != null) {
                                logger.stepLog(step.replace(headerName, textToEnter).replaceAll("[{,}]","'").replace("@",""));
                            }

                        } catch (StringIndexOutOfBoundsException e) {
                            logger.stepLog(step);
                            logger.testFailed("no string to enter. Create a separate exeception here");
                        }
                    }
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    logger.testFailed(sw.toString());
                }
                try {
                    if (test.get("dataType").toString().equalsIgnoreCase("global")) {
                        textToEnter = dataDrivenParser.getGlobalDataValue(test.get("suiteName").toString(), test.get("dataSetName").toString(), headerName);
                        logger.stepLog(step.replace(headerName, textToEnter).replaceAll("[{,}]","'").replace("@",""));

                    }
                } catch (Exception e) {
                    throw new TesboException("Key name " + headerName + " is not found in " + test.get("dataSetName").toString() + " data set");
                }
            }
        } else {
            startPoint = step.indexOf("'") + 1;
            endPoint = step.lastIndexOf("'");
            try {

                textToEnter = step.substring(startPoint, endPoint);

            } catch (StringIndexOutOfBoundsException e) {
                throw new TesboException("No string found to enter.");
            }
        }

        return textToEnter;
    }


    public String parseNumverToEnter(String step, int index) {
        String numbers;

        //extracting string
        numbers = step.replaceAll("[^-?0-9]+", " ");
        if(numbers.trim().equals("")){
            throw new TesboException("Seconds to pause is not found in '"+step+"'");
        }
        try {
            return Arrays.asList(numbers.trim().split(" ")).get(index);
        }catch (Exception e){
            throw new TesboException("Please add coordinate value (X, Y) in step '"+step+"'");
        }
    }

    public void generateReportDir() {
        File htmlReportMainDir = new File("./screenshots");

        if (!htmlReportMainDir.exists()) {
            htmlReportMainDir.mkdir();
        }
    }


    public void pressKey(WebDriver driver, JSONObject test, String step) throws Exception {

        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();
        Actions actions = new Actions(driver);
        if (step.toLowerCase().contains("enter")) {
            if (step.toLowerCase().contains("@")) {
                cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).sendKeys(Keys.ENTER);
            } else {
                actions.sendKeys(Keys.ENTER).build().perform();
            }

        } else if (step.toLowerCase().contains("tab")) {
            actions.sendKeys(Keys.TAB).build().perform();
        } else if (step.toLowerCase().contains("plus")) {
            String[] Steps = step.split(" ");
            boolean flag=false;
            for (int i = 0; i < Steps.length; i++) {

                if (Steps[i].equalsIgnoreCase("'ctrl'")) {
                    flag=true;
                    if(!(Steps[i + 2].replaceAll("'", "").toLowerCase().equals("")) &  Steps[i + 2].contains("'")
                            & ( Steps[i + 2].replaceAll("'", "").toLowerCase().equals("a") | Steps[i + 2].replaceAll("'", "").toLowerCase().equals("c") | Steps[i + 2].replaceAll("'", "").toLowerCase().equals("v"))) {
                        //actions.keyDown(Keys.COMMAND).sendKeys(Steps[i + 2].replaceAll("'", "").toLowerCase()).keyUp(Keys.COMMAND).perform();
                        cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).sendKeys(Keys.chord(Keys.CONTROL, Steps[i + 2].replaceAll("'", "").toLowerCase()));
                    }
                    else {
                        throw new TesboException("Please enter valid key");
                    }
                }
            }
            if(!flag){
                throw new TesboException("Please enter valid step.");
            }

        } else {
            throw new TesboException("Please enter valid step.");
        }
    }

    /**
     * @param step
     * @return modified step that will looks good in report
     * @author Viral Patel
     */

    public String stepModifierForReport(String step) {

        String finalstep = removeStepKeywordFromStep(step);

        //enter 'viral@gmail.com' on @ElementName

        if (removeStepKeywordFromStep(step).contains("@")) {

            String subString = "";
            if (finalstep.contains("'")) {
                String replacedStep = "";
                subString = finalstep.substring(step.indexOf("'") + 1, step.lastIndexOf("'"));
                if (finalstep.contains(subString)) {
                    replacedStep = finalstep.replace(subString, "SubString");
                }
                finalstep = (replacedStep.replace("@", "")).replace("SubString", subString);
            } else {
                finalstep = step.replace("@", "");
            }

        }

        return finalstep;
    }

    /**
     * @param step
     * @return return sentences that will remove step : or step: keyword
     * @author Viral Patel
     */
    public String removeStepKeywordFromStep(String step) {
        String finalStep = "";
        if (step.contains("Step:")) {
            finalStep = step.split("Step:")[1];
        }
        return finalStep;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param step
     * @return
     *
     */
    public String getCollectionName(String step) {
        String textToEnter = null;
        try {
            textToEnter = step.split(":")[1].trim();
        } catch (Exception e) {
            throw new TesboException("Pleas enter collection name");
        }
        return textToEnter;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param step
     * @param driver
     */
    public void windowResize(String step,WebDriver driver) {
        String size[] = null;
        try {
            size = step.split("\\(")[1].trim().replaceAll("\\)","").split(",");
            Dimension dimension = new Dimension(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
            driver.manage().window().setSize(dimension);
        } catch (Exception e) {
            throw new TesboException("Pleas enter 'X' and 'Y' dimension for window resize");
        }
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     */
    public void windowMinimize(WebDriver driver) {
        driver.manage().window().setPosition(new Point(0, -1000));
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     */
    public void windowMaximize(WebDriver driver) {
        driver.manage().window().maximize();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param test
     * @param step
     * @throws Exception
     */
    public void dragAndDropElement(WebDriver driver,JSONObject test,String step)throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();
        String[] stepWordList = step.split(":|\\s+");
        String elementTo = "";
        String elementFrom = "";

        for (int i=0;i<stepWordList.length;i++) {
            if (stepWordList[i].equalsIgnoreCase("to")) {
                elementTo = stepWordList[i+1].replace("@","");
                break;
            }
            if (stepWordList[i].contains("@")) { elementFrom = stepWordList[i].substring(1); }
        }
        if(!(elementTo.equals("") && elementFrom.equals(""))){
            // Not working
            cmd.dragAndDrop(driver, cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), elementFrom)), cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), elementTo)));
        }
        else{ throw new TesboException("Pleas enter valid step: '"+step+"'"); }

    }


    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param test
     * @param step
     * @throws Exception
     */
    public void clickOnElementFromList(WebDriver driver,JSONObject test,String step)throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();

        List<WebElement> listOfElements =cmd.findElements(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step)));

        //Click on first element from list
        if (step.toLowerCase().contains("first element")) {
         /*
            Step: Click on first element from List @elementName
          */
            listOfElements.get(0).click();
        }

        if (step.toLowerCase().contains("last element")) {
            /*
            Step: Click on last element from List @elementName
            */
            listOfElements.get(listOfElements.size()-1).click();
        }
        if (step.toLowerCase().contains("<") && step.toLowerCase().contains(">")) {
            /*
            Step: click on <any number> from List @elementName
            */
            String clickOnIndex="";
            int startPoint = step.indexOf("<") + 1;
            int endPoint = step.lastIndexOf(">");
            try {
                clickOnIndex = step.substring(startPoint, endPoint).trim();
            } catch (StringIndexOutOfBoundsException e) {
                throw new TesboException("No string found for click");
            }
            listOfElements.get(Integer.parseInt(clickOnIndex)).click();
        }

        if (step.toLowerCase().contains("\"")) {
            /*
            Step: click on "AnyTextHere" from List @elementName
            */

            String clickOnText="";
            int startPoint = step.indexOf("\"") + 1;
            int endPoint = step.lastIndexOf("\"");
            try {
                clickOnText = step.substring(startPoint, endPoint);

            } catch (StringIndexOutOfBoundsException e) {
                throw new TesboException("No string found for click");
            }
            for(WebElement element:listOfElements){
                if(element.getText().equals(clickOnText)) {
                    element.click();
                }

            }
        }

    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param test
     * @throws Exception
     */
    public boolean isSeverityOrPriority(JSONObject test) {
        SuiteParser suiteParser=new SuiteParser();
        boolean isSeverityOrPriority=false;
        JSONArray steps= suiteParser.getSeverityAndPriority(test);
        if(steps.size()>0){
            isSeverityOrPriority=true;
        }
        return isSeverityOrPriority;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param step
     * @param driver
     * @param test
     * @return
     * @throws Exception
     */
    public String printStep(WebDriver driver,String step, JSONObject test) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();
        String printStep;
        if(step.toLowerCase().contains("@")){
            printStep="Step: "+ cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).getText();
        }
        else{
            int startPoint = 0;
            int endPoint = 0;
            startPoint = step.indexOf("\"") + 1;
            endPoint = step.lastIndexOf("\"");
            String printText = step.substring(startPoint, endPoint);

            printStep="Step: "+printText;
        }
        return printStep.replace("@","");
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param test
     * @param step
     * @throws Exception
     */
    public void clickOnOffset(WebDriver driver,JSONObject test,String step)throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();

        WebElement element =cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step)));
        int startPoint = 0;
        int endPoint = 0;
        startPoint = step.indexOf("(") + 1;
        endPoint = step.lastIndexOf(")");
        String offsetString = step.substring(startPoint, endPoint);
        String offsets[]=offsetString.trim().split(",");
        if(offsets.length!=2){
            throw new TesboException("Enter X and Y offset");
        }
        cmd.clickOnOffset(driver,element,offsets);


    }

}
