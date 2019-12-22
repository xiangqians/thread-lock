package com.xiangqian.ts.util;

/**
 * @author xiangqian
 * @date 15:25 2019/12/22
 */
public class IOUtils {

    public static void quietlyClosed(AutoCloseable... autoCloseables) {
        if (autoCloseables == null) {
            return;
        }
        for (AutoCloseable autoCloseable : autoCloseables) {
            if (autoCloseable != null) {
                try {
                    autoCloseable.close();
                } catch (Exception e) {
                }
            }
        }
    }

}
