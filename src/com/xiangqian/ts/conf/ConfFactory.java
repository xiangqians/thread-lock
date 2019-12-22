package com.xiangqian.ts.conf;

import com.xiangqian.ts.util.IOUtils;
import com.xiangqian.ts.util.NumberUtils;
import com.xiangqian.ts.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.startup.Tomcat;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author xiangqian
 * @date 15:14 2019/12/22
 */
@Slf4j
public class ConfFactory {

    public static void main(String[] args) {
        TomcatConf tomcatConf = get(TomcatConf.class);
        log.debug("tomcatConf=" + tomcatConf);
    }

    /**
     * 获取Conf
     *
     * @param clazz 缓存的Class Key
     * @param <T>
     * @return
     */
    public static <T extends Conf> T get(Class<T> clazz) {
        Conf conf = null;
        if ((conf = CacheMap.INSTANCE.get(clazz)) == null) {
            synchronized (CacheMap.class) {
                if ((conf = CacheMap.INSTANCE.get(clazz)) == null) {
                    conf = newInstance(clazz);
                    init(conf);
                    CacheMap.INSTANCE.put(clazz, conf);
                }
            }
        }
        return (T) conf;
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            return (T) clazz.newInstance();
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    private static void init(Conf conf) {
        String prefix = null;
        if (conf.getClass().isAnnotationPresent(ConfValue.class)) {
            prefix = conf.getClass().getAnnotation(ConfValue.class).value() + ".";
        } else {
            prefix = "";
        }

        Field[] fieldArr = conf.getClass().getDeclaredFields();
        int len = fieldArr.length;
        Field field = null;
        for (int i = 0; i < len; i++) {
            field = fieldArr[i];
            if (field.isAnnotationPresent(ConfValue.class)) {
                String value = PropertiesConf.get(prefix, field.getAnnotation(ConfValue.class).value());
                if (value == null) {
                    continue;
                }

                Class<?> fieldClass = (Class) field.getGenericType();
//                log.debug("fieldClass=" + fieldClass);
                Object fieldValue = null;

                // 基本数据类型
                if (fieldClass.isPrimitive()) {
                    switch (fieldClass.getSimpleName()) {
                        case "int":
                            try {
                                fieldValue = NumberUtils.convert2int(value);
                            } catch (NumberFormatException e) {
                                continue;
                            }
                            break;

                        case "float":
                            try {
                                fieldValue = NumberUtils.convert2float(value);
                            } catch (NumberFormatException e) {
                                continue;
                            }
                            break;

                        case "double":
                            try {
                                fieldValue = NumberUtils.convert2double(value);
                            } catch (NumberFormatException e) {
                                continue;
                            }
                            break;

                        default:
                            break;
                    }
                }
                // 引用数据类型
                else if (fieldClass == String.class) {
                    fieldValue = StringUtils.trim(value);
                }

                if (fieldValue == null) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    field.set(conf, fieldValue);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
    }

    private static class CacheMap {
        private static final Map<Class<?>, Conf> INSTANCE;

        static {
            INSTANCE = new HashMap();
        }
    }

    private static class PropertiesConf {
        private static final Properties PROPERTIES;

        static {
            InputStream is = null;
            String filename = "task-scheduling-config.properties";
            try {
                PROPERTIES = new Properties();
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
                PROPERTIES.load(is);
            } catch (Exception e) {
                log.error("加载[" + filename + "]配置文件异常：", e);
                throw new Error(e);
            } finally {
                IOUtils.quietlyClosed(is);
            }
        }

        /**
         * @param prefix 前缀
         * @param suffix 后缀
         * @return
         */
        private static String get(String prefix, String suffix) {
            return PROPERTIES.getProperty(prefix + suffix);
        }
    }


}
