package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SeleniumParser {

    public List<Apartment> getApartments(String url) throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.managed_default_content_settings.images", 2);
        ChromeOptions op = new ChromeOptions();
        op.setExperimentalOption("prefs", prefs);

        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        driver.get(url);

        WebElement cookieBar = driver.findElement(By.cssSelector("div[data-name=CookieAgreementBar]"));
        cookieBar.findElement(By.tagName("button")).click();
        Thread.sleep(200);

        List<Apartment> apartmentsList = new ArrayList<>();
        List<WebElement> divs = driver.findElements(By.cssSelector("article[data-name=CardComponent]"));

        for (WebElement div : divs) {
            String textPrice = div.findElement(By.cssSelector("span[data-mark=MainPrice]")).getText();
            WebElement areaElement = div.findElement(By.cssSelector("div[data-name=LinkArea]"));
            List<WebElement> addresses = areaElement.findElements(By.cssSelector("a[data-name=GeoLabel]"));
            StringBuilder address = new StringBuilder();
            for (WebElement singleAddress : addresses) {
                address.append(singleAddress.getText()).append(" ");
            }
            String titleComponentSpan = areaElement.findElement(By.cssSelector("div[data-name=TitleComponent]")).getText();
            titleComponentSpan = getNormalString(titleComponentSpan);
            int amountOfRooms = getAmountOfRooms(titleComponentSpan);
            String flour = getFlour(titleComponentSpan);
            double sqr = getSqr(titleComponentSpan);
            long price = getPrice(textPrice);
            String link = div.findElement(By.cssSelector("a")).getAttribute("href");
            WebElement button = div.findElement(By.cssSelector("button[data-mark=PhoneButton]"));
            button.click();
            Thread.sleep(10);
            String phoneNumber = div.findElement(By.cssSelector("span[data-mark=PhoneValue]")).getText();
            Apartment apartment = new Apartment(
                    amountOfRooms, sqr, flour, address.toString(), price, link, phoneNumber
            );
            apartmentsList.add(apartment);
        }
        driver.close();
        return apartmentsList;
    }

    private String getNormalString(String s2) {
        Pattern pattern = Pattern.compile("(\\d\\-комн.\\s)(кв\\.)?(апарт\\.)?");
        Matcher matcher = pattern.matcher(s2);
        String result = "";
        while (matcher.find()) {
            result = s2.substring(matcher.start());
        }
        return result;
    }

    private int getAmountOfRooms(String str) {
        char c = str.charAt(0);
        return Character.getNumericValue(c);
    }


    private double getSqr(String str) {
        String[] split = str.split("\\s");
        String sqr = split[2].replace(",", ".");
        return Double.parseDouble(sqr);
    }

    private String getFlour(String str) {
        String str1 = "м²";
        String substring = str.substring(str.indexOf(str1) + str1.length(), str.indexOf("этаж"));
        substring = substring.replaceAll(",", "").trim();
        return substring;
    }

    private long getPrice(String str) {
        str = str.replaceAll("\\s", "");
        str = str.replace("₽", "");
        return Long.parseLong(str);
    }
}
