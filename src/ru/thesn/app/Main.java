package ru.thesn.app;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.*;


public class Main {
    public final static String APPLICATION_ID = "a9c4f99af0828197f1626664db92e2cb";
    public static final String E25_ADDRESS = "http://forum.worldoftanks.ru/index.php?/topic/1646365-%D0%BC%D0%B0%D1%80%D0%B0%D1%84%D0%BE%D0%BD-e-25/";
    public final static String BROWSER_NAME = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36";
    public final static String E25_API_FORMAT = "http://api.worldoftanks.ru/wot/account/tanks/?application_id=%s&account_id=%s&tank_id=55569";
    public final static String API_SEARCH_FORMAT = "http://api.worldoftanks.ru/wot/account/list/?application_id=%s&search=%s";

    public static final Map<String, User> MAP = new HashMap<>();
    public static final List<User> USER_LIST = new ArrayList<>();
    public static final List<User> ADMIN_LIST = new ArrayList<>();

    public static void main(String[] args) {
        try {
            Document document = Jsoup.connect(E25_ADDRESS).userAgent(BROWSER_NAME).referrer("none").get();

            int page = 0;

            while (document != null) {
                page++;
                Elements posts = document.body().getElementsByClass("post_block");
                for (Element post : posts) {
                    String nick = post.select("span[itemprop^=creator]").first().text();
                    if (nick.equals("WGNews")) continue;

                    if (MAP.containsKey(nick)){
                        User user = MAP.get(nick);
                        user.setMessages(user.getMessages() + 1);
                    } else {
                        User user = new User();
                        user.setName(nick);
                        user.setMessages(1);

                        String group = post.getElementsByClass("group_title").first().text();

                        if (group.equals("Разработчики")) user.setIsAdmin(true);

                        JSONAdapter array = getJSONAdapterFromPAPI(String.format(API_SEARCH_FORMAT, APPLICATION_ID, nick));

                        int userID = 0;

                        for(int i = 0; i < array.getArray().size(); i++) {
                            String testName = array.fromArray(i).getString("nickname");
                            if (nick.equals(testName))
                                userID = array.fromArray(i).getInt("account_id");
                            if (i > 25 || userID > 0) break;
                        }



                        if (userID > 0) {
                            JSONAdapter j = getJSONAdapterFromPAPI(String.format(E25_API_FORMAT, APPLICATION_ID, userID));
                            JSONAdapter arr = j.fromObject(String.valueOf(userID));
                            if (arr.getArray().size() == 1) {
                                user.setBattles(arr.fromArray(0).fromObject("statistics").getInt("battles"));
                                user.setPercent(arr.fromArray(0).fromObject("statistics").getInt("wins") * 100 / user.getBattles());
                            }
                        } else {
                            user.setBattles(-1);
                        }

                        MAP.put(nick, user);
                    }
                }

                if (page % 10 == 0)
                    System.out.print("|");
                if (page % 200 == 0)
                    System.out.println();

                if (document.outerHtml().contains("Следующая страница")) {
                    String next = document.getElementsByAttributeValue("title", "Следующая страница").first().attr("href");
                    while(true) {
                        try{
                            document = Jsoup.connect(next).userAgent(BROWSER_NAME).referrer("none").get();
                            break;
                        }catch(Exception e){
                            Thread.sleep(15000);
                        }
                    }
                } else document = null;
            }


            for(Map.Entry<String, User> entry: MAP.entrySet()){
                User user = entry.getValue();
                if (user.isAdmin())
                    ADMIN_LIST.add(user);
                else
                    USER_LIST.add(user);
            }

            Collections.sort(USER_LIST);
            Collections.sort(ADMIN_LIST);

            System.out.println();

            System.out.println("СПИСОК РАЗРАБОТЧИКОВ \n\t Имя \t\t\t\t Сообщения \t   Боев на Е25 / % побед");

            for(User user: ADMIN_LIST) {
                System.out.println(user);
            }

            System.out.println();

            System.out.println("СПИСОК ПОЛЬЗОВАТЕЛЕЙ \n\t Имя \t\t\t\t Сообщения \t   Боев на Е25 / % побед");

            int e25 = 0;
            int other = 0;

            for(User user: USER_LIST) {
                System.out.println(user);
                if (user.getBattles() > 0)
                    e25++;
                else
                    other++;
            }

            System.out.println();

            System.out.println("Высказалось владельцев Е25: " + e25);
            System.out.println("Остальные: " + other);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONAdapter getJSONAdapterFromPAPI(String urlStr) throws ParseException, IOException, PAPIError, JSONException {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line);
        br.close();

        JSONParser parser = new JSONParser();

        JSONAdapter obj = new JSONAdapter(parser.parse(sb.toString()));
        if (!obj.getString("status").equals("ok"))
            throw new PAPIError(obj.fromObject("error").getString("message"));
        return obj.fromObject("data");
    }

}