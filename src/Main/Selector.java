package Main;


import com.opencsv.CSVWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;



public class Selector{
    private String tyreProfile;
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

    void navigate(String url) { //Navigates to the specific page and brings the window to the front. Overloaded method
        driver.get(url);
        driver.switchTo().window(driver.getWindowHandle());
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }
    void navigate() { //Navigates to the specific page from pageInfo.properties and brings the window to the front.
        driver.get(urlGet);
        driver.switchTo().window(driver.getWindowHandle());
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    private void csvFileCreator() throws IOException, InterruptedException { // Creates the csvFile.
        WebElement finder;
        finder = findElement(By.xpath(".//*[@id='content-groesse']/div[3]/span[2]/button")); // Locate the Reifen Anzige button
        if (finder != null){
            finder.click();// Click the Reifen anzige button.
        }
        finder = findElement(By.xpath(".//*[@id='bottomArticleCount']/ul/li[3]/button")); // Locate the Alles link.
        if (finder != null){
            finder.click(); // Click the Alles link, so that all the tyres are displayed on one single page.
        }

        for (int l = 0; l < driver.findElements(By.className("secondHeaderStyle")).size() && l < driver.findElements(By.xpath(".//meta[@itemprop='price']")).size(); l++) { // This method creates the String that is later added to the CSV file.
            String brandName = null;
            String price = null;
            String[] tyreWidthCross;
            String[] csvWriter;

            int attempts = 0;
            Thread.sleep(400);
            while(attempts<4){ // I did this to treat a case that was frequently appearing, throwing a Stale element error.
                try {
                    brandName = driver.findElements(By.className("secondHeaderStyle")).get(l).getText();// Get the brand names and make a list with them
                    price = driver.findElements(By.xpath(".//meta[@itemprop='price']")).get(l).getAttribute("content"); // Get all the prices from the page and make a list of them
                    tyreProfile = driver.findElements(By.xpath("//p[contains(@class, 'thirdHeaderStyle inset')]")).get(l).getText(); // Gets the tire profile to be added to the CSV file
                } catch(Exception e) {
                    System.out.println("Stale element csvFileCreator");
                }
                attempts++;
            }
            Thread.sleep(400);
            String[] splitStr = tyreProfile.trim().split("\\s+");//Creates a String array with all the values from the tyreProfile and is separated by spaces.
            if (splitStr[0].toUpperCase().contains("X")){
                tyreWidthCross = splitStr[0].toUpperCase().split("X");
            }else if (splitStr[0].length() <= 3){
                tyreWidthCross = splitStr[0].split(" ");

            }else {
                tyreWidthCross = splitStr[0].split("/");
            }
            if (tyreWidthCross.length < 2) {
                csvWriter = new String[]{tyreWidthCross[0], "no value", splitStr[1], splitStr[2], brandName, price}; // Make a string of the content that has to be written to the file
            }else {
                csvWriter = new String[]{tyreWidthCross[0], tyreWidthCross[1], splitStr[1], splitStr[2], brandName, price}; // Make a string of the content that has to be written to the file

            }

            csvWrote.writeNext(csvWriter); // Wirte to the CSV file
            csvWrote.flush();
        }
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
    public void tyreSelector(String width, int crossSectionq, int diameter) throws InterruptedException, IOException { // This method is created so the user can input from where the crawling will start.
        int i = 0;
        int j = 0;
        int k = 0;

        Thread.sleep(500);
        List<WebElement> widthList = driver.findElement(By.id("drpTyreWidthSB")).findElements(By.tagName("option")); // Creates a List with all the elements that are found in the Tyre Width dropdown.
        try { // This is done to check if the value that the user inputs is found in the list.
            i = widthList.indexOf(driver.findElement(By.id("drpTyreWidthSB")).findElement(By.xpath("//option[@value='" + width + ".00']"))); //Gets the index of the element that user inputs
        }catch (Exception e){
            System.out.println("Invalid width the crawling will start from the beggining"); // If the value isn't found the message is printed and the crawling will start from 0.
            tyreSelector();
        }
        Thread.sleep(500);
        List<WebElement> crossSectionList = driver.findElement(By.id("drpTyreCrossSectionSB")).findElements(By.tagName("option")); //Gets the index of the element that user inputs
        try {
            j = crossSectionList.indexOf(driver.findElement(By.id("drpTyreCrossSectionSB")).findElement(By.xpath("//option[@value='" + crossSectionq + ".00']")));//A value will be selected from the list
        }catch (Exception e){
            System.out.println("Invalid Cross Section"); // If the value isn't found the message is printed and the crawling will start from 0.
            tyreSelector();
        }
        Thread.sleep(500);
        List<WebElement> diameterList = driver.findElement(By.id("drpTyreDiameterSB")).findElements(By.tagName("option")); //Gets the index of the element that user inputs
        try {
            k = diameterList.indexOf(driver.findElement(By.id("drpTyreDiameterSB")).findElement(By.xpath("//option[@value='R" + diameter + "']"))); //A value will be selected from the list
        }catch (Exception e){
            System.out.println("Invalid diameter");// If the value isn't found the message is printed and the crawling will start from 0.
            tyreSelector();
        }
        while (i < widthList.size()) { // This will select the tyre profile.
            driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
            Select tyreWidthSelector = new Select(driver.findElement(By.id("drpTyreWidthSB"))); // Find and create a select for the Tyre Width dropdown
            tyreWidthSelector.selectByIndex(i); // Select the element using the assigned index.
            Select crossSectionSelector = new Select(findElement(By.id("drpTyreCrossSectionSB"))); // Find and create a select for the Cross Section. This will also help get the list size.
            while (j < crossSectionSelector.getOptions().size()) {
                Thread.sleep(500);
                crossSectionSelector.selectByIndex(j); // Select the option based on the iteration.
                Select tyreDiameterSelector = new Select(findElement(By.id("drpTyreDiameterSB"))); // Find and create a select for the Tyre Diameter. Helps compare the size of the dropdown
                while (k < tyreDiameterSelector.getOptions().size()) {
                    Thread.sleep(500);
                    tyreDiameterSelector.selectByIndex(k);// Select the option based on the iteration.
                    csvFileCreator();
                    k++;
                    driver.navigate().back(); // Goes back to the first page. It's not the best solution but it's the fastest that I could get.
                    driver.navigate().back();
                    Thread.sleep(500);
                    tyreSelector(i, j, k); // Calls the overloaded method giving it the index for each element.
                }
                j++;
                tyreSelector(i, j, 0);// If all the other options have been iterated it will start with an increased value for the Cross Section and from 0 for the Diameter.

            }
            i++;
            tyreSelector(i, 0, 0); // If all the other options have been iterated through, it will start with an increased value for the Tyre width and from 0 for the Cross Section and the diameter.
        }
    }

    void tyreSelector() throws InterruptedException, IOException {
        int i = 0;
        int j = 0;
        int k = 0;
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        int tyreWidthSize = new Select(driver.findElement(By.id("drpTyreWidthSB"))).getOptions().size();
        while (i < tyreWidthSize) {
            Thread.sleep(500);
            Select tyreWidthSelector = new Select(driver.findElement(By.id("drpTyreWidthSB")));
            tyreWidthSelector.selectByIndex(i); // Select the option based on the iteration.
            driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
            Select crossSectionSelector = new Select(findElement(By.id("drpTyreCrossSectionSB")));
            while (j < crossSectionSelector.getOptions().size()) {
                Thread.sleep(500);
                crossSectionSelector.selectByIndex(j); // Select the option based on the iteration.
                Select tyreDiameterSelector = new Select(findElement(By.id("drpTyreDiameterSB")));
                while (k < tyreDiameterSelector.getOptions().size()) {
                    Thread.sleep(500);
                    tyreDiameterSelector.selectByIndex(k);// Select the option based on the iteration.
                    csvFileCreator();
                    k++;
                    driver.navigate().back();
                    driver.navigate().back();
                    Thread.sleep(500);
                    tyreSelector(i, j, k);
                }
                j++;
                tyreSelector(i, j, 0);

            }
            i++;
            tyreSelector(i, 0, 0);
        }
    }

    private void tyreSelector(int i, int j, int k) throws InterruptedException, IOException {
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        int tyreWidthSize = new Select(findElement(By.id("drpTyreWidthSB"))).getOptions().size();
        while (i < tyreWidthSize){
            Thread.sleep(1000);
            Select tyreWidthSelector = new Select(findElement(By.id("drpTyreWidthSB")));
            tyreWidthSelector.selectByIndex(i); // Select the option based on the iteration.
            Thread.sleep(500);
            Select crossSectionSelector = new Select(findElement(By.id("drpTyreCrossSectionSB")));
            while (j < crossSectionSelector.getOptions().size()){
                Thread.sleep(500);
                crossSectionSelector.selectByIndex(j); // Select the option based on the iteration.
                Select tyreDiameterSelector = new Select(findElement(By.id("drpTyreDiameterSB")));
                while (k < tyreDiameterSelector.getOptions().size()){
                    Thread.sleep(1000);
                    tyreDiameterSelector.selectByIndex(k);// Select the option based on the iteration.
                    Thread.sleep(500);
                    csvFileCreator();
                    k++;
                    Thread.sleep(500);
                    driver.navigate().back();
                    driver.navigate().back();
                    Thread.sleep(500);
                    tyreSelector(i,j,k);
                }
                j++;
                tyreSelector(i,j,0);
            }
            i++;
            tyreSelector(i,0,0);
        }
        driver.close();
    }
}
