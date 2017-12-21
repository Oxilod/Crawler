package Main;


import com.opencsv.CSVWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;



class Selector{
    private String tyreDetails;
    private WebDriver driver = new ChromeDriver();
    private String urlGet;
    private CSVWriter csvWrote = null;

    Selector(){
        File file = new File(""); // Crates a point to allow us to build the relative path
        String currentDirectory;
        currentDirectory = file.getAbsolutePath(); // gets the path where the file is situated
        System.setProperty("webdriver.chrome.driver", currentDirectory + "/Resources/chromedriver");//This sets the route for the Chrome Driver
        FileInputStream fis; // Creates the File Input stream so the pageInfo.properties is loaded
        Properties url = new Properties(); // Initializes the Properties element to load the file and get the URL.
        try {// try/catch to load the properties. This is done to catch the IOException that the FileInputStream throws. This way we don't have to treat the IOEception each time
            fis = new FileInputStream(currentDirectory + "/Resources/pageInfo.properties"); // Loads the pageInfo.properties
            url.load(fis);
            urlGet = url.getProperty("URL"); // Get the URL value from the properties element

        } catch (Exception e){
            e.printStackTrace(); //Catch and print the IOException
        }
        String csvPath = new File("").getAbsolutePath().concat("/Resources/crawledContent.csv"); // Set relative path to CSV file.
        try { // try/catch to handle the FileWriter IoException.
            csvWrote = new CSVWriter(new FileWriter(csvPath));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    void goToURL(String url) { //Navigates to the specific page and brings the window to the front. Overloaded method
        driver.get(url);
        driver.switchTo().window(driver.getWindowHandle());
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    void goToURL() { //Navigates to the specific page from pageInfo.properties and brings the window to the front.
        driver.get(urlGet);
        driver.switchTo().window(driver.getWindowHandle());
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    private String[] tyreDetails(String tyreDetails, String brandName, String price) {
        String[] csvWriter;
        String[] tyreProfile = tyreDetails.trim().split("\\s+");
        String[] tyreWidthCross = tyreProfileSplitter(this.tyreDetails);
        if (tyreWidthCross.length < 2) {
            csvWriter = new String[]{tyreWidthCross[0], "no value",
                    tyreProfile[1], tyreProfile[2], brandName, price}; // Make a string of the content that has to be written to the file
        } else {
            csvWriter = new String[]{tyreWidthCross[0], tyreWidthCross[1], tyreProfile[1], tyreProfile[2], brandName, price}; // Make a string of the content that has to be written to the file
        }
        return csvWriter;
    }

    private String[] tyreProfileSplitter(String tyreProfile) {
        String[] tyreWidthCross;
        String[] splitStr = tyreProfile.trim().split("\\s+");
        if (splitStr[0].toUpperCase().contains("X")) {
            tyreWidthCross = splitStr[0].toUpperCase().split("X");
        } else if (splitStr[0].length() <= 3) {
            tyreWidthCross = splitStr[0].split(" ");

        } else {
            tyreWidthCross = splitStr[0].split("/");
        }
        return tyreWidthCross;
    }

    private void csvFileWriter() throws InterruptedException, IOException {
        for (int l = 0; l < driver.findElements(By.className("secondHeaderStyle")).size() && l < driver.findElements(By.xpath(".//meta[@itemprop='price']")).size(); l++) { // This method creates the String that is later added to the CSV file.
            String brandName = null;
            String price = null;
            int attempts = 0;

            while(attempts<4){ // I did this to treat a case that was frequently appearing, throwing a Stale element error.
                try {
                    brandName = driver.findElements(By.className("secondHeaderStyle")).get(l).getText();// Get the brand names and make a list with them
                    price = driver.findElements(By.xpath(".//meta[@itemprop='price']")).get(l).getAttribute("content"); // Get all the prices from the page and make a list of them
                    tyreDetails = driver.findElements(By.xpath("//p[contains(@class, 'thirdHeaderStyle inset')]")).get(l).getText(); // Gets the tire profile to be added to the CSV file
                } catch(Exception e) {
                    System.out.println("Stale element csvFileCreator");
                }
                attempts++;
            }
            csvWrote.writeNext(tyreDetails(tyreDetails, brandName, price)); // Wirte to the CSV file
            csvWrote.flush();
        }
    }

    private void searchAllClick() {
        WebElement finder;
        finder = findElement(By.xpath(".//*[@id='content-groesse']/div[3]/span[2]/button")); // Locate the Reifen Anzige button
        if (finder != null) {
            finder.click();// Click the Reifen anzige button.
        }
        finder = findElement(By.xpath(".//*[@id='bottomArticleCount']/ul/li[3]/button")); // Locate the Alles link.
        if (finder != null) {
            finder.click(); // Click the Alles link, so that all the tyres are displayed on one single page.
        }
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    private void csvFileCreator() throws IOException, InterruptedException { // Creates the csvFile.
        searchAllClick();
        csvFileWriter();
    }

    private WebElement findElement(By by){ // I've created this method to treat the Stale element issue that was appearing.
        int counter = 0;
        WebElement finder = null;
        while (counter < 4){ // This will try to select the element 4 times before giving up and printing the message.
            try {
                finder = driver.findElement(by);
            } catch (Exception e){
                System.out.println("Stale element find element");
            }
            counter++;
        }
        return finder;
    }
    private void selectIndex(Select selector, int index) throws InterruptedException {
        Thread.sleep(500);
            try {
                int attempts = 0;
                while (attempts <= 4){
                    selector.selectByIndex(index);
                    attempts++;
                }
            }catch (StaleElementReferenceException e){
                System.out.println("Stale element select Index");
            }catch(Exception e){
                System.out.println("Select Index Error");
                e.printStackTrace();
            }
        }

    private int getOptionsSize(Select selector) throws InterruptedException { // I've created this method to treat the Stale element issue that was appearing.
        int counter = 0;
        int size = 0;
        Thread.sleep(100);
        while (counter < 4) { // This will try to select the element 4 times before giving up and printing the message.
            try {
                size = selector.getOptions().size();
            } catch (Exception e) {
                System.out.println("Stale element find element");
            }
            counter++;
        }
        return size;
    }

    void tyreSelectorByIndex(int i, int j, int k) throws InterruptedException, IOException {
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        Select tyreWidthSelector = new Select(findElement(By.id("drpTyreWidthSB")));
        while (i < getOptionsSize(tyreWidthSelector)) {
            selectIndex(tyreWidthSelector, i);
            Thread.sleep(100);
            Select crossSectionSelector = new Select(findElement(By.id("drpTyreCrossSectionSB")));
            int crossSectionSize = getOptionsSize(crossSectionSelector);
            while (j < crossSectionSize) {
                selectIndex(crossSectionSelector, j);
                Thread.sleep(100);
                Select tyreDiameterSelector = new Select(findElement(By.id("drpTyreDiameterSB")));
                int tyreDiameterSize = getOptionsSize(tyreDiameterSelector);
                while (k < tyreDiameterSize) {
                    selectIndex(tyreDiameterSelector, k);
                    csvFileCreator();
                    k++;
                    goToURL(urlGet);
                    tyreSelectorByIndex(i,j,k);
                }
                j++;
                tyreSelectorByIndex(i,j,0);
            }
            i++;
            tyreSelectorByIndex(i,0,0);
        }
    }
}
