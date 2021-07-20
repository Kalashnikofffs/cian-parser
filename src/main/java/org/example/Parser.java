package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Data
public class Parser {

    public List<Apartment> getApartments() throws IOException {

        String url = "https://spb.cian.ru/cat.php?currency=2&deal_type=sale&engine_version=2&minprice=3000000&offer_type=flat&region=2&room1=1&room2=1";

        List<Apartment> apartmentList = new ArrayList<>();

        Connection connect = Jsoup.connect(url);
        Connection.Response response = connect.execute();
        Elements article = response.parse().select("article");

        for (Element element : article) {
            Elements select = element.select("div[data-name=LinkArea]");
            for (Element element1 : select) {
                String link = element1.select("a").attr("href");
                String address = element1.select("a[data-name=GeoLabel]").text();
                String priceElement = element1.select("span[data-mark=MainPrice]").text();
                String span = element1.select("div[data-name=TitleComponent]").select("span").text();
                span = getNormalString(span);
                int amountOfRooms = getAmountOfRooms(span);
                String flour = getFlour(span);
                double sqr = getSqr(span);
                long price = getPrice(priceElement);
                Apartment apartment = new Apartment("", amountOfRooms, sqr, flour, address, price, link, null);
                apartmentList.add(apartment);
//                System.out.println(apartment);
                Elements phoneNumber = element.getElementsByTag("button");
                for (Element element2 : phoneNumber) {
                    String buttonText = element2.select("button[data-mark=PhoneButton]").text();
//                    if (!buttonText.isEmpty()) {
//                        System.out.println(element2.select("button[data-mark=PhoneButton]"));
//                        System.out.println(buttonText);
//                    }
                }
            }
            break;
        }
        return apartmentList;
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
