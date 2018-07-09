package framework;

import Selenium.Commands;
import logger.Logger;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import Exception.TesboException;

import static org.assertj.core.api.Assertions.assertThat;

public class StepParser {


    Logger logger = new Logger();
    DataDrivenParser dataDrivenParser = new DataDrivenParser();
    public static String screenShotURL=null;

    public void parseStep(WebDriver driver, JSONObject test, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();

        if (!step.toLowerCase().contains("{") && !step.toLowerCase().contains("}"))
            logger.stepLog(step);

        //Clicks
        if (step.toLowerCase().contains("click") && !(step.toLowerCase().contains("right") | step.toLowerCase().contains("double"))) {
            cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).click();
        }

        //Right Click
        if (step.toLowerCase().contains("right click")) {
           cmd.rightClick(driver, cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))));
        }

        //Double Click
        if (step.toLowerCase().contains("double click")) {
            cmd.doubleClick(driver, cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))));
        }

        //Capture Screenshot
        if (step.toLowerCase().contains("capture screenshot")) {
            screenShotURL=cmd.captureScreenshot(driver, test.get("suiteName").toString(), test.get("testName").toString());
            logger.stepLog("Screenshot: "+screenShotURL);
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
            cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).sendKeys(parseTextToEnter(test, step));
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
         * Step :Scroll to bottom.
         */
        if (step.toLowerCase().contains("bottom")) {
            cmd.scrollBottom(driver);
        }
        /**
         * 'top' identify.
         * step : Scroll to top.
         */
        else if (step.toLowerCase().contains("top")) {
            cmd.scrollTop(driver);
        }
        /**
         * number identify.
         * Step : Scroll to coordinate (50,100)
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
         * Step : Scroll to @element
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
            try {
                if (test.get("dataType").toString().equalsIgnoreCase("excel")) {
                    try {
                        textToEnter = dataDrivenParser.getcellValuefromExcel(dataDrivenParser.getExcelUrl(test.get("suiteName").toString(), test.get("dataSetName").toString()), headerName, (Integer) test.get("row"));
                        logger.stepLog(step.replace(headerName, textToEnter));

                    } catch (StringIndexOutOfBoundsException e) {
                        logger.stepLog(step);
                        logger.testFailed("no string to enter. Create a separate exeception here");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (test.get("dataType").toString().equalsIgnoreCase("global")) {
                    textToEnter = dataDrivenParser.getGlobalDataValue(test.get("suiteName").toString(), test.get("dataSetName").toString(), headerName);
                    logger.stepLog(step.replace(headerName, textToEnter));

                }
            } catch (Exception e) {
                throw new TesboException("Key name " + headerName + " is not found in " + test.get("dataSetName").toString() + " data set");
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


}
