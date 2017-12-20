package Main;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Crawler {
    public static void main(String[] args) throws InterruptedException, IOException {
        Properties pageInfo = new Properties();
        System.setProperty("webdriver.chrome.driver", "/Users/Tig/IdeaProjects/WebCrawler/Resources/chromedriver");
        FileInputStream fis = new FileInputStream(System.getProperty("usr.dir", "/Users/Tig/IdeaProjects/WebCrawler/Resources/pageInfo.properties"));
        pageInfo.load(fis);
        WebDriver driver = new ChromeDriver();
        driver.switchTo().window(driver.getWindowHandle());
        FileWriter writer = new FileWriter("/Users/Tig/IdeaProjects/WebCrawler/Resources/CrawledContent.csv");

        System.out.println("Page info loaded");
        driver.get(pageInfo.getProperty("URL"));
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        Select carSelector = new Select(driver.findElement(By.id("drpTyreTypeCloneSB")));
        Select tyreWidthSelector = new Select(driver.findElement(By.id("drpTyreWidthSB")));
        carSelector.selectByValue("Car");
        for (int i = 0; i < tyreWidthSelector.getOptions().size(); i++) {
            tyreWidthSelector.selectByIndex(i);
            Select crossSectionSelector = new Select(driver.findElement(By.id("drpTyreCrossSectionSB")));
            driver.manage().timeouts().implicitlyWait(5,TimeUnit.SECONDS);
            String tyreWidth = tyreWidthSelector.getFirstSelectedOption().getText();
            for (int j = 0; j < crossSectionSelector.getOptions().size(); j++) {
                Thread.sleep(5000);
                crossSectionSelector.selectByIndex(j);
                Select tyreDiameterSelector = new Select(driver.findElement(By.id("drpTyreDiameterSB")));
                String crossSection = crossSectionSelector.getFirstSelectedOption().getText();
                driver.manage().timeouts().pageLoadTimeout(10,TimeUnit.SECONDS);
                for (int k = 0; k < tyreDiameterSelector.getOptions().size(); k++) {
                    tyreDiameterSelector.selectByIndex(k);
                    String tyreDiameter = tyreDiameterSelector.getFirstSelectedOption().getText();
                    driver.findElement(By.xpath(".//*[@id='content-groesse']/div[3]/span[2]/button")).click();
                    Thread.sleep(5000);
                    List<WebElement> brandNames = driver.findElements(By.className("secondHeaderStyle"));
                    List<WebElement> prices = driver.findElements(By.xpath(".//meta[@itemprop='price']"));
                    for (int l = 0; l < brandNames.size() && l < prices.size(); l++) {
                        driver.manage().timeouts().implicitlyWait(5,TimeUnit.SECONDS);
                        System.out.println(brandNames.get(l).getText());
                        driver.manage().timeouts().implicitlyWait(5,TimeUnit.SECONDS);
                        System.out.println(prices.get(l).getAttribute("content"));

                        writer.append(tyreWidth);
                        writer.append(',');
                        writer.append(crossSection);
                        writer.append(',');
                        writer.append(tyreDiameter);
                        writer.append(',');
                        writer.append(brandNames.get(l).getText());
                        writer.append(',');
                        writer.append(prices.get(l).getAttribute("content"));
                        writer.append('\n');
                        writer.flush();
                    }
                    brandNames.clear();
                    Thread.sleep(5000);
                }
            }

        }
        driver.close();
    }
}
