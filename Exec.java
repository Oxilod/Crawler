package Main;

import java.io.File;
import java.io.IOException;

public class Exec{

    public static void main(String[] args) throws IOException, InterruptedException
    {

        File file = new File("");
        System.setProperty("webdriver.chrome.driver", file.getAbsolutePath() + "/Resources/chromedriver");
        Selector selector = new Selector();
        selector.goToURL();
        selector.tyreSelectorByIndex(0,0,0);
        Thread.sleep(5000);
    }
}

