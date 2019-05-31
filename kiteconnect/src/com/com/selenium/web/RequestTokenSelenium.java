package com.com.selenium.web;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import static com.sun.javafx.fxml.expression.Expression.get;


public class RequestTokenSelenium {

    String passwordField = "//input[@type='password']";
    String passwrdSubmitBtn = "//button[@type='submit']";
    String pinField = "//label[text()='PIN']/../input[@type='password']";
    String pinSubmitField = "//button[@type='submit'][contains(.,'Continue']";

    public RequestTokenSelenium(){}

    public String getRequestToken(String url){

        try{

            WebDriver driver = new HtmlUnitDriver(BrowserVersion.CHROME);
            //WebDriverWait wait = new WebDriverWait(driver, 10);

            //String phantomJSExe = "E:\\MyGit\\javakiteconnect\\lib\\phantomjs";
            //File file = new File("E:\\MyGit\\javakiteconnect\\lib\\phantomjs");
            //System.setProperty("phantomjs.binary.path", file.getAbsolutePath());
            //WebDriver driver = new PhantomJSDriver();

            //System.setProperty("webdriver.gecko.driver", "E:\\SW\\Selenium\\geckodriver.exe");
            //WebDriver driver = new FirefoxDriver();

            driver.manage().window().maximize();
            driver.manage().deleteAllCookies();

            driver.get(url);
            System.out.println("Current URI after launching page >> " + driver.getCurrentUrl());

            WebClient webClient = (WebClient)get(driver, "webClient");

            WebElement elem=(new WebDriverWait(driver, 10)) //added this line
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath(passwordField)));

            driver.findElement(By.xpath(passwordField)).sendKeys("b@laji1986");
            driver.findElement(By.xpath(passwrdSubmitBtn)).click();

            Thread.sleep(3000);
            System.out.println("Current URl after feeding password >> " + driver.getCurrentUrl());

            driver.findElement(By.xpath(pinField)).sendKeys("020583");
            driver.findElement(By.xpath("pinSubmitField")).click();

            Thread.sleep(3000);

            String currUrl = driver.getCurrentUrl();
            driver.quit();
            return currUrl;

        }catch(Exception e){
            e.printStackTrace();
        }

        return "";

    }

    private static Object get(Object obj, String field) throws Exception{
        Field f = obj.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(obj);
    }

    public static void main(String[] args){
        try{
            //WebDriver driver = new HtmlUnitDriver(BrowserVersion.CHROME);

            String geckoDriverPath = "E:\\SW\\Selenium\\geckodriver.exe";
            System.setProperty("webdriver.gecko.driver", geckoDriverPath);

            WebDriver driver = new FirefoxDriver();
            driver.get("https://www.youtube.com/channel/UCqvVj1LkOpA8tjb7RadTvOg/videos");

            driver.manage().window().maximize();
            driver.manage().deleteAllCookies();

            String vdoTitle = "//a[@id='video-title']";
            List<WebElement> vdoTitles = driver.findElements(By.xpath(vdoTitle));
            System.out.println("No of VDO >> " + vdoTitles.size());
            for(WebElement elem : vdoTitles){
                System.out.println(elem.getText());
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}