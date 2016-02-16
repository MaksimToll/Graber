package kovalenko.elance.aligrabber;

import com.google.gson.Gson;
import kovalenko.elance.aligrabber.behancerabber.Data;
import kovalenko.elance.aligrabber.behancerabber.Designer;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for parssing Behalace site
 */
public class BehanceGrabber extends Thread {

    public static final int expectedImages = 9;
    static final String category = "{\"108\":\"Advertising\",\"3\":\"Animation\",\"4\":\"Architecture\"," +
            "\"5\":\"Art Direction\",\"130\":\"Automotive Design\",\"109\":\"Branding\",\"133\":\"Calligraphy\"," +
            "\"9\":\"Cartooning\",\"124\":\"Character Design\",\"12\":\"Cinematography\",\"15\":\"Computer Animation\"" +
            ",\"19\":\"Copywriting\",\"20\":\"Costume Design\",\"21\":\"Crafts\",\"137\":\"Creative Direction\"," +
            "\"23\":\"Culinary Arts\",\"122\":\"Digital Art\",\"27\":\"Digital Photography\",\"28\":\"Directing\"," +
            "\"110\":\"Drawing\",\"32\":\"Editorial Design\",\"33\":\"Engineering\",\"35\":\"Entrepreneurship\"," +
            "\"36\":\"Exhibition Design\",\"37\":\"Fashion\",\"93\":\"Fashion Styling\",\"38\":\"Film\"," +
            "\"112\":\"Fine Arts\",\"40\":\"Furniture Design\",\"41\":\"Game Design\",\"43\":\"Graffiti\"," +
            "\"44\":\"Graphic Design\",\"131\":\"Icon Design\",\"48\":\"Illustration\",\"49\":\"Industrial Design\"," +
            "\"50\":\"Information Architecture\",\"51\":\"Interaction Design\",\"52\":\"Interior Design\"," +
            "\"53\":\"Jewelry Design\",\"54\":\"Journalism\",\"55\":\"Landscape Design\",\"59\":\"MakeUp Arts (MUA)\"," +
            "\"63\":\"Motion Graphics\",\"64\":\"Music\",\"66\":\"Packaging\",\"67\":\"Painting\",\"69\":\"Pattern Design\"," +
            "\"70\":\"Performing Arts\",\"73\":\"Photography\",\"74\":\"Photojournalism\",\"78\":\"Print Design\"," +
            "\"79\":\"Product Design\",\"123\":\"Programming\",\"136\":\"Retouching\",\"86\":\"Sculpting\",\"87\":\"Set Design\"," +
            "\"118\":\"Sound Design\",\"91\":\"Storyboarding\",\"135\":\"Street Art\",\"95\":\"Textile Design\"," +
            "\"126\":\"Toy Design\",\"97\":\"Typography\",\"132\":\"UI\\/UX\"," +
            "\"120\":\"Visual Effects\",\"102\":\"Web Design\",\"103\":\"Web Development\",\"105\":\"Writing\"}";
    final static Logger logger = Logger.getLogger(BehanceGrabber.class);
    final static String SAVE_FILE = "/" + "save_state.txt";
    public static Map<String, String> categoryMap = new TreeMap<>();
    public static String urlForStart = Parser.BASE;
    static Properties properties = new Properties();
    public Main main;
    String lastProjectlink = "";
    int countParsedProject = 0;

    private int projectCounter = 0;

    public BehanceGrabber(Main main) {
        this.main = main;
        Properties sysProperties = System.getProperties();
        String user = (String) sysProperties.get("user.home");
        Parser.defaultLocation = user + "/Behance";
        properties.setProperty("targetDirectory", this.main.targetDirectory.getText());
        if (properties.getProperty("targetDirectory") != null && !properties.getProperty("targetDirectory").isEmpty()) {
            Parser.defaultLocation = properties.getProperty("targetDirectory");
        }


        Parser.categoryString = this.main.selectCat.getSelectedItem().toString();
        Parser.categoryNumb = categoryMap.get(Parser.categoryString);
        urlForStart = (findSavedLink() != null) ? findSavedLink() : Parser.BASE;


        File f = new File(user + "/" + "Behance");
        if (!f.exists())
            f.mkdir();
        this.main.start.setText("Stop");
        this.main.start.setIcon(new ImageIcon(this.getClass().getResource("/kovalenko/elance/aligrabber/resources/cross-button.png")));
        lastProjectlink = findLastSavedProject();
        Parser.designers.clear();
        projectCounter = 0;
    }

