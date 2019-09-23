package com.cmlx.jsoup.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Desc
 * @Author cmlx
 * @Date 2019-9-23 0023 15:20
 */
@Data
@Accessors(chain = true)
public class UrlDto {

    private String url;
    private String urlTitle;
    private String urlImage;

}