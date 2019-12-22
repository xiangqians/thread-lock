package com.xiangqian.ts.util;

import java.util.UUID;

/**
 * @author xiangqian
 * @date 15:29 2019/12/22
 */
public class StringUtils {

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String trim(Object obj) {
        return obj == null ? "" : obj.toString().trim();
    }

}
