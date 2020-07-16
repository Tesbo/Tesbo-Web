package selenium;

import execution.SetCommandLineArgument;
import framework.CommonMethods;
import framework.GetConfiguration;
import logger.TesboLogger;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import exception.TesboException;


import static org.openqa.selenium.support.ui.ExpectedConditions.*;

public class Commands {

    protected static Wait<WebDriver> wait;
    String parantWindow = "";
    String childWindow = "";
    TesboLogger tesboLogger =new TesboLogger();
    private static final Logger log = LogManager.getLogger(Commands.class);
    boolean isIf=true;
    String elementText="element";
    String isBreakText="isBreak";
    CommonMethods commonMethods=new CommonMethods();

    /**
     * @param driver       webdriver object for the Test
     * @param elementvalue element loactor valuse
     * @return webelement
     */

    public WebElement findElement(WebDriver driver, String elementvalue) {
        WebElement element = null;
        GetConfiguration config = new GetConfiguration();

        if(elementvalue.contains("_IF")){
            isIf=false;
            elementvalue=elementvalue.replace("_IF","");
        }
        List<String> locatorTypes;
        locatorTypes=config.getLocatorPreference();
        if(locatorTypes!=null){
            if(locatorTypes.isEmpty()){
                commonMethods.throwTesboException("Please enter locator Preference",log);
            }
            element= findElementFromLocatorPreference(driver,elementvalue,locatorTypes);

        }else {
            element=getElementByClassNameXpathIdAndCssSelector(driver,elementvalue);

            if (config.getHighlightElement()) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].setAttribute('style', 'background: yellow; border: 2px solid red;');", element);
            }
        }
        return element;
    }

    public WebElement getElementByClassNameXpathIdAndCssSelector(WebDriver driver,String elementvalue){
        WebElement element=null;
        try {
            element = driver.findElement(By.cssSelector(elementvalue));
        } catch (NoSuchElementException css) {
            try {
                element = driver.findElement(By.id(elementvalue));
            } catch (NoSuchElementException id) {
                try {
                    element = driver.findElement(By.xpath(elementvalue));
                } catch (Exception xpath) {
                    try {
                        element = driver.findElement(By.className(elementvalue));
                    } catch (Exception className) {
                        try {
                            element = driver.findElement(By.name(elementvalue));
                        } catch (Exception name) {
                            element=getElementByPartialLinkTextLinkTextTagNameAndName(driver,elementvalue);
                        }
                    }
                }
            }
        }
        return element;
    }

    public WebElement getElementByPartialLinkTextLinkTextTagNameAndName(WebDriver driver,String elementvalue){
        WebElement element=null;
        try {
            element = driver.findElement(By.name(elementvalue));
        } catch (Exception name) {
            try {
                element = driver.findElement(By.tagName(elementvalue));
            } catch (Exception tagName) {
                try {
                    element = driver.findElement(By.linkText(elementvalue));
                } catch (Exception linkText) {
                    try {
                        element = driver.findElement(By.partialLinkText(elementvalue));
                    } catch (NoSuchElementException e) {
                        throwExceptionWhenElementNotFound(elementvalue);
                    }
                }
            }
        }
        return element;
    }

    public WebElement findElementFromLocatorPreference(WebDriver driver, String elementvalue,List<String> locatorTypes) {
        WebElement element = null;
        GetConfiguration config = new GetConfiguration();

        int locatorTypesSize=locatorTypes.size();
        int locatorCount=1;
        boolean isBreak=false;
        for (String locatorType:locatorTypes){

            if(locatorType.equalsIgnoreCase("css")){
                JSONObject elementDetails=getElementByCSS(driver,elementvalue,locatorCount,locatorTypesSize);
                element= (WebElement) elementDetails.get(elementText);
                isBreak= (boolean) elementDetails.get(isBreakText);
            }
            else if(locatorType.equalsIgnoreCase("id")){
                JSONObject elementDetails=getElementById(driver,elementvalue,locatorCount,locatorTypesSize);
                element= (WebElement) elementDetails.get(elementText);
                isBreak= (boolean) elementDetails.get(isBreakText);
            }
            else if(locatorType.equalsIgnoreCase("xpath")){
                JSONObject elementDetails=getElementByXpath(driver,elementvalue,locatorCount,locatorTypesSize);
                element= (WebElement) elementDetails.get(elementText);
                isBreak= (boolean) elementDetails.get(isBreakText);
            }
            else if(locatorType.equalsIgnoreCase("className")){
                JSONObject elementDetails=getElementByClassName(driver,elementvalue,locatorCount,locatorTypesSize);
                element= (WebElement) elementDetails.get(elementText);
                isBreak= (boolean) elementDetails.get(isBreakText);
            }
            else if(locatorType.equalsIgnoreCase("name")){
                JSONObject elementDetails=getElementByName(driver,elementvalue,locatorCount,locatorTypesSize);
                element= (WebElement) elementDetails.get(elementText);
                isBreak= (boolean) elementDetails.get(isBreakText);
            }
            else if(locatorType.equalsIgnoreCase("tagName")){
                JSONObject elementDetails=getElementByTagName(driver,elementvalue,locatorCount,locatorTypesSize);
                element= (WebElement) elementDetails.get(elementText);
                isBreak= (boolean) elementDetails.get(isBreakText);
            }
            else if(locatorType.equalsIgnoreCase("linkText")){
                JSONObject elementDetails=getElementByLinkText(driver,elementvalue,locatorCount,locatorTypesSize);
                element= (WebElement) elementDetails.get(elementText);
                isBreak= (boolean) elementDetails.get(isBreakText);
            }
            else if(locatorType.equalsIgnoreCase("partialLinkText")){
                JSONObject elementDetails=getElementByPartialLinkText(driver,elementvalue,locatorCount,locatorTypesSize);
                element= (WebElement) elementDetails.get(elementText);
                isBreak= (boolean) elementDetails.get(isBreakText);
            }
            if(isBreak){break;}
            locatorCount++;
        }

        if (config.getHighlightElement() && isIf) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].setAttribute('style', 'background: yellow; border: 2px solid red;');", element);
        }
        return element;
    }

    public JSONObject getElementByCSS(WebDriver driver,String elementValue,int locatorCount,int locatorTypesSize){
        WebElement element = null;
        boolean isBreak=false;
        try {
            element = driver.findElement(By.cssSelector(elementValue));
            isBreak=true;
        } catch (Exception css) {
            if(locatorCount==locatorTypesSize) {
                throwExceptionWhenElementNotFound(elementValue);
            }
        }
        JSONObject elementDetails=new JSONObject();
        elementDetails.put(elementText,element);
        elementDetails.put(isBreakText,isBreak);

        return elementDetails;
    }

    public JSONObject getElementById(WebDriver driver,String elementValue,int locatorCount,int locatorTypesSize){
        WebElement element = null;
        boolean isBreak=false;
        try {
            element = driver.findElement(By.id(elementValue));
            isBreak=true;
        } catch (Exception id) {
            if(locatorCount==locatorTypesSize) {
                throwExceptionWhenElementNotFound(elementValue);
            }
        }
        JSONObject elementDetails=new JSONObject();
        elementDetails.put(elementText,element);
        elementDetails.put(isBreakText,isBreak);

        return elementDetails;
    }

    public JSONObject getElementByXpath(WebDriver driver,String elementValue,int locatorCount,int locatorTypesSize){
        WebElement element = null;
        boolean isBreak=false;
        try {
            element = driver.findElement(By.xpath(elementValue));
            isBreak=true;
        } catch (Exception xpath) {
            if(locatorCount==locatorTypesSize) {
                throwExceptionWhenElementNotFound(elementValue);
            }
        }
        JSONObject elementDetails=new JSONObject();
        elementDetails.put(elementText,element);
        elementDetails.put(isBreakText,isBreak);

        return elementDetails;
    }

    public JSONObject getElementByClassName(WebDriver driver,String elementValue,int locatorCount,int locatorTypesSize){
        WebElement element = null;
        boolean isBreak=false;
        try {
            element = driver.findElement(By.className(elementValue));
            isBreak=true;
        } catch (Exception className) {
            if(locatorCount==locatorTypesSize) {
                throwExceptionWhenElementNotFound(elementValue);
            }
        }
        JSONObject elementDetails=new JSONObject();
        elementDetails.put(elementText,element);
        elementDetails.put(isBreakText,isBreak);

        return elementDetails;
    }

    public JSONObject getElementByName(WebDriver driver,String elementValue,int locatorCount,int locatorTypesSize){
        WebElement element = null;
        boolean isBreak=false;
        try {
            element = driver.findElement(By.name(elementValue));
            isBreak=true;
        } catch (Exception name) {
            if(locatorCount==locatorTypesSize) {
                throwExceptionWhenElementNotFound(elementValue);
            }
        }
        JSONObject elementDetails=new JSONObject();
        elementDetails.put(elementText,element);
        elementDetails.put(isBreakText,isBreak);

        return elementDetails;
    }

    public JSONObject getElementByTagName(WebDriver driver,String elementValue,int locatorCount,int locatorTypesSize){
        WebElement element = null;
        boolean isBreak=false;
        try {
            element = driver.findElement(By.tagName(elementValue));
            isBreak=true;
        } catch (Exception tagName) {
            if(locatorCount==locatorTypesSize) {
                throwExceptionWhenElementNotFound(elementValue);
            }
        }
        JSONObject elementDetails=new JSONObject();
        elementDetails.put(elementText,element);
        elementDetails.put(isBreakText,isBreak);

        return elementDetails;
    }

    public JSONObject getElementByLinkText(WebDriver driver,String elementValue,int locatorCount,int locatorTypesSize){
        WebElement element = null;
        boolean isBreak=false;
        try {
            element = driver.findElement(By.linkText(elementValue));
            isBreak=true;
        } catch (Exception linkText) {
            if(locatorCount==locatorTypesSize) {
                throwExceptionWhenElementNotFound(elementValue);
            }
        }
        JSONObject elementDetails=new JSONObject();
        elementDetails.put(elementText,element);
        elementDetails.put(isBreakText,isBreak);

        return elementDetails;
    }

    public JSONObject getElementByPartialLinkText(WebDriver driver,String elementValue,int locatorCount,int locatorTypesSize){
        WebElement element = null;
        boolean isBreak=false;
        try {
            element = driver.findElement(By.partialLinkText(elementValue));
            isBreak=true;
        } catch (Exception e) {
            if(locatorCount==locatorTypesSize) {
                throwExceptionWhenElementNotFound(elementValue);
            }
        }
        JSONObject elementDetails=new JSONObject();
        elementDetails.put(elementText,element);
        elementDetails.put(isBreakText,isBreak);

        return elementDetails;
    }

    public void throwExceptionWhenElementNotFound(String elementvalue){
        if(isIf) {
            commonMethods.throwTesboException("Element Not found With Locator '"+elementvalue+"'",log);
        }
    }

    public List<WebElement> findElements(WebDriver driver, String elementvalue) {

        List<WebElement> listOfElements =null;

        listOfElements=getElementsByClassNameXpathIdAndCssSelector(driver,elementvalue);

        return listOfElements;
    }

    public List<WebElement> getElementsByClassNameXpathIdAndCssSelector(WebDriver driver,String elementvalue){
        List<WebElement> listOfElements=null;
        try {

            listOfElements = driver.findElements(By.cssSelector(elementvalue));

        } catch (NoSuchElementException css) {

            try {
                listOfElements = driver.findElements(By.xpath(elementvalue));
            } catch (Exception xpath) {
                try {
                    listOfElements = driver.findElements(By.id(elementvalue));

                } catch (NoSuchElementException id) {
                    try {
                        listOfElements = driver.findElements(By.className(elementvalue));
                    } catch (Exception className) {
                        listOfElements=getElementsByPartialLinkTextLinkTextTagNameAndName(driver,elementvalue);
                    }

                }

            }
        }
        return listOfElements;
    }

    public List<WebElement> getElementsByPartialLinkTextLinkTextTagNameAndName(WebDriver driver,String elementvalue){
        List<WebElement> listOfElements =null;
        try {
            listOfElements = driver.findElements(By.name(elementvalue));
        } catch (Exception name) {
            try {
                listOfElements = driver.findElements(By.tagName(elementvalue));
            } catch (Exception tagName) {
                try {
                    listOfElements = driver.findElements(By.linkText(elementvalue));
                } catch (Exception linkText) {
                    try {
                        listOfElements = driver.findElements(By.partialLinkText(elementvalue));
                    } catch (NoSuchElementException e) {
                        throwExceptionWhenElementNotFound(elementvalue);
                    }
                }

            }
        }
        return listOfElements;
    }

    /**
     * @param el webelement
     */

    public void clickOnElement(WebElement el) {
        el.click();
    }

    /**
     * @param el   webelement
     * @param text text to enter into the text box
     */

    public void sendskeyOnElement(WebElement el, String text) {
        el.sendKeys(text);
    }


    public void openUrl(WebDriver driver, String url) {
        driver.get(url);
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param element
     */
    public void scrollAndClick(WebDriver driver, WebElement element)  {
        scrollToCoordinate(driver, "250", "300");
        pause(5);
        element.click();
    }

    public void pause(int sec) {
        try {
            Thread.sleep((long)sec * 1000);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
    }

    public void switchToActiveElement(WebDriver driver) {
        driver.switchTo().activeElement();
    }

    public void switchToDefaultContent(WebDriver driver) {
        driver.switchTo().defaultContent();
    }

    /**
     * @param driver
     * @param id
     * @Description : Switch to IFrame using ID, Name and Web Element.
     */
    public void switchFrame(WebDriver driver, String id) {
        driver.switchTo().frame(id);
    }

    /**
     * @param driver
     * @Description : Switch to main or parent IFrame.
     */
    public void switchMainFrame(WebDriver driver) {
        driver.switchTo().parentFrame();
    }

    /**
     * @param driver
     * @Description :
     */
    public void switchFrameElement(WebDriver driver, WebElement element) {
        driver.switchTo().frame(element);
    }

    /**
     * @param driver
     * @Description : Switch to alert and Accept.
     */
    public void switchAlertAccept(WebDriver driver) {
        driver.switchTo().alert().accept();
    }

    /**
     * @param driver
     * @Description : Switch to alert and Dismiss.
     */
    public void switchAlertDismiss(WebDriver driver) {
        driver.switchTo().alert().dismiss();
    }

    /**
     * @param driver
     * @Description : Switch to alert and get alert text.
     */
    public String switchAlertRead(WebDriver driver) {
        return driver.switchTo().alert().getText();
    }

    /**
     * @param driver
     * @param text
     * @Description : Switch to alert and enter text.
     */
    public void switchAlertSendKey(WebDriver driver, String text) {
        Alert alert  = new WebDriverWait(driver, 10).until(ExpectedConditions.alertIsPresent());
        alert.sendKeys(text);
        alert.accept();
    }

    /**
     * @param driver
     * @Description : Switch to new open window.
     */
    public void switchNewWindow(WebDriver driver, String browser) {
        //Get parent window.
        parantWindow = driver.getWindowHandle();
        //Get all open windows.
        Set<String> handles = driver.getWindowHandles();

        for (String windowHandle : handles) {
            //Condition for get new window.
            if (!windowHandle.equals(parantWindow)) {
                childWindow = windowHandle;
                //Switch to new window.
                driver.switchTo().window(childWindow);
                if(browser.equals("firefox")){
                    break;
                }
            }
        }
    }

    /**
     * @param driver
     * @Description : Switch to main/parent window.
     */
    public void switchMainWindow(WebDriver driver) {
        JSONArray handles = (JSONArray) driver.getWindowHandles();

        //Condition for get new window.
        driver.switchTo().window(handles.get(0).toString());

    }

    /**
     * @param driver
     * @Description : Close window.
     */
    public void closeWindow(WebDriver driver) {
        driver.close();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param step
     * @param browser
     * @Description : Close window tab using index.
     */
    public void closeWindowByIndex(WebDriver driver, String step,String browser) {

        int startPoint = step.indexOf('<') + 1;
        int endPoint = step.lastIndexOf('>');
        String indexes = step.substring(startPoint, endPoint).trim();
        String[] index=getIndexOfBrowserWindow(indexes);

        ArrayList<String> tabs = new ArrayList<> (driver.getWindowHandles());
        if(browser.equalsIgnoreCase("chrome")){
            ArrayList<String> chromeTabs= new ArrayList<>();
            chromeTabs.add(tabs.get(0));
            for(int i=tabs.size()-1;i>0;i--){
                chromeTabs.add(tabs.get(i));
            }
            tabs=new ArrayList<>();
            tabs.addAll(chromeTabs);
        }
        int tabIndex;
        for(int i=0; i<index.length;i++) {
            try {
                 tabIndex=Integer.parseInt(index[i]);
            }catch (Exception e){
                throw new TesboException("Enter numeric index value for close particular browser tab");
            }
            try {
                driver.switchTo().window(tabs.get(tabIndex));
                driver.close();
            }catch (Exception e){
                throw new TesboException("Browser tab index <"+tabIndex+"> is not found");
            }
        }

    }

    public String[] getIndexOfBrowserWindow(String indexes){
        String[] index;
        if(indexes.toLowerCase().contains("to")){
            int startIndex=Integer.parseInt(indexes.toLowerCase().split("to")[0].trim());
            int endIndex=Integer.parseInt(indexes.toLowerCase().split("to")[1].trim());
            if(startIndex>endIndex) {
                commonMethods.throwTesboException("Starting index is greater than end index <" + indexes + ">",log);
            }
            int numberOfIndex=0;
            for(int i=startIndex;i<=endIndex;i++){numberOfIndex++;}
            index= new String[numberOfIndex];
            for(int i=0;i<numberOfIndex;i++){ index[i]= String.valueOf(startIndex++); }
        }else {
            if (indexes.contains(",")) {
                index = indexes.split(",");
            } else {
                index = new String[1];
                index[0] = indexes;
            }
        }
        return index;
    }

    /**
     * @param driver
     * @Description : Navigate to back window.
     */
    public void navigateBack(WebDriver driver) {
        driver.navigate().back();
    }

    /**
     * @param driver
     * @Description : Navigate to forward window.
     */
    public void navigateForward(WebDriver driver) {
        driver.navigate().forward();
    }

    /**
     * @param driver
     * @Description : Navigate refresh window.
     */
    public void navigateRefresh(WebDriver driver) {
        driver.navigate().refresh();
    }

    /**
     * @param driver
     * @Description : Scrolling to bottom.
     */
    public void scrollBottom(WebDriver driver) {
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     */
    public void scrollHorizontal(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(1000,0)", "");
    }

    /**
     * @param driver
     * @Description : Scrolling to top.
     */
    public void scrollTop(WebDriver driver) {
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        js.executeScript("window.scrollTo(0, -document.body.scrollHeight);");
    }

    /**
     * @param driver
     * @param element
     * @Description : Scrolling to web element.
     */
    public void scrollToElement(WebDriver driver, WebElement element) {
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        js.executeScript("arguments[0].scrollIntoView();", (element));
    }

    /**
     * @param driver
     * @param x
     * @param y
     * @Description : Scrolling to coordinate.
     */
    public void scrollToCoordinate(WebDriver driver, String x, String y) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(" + x + ", " + y + ")");
    }

    /**
     * @param driver
     * @param element
     * @Description : pause driver until element disappear.
     */
    public void pauseElementDisappear(WebDriver driver, WebElement element) {
        wait = new WebDriverWait(driver, 15);
        wait.until(invisibilityOf(element));

    }

    /**
     * @param driver
     * @param element
     * @Description : pause driver until element clickable.
     */
    public void pauseAndClick(WebDriver driver, WebElement element) {
        wait = new WebDriverWait(driver, 20);
        wait.until(elementToBeClickable(element));
        element.click();
    }

    /**
     * @param driver
     * @param element
     * @Description : pause driver until element clickable.
     */
    public void pauseElementClickable(WebDriver driver, WebElement element) {
        wait = new WebDriverWait(driver, 20);
        wait.until(elementToBeClickable(element));
    }

    /**
     * @param driver
     * @param element
     * @return : Web element
     * @Description : pause driver until element display.
     */
    public void pauseElementDisplay(WebDriver driver, WebElement element) {
        wait = new WebDriverWait(driver, 20);
        wait.until(visibilityOf(element));

    }

    /**
     * @param element
     * @param text
     * @Description : select drop down using visible text.
     */
    public void selectText(WebElement element, String text) {
        Select dropDown = new Select(element);
        dropDown.selectByVisibleText(text);
    }

    /**
     * @param element
     * @param index
     * @Description : select drop down value using index.
     */
    public void selectIndex(WebElement element, int index) {
        Select dropDown = new Select(element);
        dropDown.selectByIndex(index);
    }

    /**
     * @param element
     * @param text
     * @Description : select drop down using value.
     */
    public void selectValue(WebElement element, String text) {
        Select dropDown = new Select(element);
        dropDown.selectByValue(text);
    }

    /**
     * @param element
     * @Description : Deselect all the value from drop down.
     */
    public void deselectAll(WebElement element) {
        Select dropDown = new Select(element);
        dropDown.deselectAll();
    }

    /**
     * @param element
     * @param text
     * @Description : Deselect value using visible text.
     */
    public void deselectText(WebElement element, String text) {
        Select dropDown = new Select(element);
        dropDown.deselectByVisibleText(text);
    }

    /**
     * @param element
     * @param index
     * @Description : Deselect value using Index.
     */
    public void deselectIndex(WebElement element, int index) {
        Select dropDown = new Select(element);
        dropDown.deselectByIndex(index);
    }

    /**
     * @param element
     * @param text
     * @Description : Deselect drop down using value.
     */
    public void deselectValue(WebElement element, String text) {
        Select dropDown = new Select(element);
        dropDown.deselectByValue(text);
    }


    public String captureScreenshot(WebDriver driver, String suitName, String testName) {
        return captureScreen(driver, suitName, testName);
    }

    public String captureScreen(WebDriver driver, String suitName, String testName) {
        String path;
        try {
            File filePath = new File("screenshots");
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            path = filePath + "/" + (suitName.split(".s"))[0] + "_" + testName.replaceAll("\\s", "") + "_" + dtf.format(LocalDateTime.now()) + ".png";
            FileUtils.copyFile(scrFile, new File(path));
        } catch (IOException e) {
            path = "Failed to capture screenshot: " + e.getMessage();
        }
        return path;
    }

    /**
     * @param browserName
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public boolean isCapabilities(String browserName) {
        GetConfiguration config = new GetConfiguration();
        JSONObject capabilities = null;
        boolean browser = false;
        try {
            capabilities = config.getCapabilities(browserName);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
        if (capabilities != null) {
            if(capabilities.size()>0){
                browser = true;
            }
            else {
                browser = false;
            }
        }
        return browser;
    }

    /**
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public String getSeleniumAddress() {
        GetConfiguration config = new GetConfiguration();
        String seleniumAddress = null;
        try {
            seleniumAddress = config.getSeleniumAddress();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
        return seleniumAddress;
    }

    /**
     * @param browserName
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public JSONObject getCapabilities(String browserName) {
        GetConfiguration config = new GetConfiguration();
        JSONObject capabilities = null;
        try {
            capabilities = config.getCapabilities(browserName);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }

        return capabilities;
    }

    /**
     * @param capabilities
     * @param capability
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public DesiredCapabilities setCapabilities(JSONObject capabilities, DesiredCapabilities capability) {

        for (Object cap : capabilities.keySet()) {
            capability.setCapability(cap.toString(), capabilities.get(cap.toString()));
        }

        if(SetCommandLineArgument.platform!=null){
            capability.setCapability("platform", SetCommandLineArgument.platform);
        }
        return capability;
    }

    /**
     * @param seleniumAddress
     * @param driver
     * @param capability
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public WebDriver openRemoteBrowser(WebDriver driver, DesiredCapabilities capability, String seleniumAddress) {
        try {
            driver = new RemoteWebDriver(new URL(seleniumAddress), capability);
        } catch (MalformedURLException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
        return driver;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param element
     */
    public void rightClick(WebDriver driver,WebElement element) {
        Actions action = new Actions(driver).contextClick(element);
        action.build().perform();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param element
     */
    public void doubleClick(WebDriver driver,WebElement element) {
        Actions action = new Actions(driver);
        action.doubleClick(element).perform();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     *
     */
    public void deleteAllCookies(WebDriver driver) {
        driver.manage().deleteAllCookies();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param element
     */
    public void mouseHover(WebDriver driver, WebElement element)  {
        Actions action = new Actions(driver);
        action.moveToElement(element).build().perform();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     *
     * @param driver
     * @param elementTo
     * @param elementFrom
     */
    public void dragAndDrop(WebDriver driver, WebElement elementFrom,WebElement elementTo)  {
        Actions action = new Actions(driver);
        action.dragAndDrop(elementFrom, elementTo).build().perform();

        // Drag And Drop functionality is pending
    }


    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param element
     */
    public void clickAndHold(WebDriver driver, WebElement element)  {
        Actions action = new Actions(driver);
        action.clickAndHold(element).build().perform();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     *
     */
    public void getPageSource(WebDriver driver)  {
        log.error(driver.getPageSource());
        tesboLogger.stepLog(driver.getPageSource());
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param cookieName
     */
    public boolean isCookieAvailable(WebDriver driver, String cookieName)  {
        boolean isCookie=false;
        for(Cookie ck : driver.manage().getCookies())
        {
            if(cookieName.equalsIgnoreCase(ck.getName())){ isCookie=true; }
        }
        return isCookie;

    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param element
     * @param suitName
     * @param testName
     * @throws IOException
     */
    public String screenshotElement(WebDriver driver, WebElement element, String suitName, String testName) throws IOException {

        // Get entire page screenshot
        String path;
        File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        BufferedImage fullImg = null;
        fullImg = ImageIO.read(screenshot);
        // Get the location of element on the page
        Point point = element.getLocation();
        // Get width and height of the element
        int eleWidth = element.getSize().getWidth();
        int eleHeight = element.getSize().getHeight();
        // Crop the entire page screenshot to get only element screenshot
        BufferedImage eleScreenshot= fullImg.getSubimage(point.getX(), point.getY(), eleWidth, eleHeight);
        ImageIO.write(eleScreenshot, "png", screenshot);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        path ="ElementScreenshots/"+(suitName.split(".s"))[0] + "_" + testName.replaceAll("\\s", "") + "_" + dtf.format(LocalDateTime.now()) + ".png";
        File screenshotLocation = new File(path);
        FileUtils.copyFile(screenshot, screenshotLocation);

        return path;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param offset
     * @param element
     */
    public void clickOnOffset(WebDriver driver,WebElement element, String[] offset)  {
        Actions builder = new Actions(driver);
        builder.moveToElement(element, Integer.parseInt(offset[0].trim()), Integer.parseInt(offset[1].trim())).click().build().perform();

    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     */
    public boolean getCurrentUrl(WebDriver driver,String url)  {
        boolean isURL=false;
        if(url.equals(driver.getCurrentUrl())){ isURL=true; }
        return isURL;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     */
    public boolean verifyCurrentUrlContains(WebDriver driver,String url)  {
        boolean isURL=false;
        if(driver.getCurrentUrl().contains(url)){ isURL=true; }
        return isURL;
    }

}