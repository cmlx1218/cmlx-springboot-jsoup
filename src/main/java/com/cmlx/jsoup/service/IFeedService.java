package com.cmlx.jsoup.service;

import com.cmlx.jsoup.pojo.dto.UrlDto;

/**
 * @Desc
 * @Author cmlx
 * @Date 2019-10-11 0011 17:04
 */
public interface IFeedService {

    /**
     * 解析普通Url
     *
     * @param url
     * @return
     * @throws Exception
     */
    UrlDto resolveUrlOrdinary(String url) throws Exception;

}
