package com.cmlx.jsoup.service.impl;

import com.cmlx.jsoup.pojo.dto.UrlDto;
import com.cmlx.jsoup.service.IFeedService;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

/**
 * @Desc
 * @Author cmlx
 * @Date 2019-10-11 0011 17:05
 */
@Service
public class FeedService implements IFeedService {
    @Override
    public UrlDto resolveUrlOrdinary(String url) throws Exception {
        //设置官网地址
        String basePath = null;
        String[] split = url.split("/");
        if (split.length == 0 || split.length == 1) {
            basePath = split[0];
        }
        if (url.contains("//")) {
            basePath = split[2];
        }
        Document doc = null;
        String title = null;
        String image = null;
        if (!url.contains("http") && !url.contains("https")) {
            url = "http://" + url;
        }
        try {
            WebClient browser = new WebClient(BrowserVersion.CHROME);
            browser.getOptions().setCssEnabled(false);
            browser.getOptions().setJavaScriptEnabled(true);
            browser.getOptions().setThrowExceptionOnScriptError(false);
            browser.getOptions().setUseInsecureSSL(true);
            HtmlPage htmlPage = browser.getPage(url);
            browser.waitForBackgroundJavaScript(100000);

            doc = Jsoup.parse(htmlPage.asXml());
            title = doc.title();
            Elements images = doc.select("img[src~=(?i)\\.(png|jpe?g|gif|webp)]");
            if (images.size() > 0) {
                String src1 = images.get(0).attr("src");
                if (src1 != null && (!src1.contains("//") || src1.contains("data:"))) {
                    src1 = basePath + src1;
                }
                System.out.println("src : " + src1);
                image = src1;
                if (!image.contains("http") && !image.contains("https")) {
                    image = "http://" + image;
                }
                if (image.contains("////")) {
                    image = image.replace("////", "//");
                }
            }
        } catch (Exception e) {
            //接收到错误链接（404页面）
            throw new RuntimeException("URL解析失败");
        }

        UrlDto urlDto = new UrlDto().setUrlTitle(title).setUrlImage(image).setUrl(url);
        if (urlDto.getUrlTitle() == null && urlDto.getUrlImage() == null) {
            //接收到错误链接（404页面）
            throw new RuntimeException("URL解析失败");
        }
        return urlDto;
    }
}
