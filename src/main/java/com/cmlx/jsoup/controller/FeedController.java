package com.cmlx.jsoup.controller;

import com.cmlx.jsoup.pojo.dto.UrlDto;
import com.cmlx.jsoup.service.IFeedService;
import com.cmlx.jsoup.tools.commons.AppUrlConstant;
import com.cmlx.jsoup.tools.util.HttpUtil;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Desc
 * @Author cmlx
 * @Date 2019-9-23 0023 15:18
 */
@RequestMapping("feed")
@RestController
@Validated
public class FeedController {

    @Autowired
    private IFeedService iFeedService;

    @RequestMapping("resolveUrl")
    public UrlDto resolveUrl(String url) throws Exception {
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
            if (basePath.contains("music.163")) {
                String s = HttpUtil.get(url);
                doc = Jsoup.parse(s);
            } else {
                WebClient browser = new WebClient();
                browser.getOptions().setCssEnabled(false);
                browser.getOptions().setJavaScriptEnabled(false);
                browser.setAjaxController(new NicelyResynchronizingAjaxController());
                browser.getOptions().setThrowExceptionOnScriptError(false);
                browser.getOptions().setUseInsecureSSL(true);
                HtmlPage htmlPage = browser.getPage(url);
                browser.waitForBackgroundJavaScript(10);
                doc = Jsoup.parse(htmlPage.asXml());
            }
            //TODO 针对淘宝口令做特殊处理(淘宝分享口令不能用于解析，需要拿到js里面的真正地址)
            if (basePath.contains("m.tb.cn") || basePath.contains("taobao.com")) {
                Elements script = doc.getElementsByTag("script");
                for (Element element : script) {
                    String data = element.data();
                    if (data.contains("extraData")) {
                        title = data.substring(data.indexOf("{\"title\":\"") + 10, data.indexOf("\",\"priceL"));
                        image = data.substring(data.indexOf("\",\"pic\":\"") + 9, data.indexOf("\"};"));
                        break;
                    }
//                    boolean flag = false;
                    /*取得JS变量数组*/
//                    String[] data = element.data().toString().split("var");
                    /*取得单个JS变量*/
//                    for (String s : data) {
                    /*获取满足条件的JS变量*/
//                        if (s.contains("url")) {
//                            url = s.split("=", 1)[0];
//                            if (url.contains("taobao.com")) {
//                                url = url.substring(url.indexOf("\'") + 1, url.lastIndexOf("\'"));
//                                flag = true;
//                                break;
//                            }
//                        }
//                    }
//                    if (flag) {
//                        break;
//                    }
                }
                UrlDto urlDto = new UrlDto().setUrlTitle(title).setUrlImage(image).setUrl(url);
                if (urlDto.getUrlTitle() == null && urlDto.getUrlImage() == null) {
                    throw new RuntimeException("URL解析失败");
                }
                return urlDto;
            }
            // TODO 针对微博做特殊（微博使用自定义图片，且抓取的内容是content）
            if (basePath.contains("m.weibo.cn") || basePath.contains("weibo.com")) {
                Elements script = doc.getElementsByTag("script");
                for (Element element : script) {
                    String data = element.data();
                    if (data.contains("$render_data")) {
                        if (data.contains("textLength")) {
                            int i = data.indexOf("\"text\": \"");
                            int i1 = data.lastIndexOf("\"text\": \"");
                            if (i == i1) {
                                title = data.substring(data.lastIndexOf("\"text\": \"") + 9, data.indexOf("\",\n" +
                                        "        \"textLength\""));
                            }
                            if (i != i1) {
                                title = data.substring(data.lastIndexOf("\"text\": \"") + 9, data.indexOf("\",\n" +
                                        "            \"textLength\""));
                            }
                            break;
                        }
                    }
                }
                //TODO 微博的转发动态拿不到content，就拿微博的title
                if (title == null || "".equals(title)) {
                    Elements titles = doc.select("meta[content]");
                    for (int i = 0; i < titles.size(); i++) {
                        String name = titles.get(i).attr("name");
                        if (name != null && name.equals("description")) {
                            title = titles.get(i).attr("content");
                            break;
                        }
                    }
                }
                if (title == null || "".equals(title)) {
                    title = doc.title();
                }
                image = AppUrlConstant.WEIBO_IMAGE;
                UrlDto urlDto = new UrlDto().setUrlTitle(title).setUrlImage(image).setUrl(url);
                if (urlDto.getUrlTitle() == null && urlDto.getUrlImage() == null) {
                    throw new RuntimeException("URL解析失败");
                }
                return urlDto;
            }
            title = doc.title();
            // TODO 哔哩哔哩图标
            if (basePath.contains("b23.tv") || basePath.contains("bilibili.com")) {
                image = AppUrlConstant.BILIBILI_IMAGE;
            }
            // TODO 酷狗图标
            if (basePath.contains("kugou")) {
                image = AppUrlConstant.KUGOU_IMAGE;
            }
            // TODO 网易云音乐图标
            if (basePath.contains("music.163")) {
                image = AppUrlConstant.WANGYI_IMAGE;
            }

            Elements images = doc.select("img[src]");
            if (image == null && images.size() > 0) {
                String src1 = images.get(0).attr("src");
                if (src1 != null && (!src1.contains("//") || src1.contains("data:")) && !src1.contains("data:image/")) {
                    src1 = basePath + src1;
                }
                System.out.println("src : " + src1);
                image = src1;
                if (!image.contains("http") && !image.contains("https") && !src1.contains("data:image/")) {
                    image = "http://" + image;
                }
                if (image.contains("////")) {
                    image = image.replace("////", "//");
                }
            }
        } catch (
                Exception e) {
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
