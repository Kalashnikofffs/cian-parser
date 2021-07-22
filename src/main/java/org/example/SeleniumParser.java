package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.ss.formula.functions.T;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SeleniumParser {

    private final String login;
    private final String password;
    Set<Cookie> cookieSet;
    int counter = 0;
    List<WebDriver> driverList = new ArrayList<>();
    List<Thread> threadList = new ArrayList<>();

    public SeleniumParser(String login, String password) {
        this.login = login;
        this.password = password;
    }

    //test account
//    private final String login = "xohoke9968@godpeed.com";
//    private final String password = "Qwerty123456";

    public List<Apartment> getApartments(String url) throws InterruptedException {
        List<Apartment> apartmentsList = new ArrayList<>();
        List<String> links = new ArrayList<>();
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        ChromeOptions op = new ChromeOptions();
//        op.setExperimentalOption("excludeSwitches", "disable-popup-blocking");
        // set up webdriver
        WebDriver driver = getWebDriver(url);

        //check if there any pop ups and close it
        checkIfAnyIframes(driver);

        //accept cookies
        acceptCookies(driver);
        Thread.sleep(200);

        // check is user is logged in or not and log in if not
        logIn(driver);

        List<WebElement> divs = driver.findElements(By.cssSelector("article[data-name=CardComponent]"));

        iterateDivsAndParseApartments(apartmentsList, divs, links);

        openLinksAndAddToFavoriteAndAddComments(url, links, driver);

        driver.close();


        return apartmentsList;
    }

    private void openLinksAndAddToFavoriteAndAddComments(String url, List<String> links, WebDriver oldDriver) {
        for (int i = 0; i < 8; i++) {
            threadList.add(
                    new Thread(() -> {
                        driverList.add(getWebDriver(links.get(counter++)));
                    }));

        }
        for (Thread tr:
             threadList) {
            tr.start();
        }
        for (String link : links) {

            WebDriver driver = driverList.get(links.indexOf(link));
            cookieSet = oldDriver.manage().getCookies();
            driver.get(link);
            for (Cookie ck :
                    cookieSet) {
                driver.manage().addCookie(ck);
            }
            driver.get(link);

            checkIfThereA3DTourOnThePageAndClosePopUp(driver);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.findElement(By.cssSelector("div[data-name=CommentsButton]")).click();
            String xpathString = "//div[contains(@class, 'comment')]";
            List<WebElement> elements = driver.findElements(By.xpath(xpathString));
            WebElement commentElement = elements.get(1);
            WebElement textarea = commentElement.findElement(By.tagName("textarea"));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String price = driver.findElement(By.cssSelector("span[itemprop=price")).getText();
            textarea.clear();
            textarea.sendKeys(getId(link) + " - " + price + " " + LocalDate.now());
            List<WebElement> buttons = driver.findElements(By.tagName("button"));
            for (WebElement button : buttons) {
                if (button.getText().equalsIgnoreCase("Сохранить")) {
                    button.click();
                    break;
                }
            }
            driver.close();
        }
        ;

    }


    private void checkIfThereA3DTourOnThePageAndClosePopUp(WebDriver driver) {
        if (driver.findElements(By.cssSelector("div[data-name=TourModal]")).size() > 0) {
            List<WebElement> elements = driver.findElements(By.cssSelector("div[data-name=TourModal"));
            for (WebElement element : elements) {
                WebElement button = element.findElement(By.tagName("button"));
                button.click();
                break;
            }
        }
    }

    private void acceptCookies(WebDriver driver) {
        WebElement cookieBar = driver.findElement(By.cssSelector("div[data-name=CookieAgreementBar]"));
        cookieBar.findElement(By.tagName("button")).click();
    }

    private void iterateDivsAndParseApartments(List<Apartment> apartmentsList, List<WebElement> divs, List<String> links) throws InterruptedException {
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
            String id = getId(link);
            links.add(link);
            WebElement button = div.findElement(By.cssSelector("button[data-mark=PhoneButton]"));
            button.click();
            Thread.sleep(10);
            String phoneNumber = div.findElement(By.cssSelector("span[data-mark=PhoneValue]")).getText();
            Apartment apartment = new Apartment(
                    id, amountOfRooms, sqr, flour, address.toString(), price, link, phoneNumber
            );
            apartmentsList.add(apartment);
        }
    }

    private void checkIfAnyIframes(WebDriver driver) {
        try {
            if (driver.findElements(By.tagName("iframe")).size() > 0) {
                for (WebElement iframe : driver.findElements(By.tagName("iframe"))) {
                    iframe.findElement(By.tagName("button")).click();
                    break;
                }
            }
        } catch (Exception ex) {

        }
    }

    private WebDriver getWebDriver(String url) {
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        driver.get(url);
        return driver;
    }

    private void logIn(WebDriver driver) throws InterruptedException {
        if (isLogInPossible(driver)) {
            driver.findElement(By.cssSelector("a[id=login-btn]")).click();
            Thread.sleep(500);
            if (driver.findElements(By.cssSelector("button[data-mark=SwitchToEmailAuth]")).size() > 0) {
                driver.findElement(By.cssSelector("button[data-mark=SwitchToEmailAuth]")).click();
                Thread.sleep(500);
            }
            driver.findElement(By.cssSelector("input[name=username]")).sendKeys(login);
            Thread.sleep(500);
            driver.findElement(By.cssSelector("button[data-mark=ContinueAuthBtn]")).click();
            Thread.sleep(500);
            driver.findElement(By.cssSelector("input[name=password]")).sendKeys(password);
            Thread.sleep(500);
            driver.findElement(By.cssSelector("button[data-mark=ContinueAuthBtn]")).click();
            Thread.sleep(500);
        }
    }

    private String getId(String link) {
        String regex = "/\\d+/";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(link);
        String id = "";
        while (matcher.find()) {
            id = link.substring(matcher.start() + 1, matcher.end() - 1);
        }
        return id;
    }

    private boolean isLogInPossible(WebDriver driver) {
        return driver.findElements(By.cssSelector("a[id=login-btn]")).size() > 0;
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
