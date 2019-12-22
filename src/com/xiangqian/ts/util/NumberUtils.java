package com.xiangqian.ts.util;

/**
 * @author xiangqian
 * @date 15:29 2019/12/22
 */
public class NumberUtils {

    public static int convert2int(Object obj, int defaultValue) {
        try {
            return convert2int(obj);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public static int convert2int(Object obj) throws NumberFormatException {
        return Integer.parseInt(StringUtils.trim(obj));
    }

    public static double convert2double(Object obj, double defaultValue) {
        try {
            return convert2double(obj);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public static double convert2double(Object obj) throws NumberFormatException {
        return Double.parseDouble(StringUtils.trim(obj));
    }

    public static float convert2float(Object obj, float defaultValue) {
        try {
            return convert2float(obj);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public static float convert2float(Object obj) throws NumberFormatException {
        return Float.parseFloat(StringUtils.trim(obj));
    }

}
