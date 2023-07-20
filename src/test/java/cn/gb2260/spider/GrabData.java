package cn.gb2260.spider;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 爬取数据
 */
public class GrabData {

    // 民政部
    String mzb = "https://www.mca.gov.cn/mzsj/xzqh/2022/202201xzqh.html";
    // 统计局
    String tjj = "http://www.stats.gov.cn/sj/tjbz/tjyqhdmhcxhfdm/2022/index.html";
    String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1823.86";
    String Cookie = "wzws_sessionid=gjdlZDJkMIAxLjIwMi43MS4xMIE4NzJiOTGgZLiNog==; SF_cookie_1=72991020; wzws_cid=ca4e35f2b701329414f92ecc9479cc5bbb3219f33e8140dc267a43e675ed17276f99d09131580eda62cedfa420db6be6d9c8f3e68c5d1ad5b09a409f405215e5eace69f4c5a8ab0e0092b6c215cb31d0";

    @Test
    public void testMzb() {

        // 请求连接
        String html = HttpRequest.get(mzb).header("User-Agent", ua).execute().body();
        // 解析
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        Element table = body.getElementsByTag("table").get(0);
        Elements trs = table.getElementsByTag("tr");
        Map<String, String> xzqMap = new LinkedHashMap<>();
        for (Element tr : trs) {
            String xzqCode = null, xzqName = null;
            Elements tds = tr.getElementsByTag("td");
            for (Element td : tds) {
                String text = td.text();
                if (StrUtil.isBlank(text)) {
                    continue;
                }
                if (NumberUtil.isInteger(text)) {
                    xzqCode = text;
                } else {
                    xzqName = text;
                }
            }
            if (xzqCode == null || xzqName == null) {
                continue;
            }
            xzqMap.put(xzqCode.trim(), xzqName.trim());
        }
        System.out.println(JSONUtil.toJsonPrettyStr(xzqMap));
    }

    @Test
    public void testTjj() {
        String basePath = tjj.substring(0, tjj.lastIndexOf("/") + 1);
        // 请求连接
        String html = HttpRequest.get(tjj).header("User-Agent", ua).header("Cookie", Cookie).execute().body();
        // 解析
        Document doc = Jsoup.parse(html);
        Elements trs = doc.selectXpath("//tr[@class='provincetr']");
        Map<String, String> xzqMap = new LinkedHashMap<>();
        for (Element tr : trs) {
            Elements tds = tr.getElementsByTag("td");
            for (Element td : tds) {
                Element a = td.getElementsByTag("a").get(0);
                String href = a.attr("href");
                String xzqName = a.text();
                String xzqCode = FileUtil.mainName(href) + "0000";
                xzqMap.put(xzqCode, xzqName);

                // shiURL
                String shiUrl = basePath + href;
                html = HttpRequest.get(shiUrl).header("User-Agent", ua).header("Cookie", Cookie).execute().body();
                Document shiDoc = Jsoup.parse(html);
                Elements cityTrs = shiDoc.selectXpath("//tr[@class='citytr']");
                for (Element cityTr : cityTrs) {
                    Element cityTd = cityTr.getElementsByTag("td").get(1);
                    Element cityA = cityTd.getElementsByTag("a").get(0);
                    String cityAHref = cityA.attr("href");
                    String cityName = cityA.text();
                    String cityCode = FileUtil.mainName(cityAHref);
                    xzqMap.put(cityCode + "00", cityName);

                    // xianURl
                    String xianUrl = basePath + cityAHref;
                    html = HttpRequest.get(xianUrl).header("User-Agent", ua).header("Cookie", Cookie).execute().body();
                    Document xianDoc = Jsoup.parse(html);
                    Elements xianTrs = shiDoc.selectXpath("//tr[@class='countytr']");
                    for (Element xianTr : xianTrs) {
                        Element xianTd = xianTr.getElementsByTag("td").get(1);
                        Element xiana = xianTd.getElementsByTag("a").get(0);
                        String xianherf = xiana.attr("href");
                        String xianname = xiana.text();
                        String xiancode = FileUtil.mainName(xianherf);
                        xzqMap.put(xiancode, xianname);
                    }
                }
            }
        }
        System.out.println(JSONUtil.toJsonPrettyStr(xzqMap));
    }

    @Test
    public void readCSV() {
        // csv来源 https://github.com/xiangyuecn/AreaCity-JsSpider-StatsGov
        String csv = "D:/ok_data_level3.csv";
        String txt = "D:/202306.txt";
        CsvReader reader = CsvUtil.getReader();
        reader.setContainsHeader(true);
        //从文件中读取CSV数据
        CsvData data = reader.read(FileUtil.file(csv));
        List<CsvRow> rows = data.getRows();
        //遍历行
        List<String> lines = new ArrayList<>();
        for (CsvRow csvRow : rows) {
            Console.log(csvRow.getRawList());
            String xzqCode = csvRow.getByName("ext_id");
            String xzqName = csvRow.getByName("ext_name");
            if (xzqCode.length() < 6) {
                continue;
            }
            lines.add(xzqCode.substring(0, 6) + "\t" + xzqName);
        }
        FileUtil.writeLines(lines, txt, StandardCharsets.UTF_8);
    }
}