    public static void fillGategories() {
        String p = "(\\d+)\"\\s*:\\s*\"\\s*(|[a-zA-Z\\s*\\(\\)-]+)\"";

        Pattern pattern = Pattern.compile(p);
        Matcher mathces = pattern.matcher(category);
        kovalenko.elance.aligrabber.Category temp;
        while (mathces.find()) {
            String key = mathces.group(1);
            String category = mathces.group(2);
            categoryMap.put(category, key);

        }

    }

    public static void logMessage(Main main, String message, Logger log) {
        main.progress.append(message + "\n");
        log.debug(message);
    }

    public static String findLastSavedProject() {
        String resultLink = "";
        BufferedReader br = null;
        File saveState = new File(Parser.defaultLocation + "/" + Parser.categoryString + SAVE_FILE);
        if (saveState.exists()) {
            try {
                String sCurrentLine;
                br = new BufferedReader(new FileReader(saveState));
                while ((sCurrentLine = br.readLine()) != null) {

                    resultLink = Parser.getSecondLink(sCurrentLine);
                }
            } catch (IOException e) {
                // close connection and do nothing.
                logger.error("can`t load last saved ");
            } finally {
                try {
                    if (br != null) br.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
        return resultLink;
    }

    private void logMessage(String message) {
        main.progress.append(message + "\n");
        logger.debug(message);
    }

    public void run() {

        try {
            properties.setProperty("spaceSize", this.main.spaceBetweenImages.getValue().toString());
            properties.setProperty("imagesInRow", this.main.imagesInRow.getValue().toString());
            properties.setProperty("targetDirectory", this.main.targetDirectory.getText());
            properties.setProperty("backgroundColor", "" + this.main.backgroundColor.getBackground().getRGB());
            properties.setProperty("extractImagesFromDetails", this.main.extractImagesFromDetails.isSelected() ? "Y" : "N");
            properties.setProperty("ignoreImages", this.main.ignoreImages.isSelected() ? "Y" : "N");
            properties.setProperty("ignoreImagesWidth", this.main.ignoreImagesWidth.getValue().toString());
            properties.setProperty("ignoreImagesHeight", this.main.ignoreImagesWidth.getValue().toString());

            if (properties.getProperty("targetDirectory") != null && !properties.getProperty("targetDirectory").isEmpty()) {
                Parser.defaultLocation = properties.getProperty("targetDirectory");
            } else {
                Properties sysProperties = System.getProperties();
                String user = (String) sysProperties.get("user.home");
                Parser.defaultLocation = user + "/Behance";

                File f = new File(user + "/" + "Behance");
                if (!f.exists())
                    f.mkdir();
            }

            parse();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    public void parse() {

        logMessage("Begin ");

        try {

            ThreadPoolExecutor executorService;

            do {
                urlForStart = (findSavedLink() != null) ? findSavedLink() : Parser.BASE;
                Parser.getAllDesigner(urlForStart, Parser.categoryNumb);

                logMessage("End iteration . Total parsed " + Parser.designers.size() + " links. try to get pictures");
                ArrayList<Designer> authors = Parser.designers;
                lastProjectlink = findLastSavedProject();
                int iter = tryGetIterator(authors);

                executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
                for (; iter < authors.size(); iter++) {
                    projectCounter++;
                    Designer d = authors.get(iter);
                    if (isInterrupted() |  Parser.isFinish ) {
                        break;
                    }
                    AsyncImageCreator imageWorker = new AsyncImageCreator(main, d);
                    executorService.execute(imageWorker);
                    if (iter != 0 && iter % 10 == 0) {
                        logger.info("wait for ending 10 tasks.");

                        while (executorService.getActiveCount() != 0) { // code wait when completed all threads
//                            System.out.println("WTF--------------------------");
                        }
                    }
                    if(isInterrupted()){
                        executorService.shutdownNow();
                        break;
                    }

                }

                long begin = System.currentTimeMillis();
                executorService.shutdown();
                System.out.println("Time take a ----------------------------- "+(System.currentTimeMillis() - begin));
                // final boolean done = executorService.awaitTermination(1, TimeUnit.MINUTES);
                boolean done = executorService.awaitTermination(1, TimeUnit.MINUTES);


                System.err.print("First iteration complete");
                countParsedProject = Parser.designers.size();
                if (isInterrupted() || Parser.isFinish) {
                    break;
                }
                Parser.designers.clear();

            } while (true);
            executorService.shutdown();
        } catch (IOException e) {
            logMessage(e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.main.start.setText("Start");
        this.main.state.setText("");
        this.main.start.setIcon(new ImageIcon(this.getClass().getResource("/kovalenko/elance/aligrabber/resources/tick-button.png")));
        this.main.changeTargetDirectory.setEnabled(true);
        this.main.pruneTargetDirectory.setEnabled(true);
        logMessage("Parsing completed.  Count of project is "+ projectCounter );

    }

    private int tryGetIterator(List<Designer> projects) {
        int iter = 0;
        String tmp = "";

        if (!lastProjectlink.isEmpty()) {
            for (Designer d : projects
                    ) {
                tmp = (lastProjectlink.trim().equals(d.getName().trim())) ? d.getName() : "";
//                                System.out.println(d.getName());
                if (tmp != "") {
                    iter = projects.indexOf(d);
                    iter++;

                }
            }

        }
        return iter;
    }

    private String findSavedLink() {
        String resultLink = null;
        BufferedReader br = null;
        File saveState = new File(Parser.defaultLocation + "/" + Parser.categoryString + SAVE_FILE);
        if (saveState.exists()) {
            try {

                String sCurrentLine;
                br = new BufferedReader(new FileReader(saveState));
                while ((sCurrentLine = br.readLine()) != null) {
                    resultLink = Parser.getFirstLink(sCurrentLine);
                }

            } catch (IOException e) {
                logMessage("Can`t load last saved order " + e.getMessage());
                // close connection and do nothing.
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            } finally {
                try {
                    if (br != null) br.close();
                } catch (IOException ex) {

                    ex.printStackTrace();
                }
            }

        }
        return resultLink;
    }

    public BufferedImage tryLoadImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            InputStream in = url.openStream();
            BufferedImage image = ImageIO.read(in);
            return image;
        } catch (IOException e) {
            logMessage("can`t load image -> " + imageUrl + "\n error message --> " + e.getMessage());


        } catch (Exception exception) {
            logger.error(exception.getMessage() + " " + imageUrl);
        }
        return null;
    }


}

class Parser {

    // https://www.behance.net/search?ts=1455270300&ordinal=60&per_page=12&field=5&content=projects&sort=featured_date&time=week&location_id=

    public static final String TS = "/search?ts=";
    public static final String ORDINAL = "&ordinal=";
    public static final String PER_P = "&per_page=";
    public static final String LAST_PART = "&content=projects&sort=appreciations&time=all&location_id=";
//    public static final String LAST_PART = "&content=projects&sort=featured_date&time=week&location_id=";
    public static final String CATEGORY = "&field=";
    //
    public static final String BASE = "https://www.behance.net";
    public static final int PER_PAGE = 12;
    public static final int TIMEAUT = 30000;
    final static Logger logger = Logger.getLogger(Parser.class);
    //for creation request
    public static String defaultLocation = System.getProperties().getProperty("user.home") + "/Behance";
    public static ArrayList<Designer> designers = new ArrayList<>();
    public static int limitOfProject = 180;
    public static String categoryNumb = "108"; // ===>  Advertising
    public static String categoryString = "Advertising";
    public static boolean isFinish = false;



    public static void getAllDesigner(String startUrl, String category) throws IOException {
        int ordinal = 0;
        if (startUrl.contains(ORDINAL)) {
            Pattern pattern = Pattern.compile("ordinal=(\\d+)");
            Matcher matcher = pattern.matcher(startUrl);
            if (matcher.find()) {
                ordinal = Integer.parseInt(matcher.group(1));
            }
        }

        if (category == null) {
            category = "";
        }
        String timestamp = Parser.getTimestamp(startUrl);
        String url = "";
        int from = ordinal;
        Set<Designer> designersSet = new ConcurrentSkipListSet<>();
////////////////////////////////
        String lastPrj = BehanceGrabber.findLastSavedProject();
        Designer desForSaving = new Designer();

        if (!designersLatIteration.isEmpty() && lastPrj != "") {
            url = new StringBuilder().append(BASE).append(TS).append(timestamp).append(ORDINAL)
                    .append(ordinal).append(PER_P).append(PER_PAGE).append(CATEGORY)
                    .append(category).append(LAST_PART).toString();
            designersSet = getProjectLink(url);

            for (Designer des : designersSet) {
                if (des.getName().equals(lastPrj)) {
                    desForSaving = des;
                }
            }

            int index = designersLatIteration.indexOf(desForSaving);
            for (int i = 0; i < index && index > 0; i++) {
                designersSet.remove(designersLatIteration.get(i));
            }
            Iterator<Designer> iterator = designersLatIteration.iterator();
            while(iterator.hasNext()){
                Designer designer = iterator.next();
                if(designer.getCountry()!=null){
                    designersSet.remove(designer);
                    iterator.remove();
                }
            }
            designers.addAll(designersSet);
            ordinal +=PER_PAGE;
        }
/////////////////////////////////

        for (; ordinal < from + limitOfProject; ordinal += PER_PAGE) { // TODO change it to more logical value, ONLY for test
            url = new StringBuilder().append(BASE).append(TS).append(timestamp).append(ORDINAL)
                    .append(ordinal).append(PER_P).append(PER_PAGE).append(CATEGORY)
                    .append(category).append(LAST_PART).toString();
//            url = BASE + TS + timestamp + ORDINAL + ordinal + PER_P + PER_PAGE + CATEGORY + category + LAST_PART;
            designersSet = getProjectLink(url);
            designers.addAll(designersSet); // return links on projects and field designers
        }
        designersLatIteration = new ArrayList<>(designersSet);

        tempCountDuplicate();


    }

    private static void tempCountDuplicate(){
        Set<Designer> nodDuplicate = new HashSet<>();
        for (Designer d: designers) {
            nodDuplicate.add(d);
        }
        System.err.print(" count duplicate in designers " +(designers.size()-nodDuplicate.size())/2);
        System.err.print(" count designers " + designers.size() );
    }

    private static ArrayList<Designer> designersLatIteration = new ArrayList<>();

    public static Connection.Response getResponseFromUrl(String url, int timeout) {
        try {
            Connection con = HttpConnection.connect(url);
            con
                    .method(Connection.Method.GET)
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Host", "www.behance.net")
                    .header("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:43.0) Gecko/20100101 Firefox/43.0")
                    .header("X-Requested-With", "XMLHttpRequest").timeout(timeout)
                    .ignoreContentType(true);


            Connection.Response resp = con.execute();

            return resp;
        } catch (SocketTimeoutException ex) {
            logger.error("can`t connect to link " + url + "  " + ex.getMessage());

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;

    }

    public static String getTimestamp(String url) throws IOException {

        Connection.Response resp = getResponseFromUrl(url, TIMEAUT);

        Gson gson = new Gson();
        Data data = gson.fromJson(resp.body(), Data.class);
        return data.getTimestamp();
    }

    public static Set<Designer> getProjectLink(String url) {
        Designer designer = new Designer();
        Set<Designer> stringSet = new HashSet<>();
        try {

            Connection.Response resp = getResponseFromUrl(url, TIMEAUT);

            Gson gson = new Gson();
            Data data = gson.fromJson(resp.body(), Data.class);

            Document mainPage = Jsoup.parse(data.getHtml());
            Elements infos = mainPage.getElementsByClass("cover-info-stats");
            for (Element info : infos) {
                Element elem = info.getElementsByClass("projectName").first();
                Element elem2 = info.getElementsByClass("js-mini-profile").first();
                Element multiOwnter = info.getElementsByClass("multiple-owner-link").first();
                designer = new Designer();
                designer.setMainUrl(url);
                designer.setImageUrl(elem.attr("href"));
                Element name = multiOwnter != null ? multiOwnter : elem2.parent();
                designer.setName(name.attr("href"));
                stringSet.add(designer);
            }
            if (!designers.isEmpty() && designers.containsAll(stringSet)) {
                isFinish = true;
            }
//            designers.addAll(stringSet);
            return stringSet;

        } catch (Exception e) {
            saveLastLink(designer);
            logger.error("can`t download link" + e.getMessage());
        }
        return stringSet;
    }


    /**
     * accepts linc to project and return links of Images
     * and accepts link designer and change
     *
     * @throws IOException
     */
    public static Set<String> parseImageLinks(String url, Designer author) {

        Set<String> links = new HashSet<>();
        try {


            Connection.Response resp = getResponseFromUrl(url, TIMEAUT);

            String country = parseLocation(resp.body());
            author.setCountry(country);

            String t = "src\\s*\":\\s*\"(([^\"])+)\""; // take the part of url
            Pattern pattern = Pattern.compile(t);
            Matcher mathces = pattern.matcher(resp.body());
            while (mathces.find()) {
                String link = mathces.group().replaceAll("\\\\", "");

                if (link.contains("disp")) {//TODO change it. Better way to chose link
                    if (!links.contains(link.replaceAll("src\":\"", "").replaceAll("\"", ""))) {
                        links.add(link.replaceAll("src\":\"", "").replaceAll("\"", ""));
                    }

                }

            }
            logger.debug("Images Link is parsed = " + links.size());

            return links;
        } catch (Exception ex) {
            logger.error(" Can`t parse link.", ex);
            return links;
        }

    }

    public static String definitionLocation(Designer author) { // rewrite it

        File dir = new File(defaultLocation + "/" + categoryString + "/" + author.getCountry());
        Path path = dir.toPath();
        boolean isExist = false;
        if (Files.exists(path)) {
            return path.toString() + "/";
        }
        if (author.getCountry() != null && Files.notExists(path)) {
            try {
                isExist = dir.mkdirs();
            } catch (SecurityException e) {
                logger.error(" creating dir for ");
                e.printStackTrace();
            }

        }
        return isExist ? path.toString() + "/" : defaultLocation + "/default/";
    }


    private static String parseLocation(String body) {
        String p = "\"location\":\"([^\"]+)";
        String p2 = "[,|.]\\s*([a-zA-Z\\s-]+)$";
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(body);
        String res = null;
        if (matcher.find()) {
            String temp = matcher.group(1);
            pattern = Pattern.compile(p2);
            Matcher m2 = pattern.matcher(temp);
            if (m2.find()) {
                res = m2.group(1);
            }
        }
        return res;
    }


    public static String getNameFromLink(String url) {
        String p = ".*/([\\S]+)$";
        Pattern pattern = Pattern.compile(".*\\/([\\S]+)$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";


    }

    public static String getFirstLink(String url) {
        String p = "\\s*(\\S+)";
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }

        return "";


    }

    /**
     * find last saved project link
     * @param url
     * @return
     */
    public static String getSecondLink(String url) {
        String p = "\\s*\\S+\\s*:\\s+(\\S+)";
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";


    }

    public static synchronized void saveLastLink(Designer designer) {
        String location = Parser.definitionLocation(designer);
        File saveState = new File(defaultLocation + "/" + Parser.categoryString + "/" + "save_state.txt");

        try {
            if (!saveState.exists()) {
                File catalog = new File(location + Parser.categoryString);
                catalog.mkdir();
                saveState.createNewFile();
            }
            FileWriter fw = new FileWriter(saveState.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            String text = designer.getMainUrl();
            bw.write(text + " : " + designer.getName());
            bw.flush();


        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}
