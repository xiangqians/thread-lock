package com.xiangqian.ts;

import com.xiangqian.ts.conf.TomcatConf;
import com.xiangqian.ts.tomcat.TomcatServer;
import com.xiangqian.ts.tomcat.servlet.HelloHttpServlet;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * sun.misc.Unsafe native源码地址：
 * http://hg.openjdk.java.net/jdk9/sandbox/hotspot/file/eb9d79764139/src/share/vm/prims/unsafe.cpp
 * Unsafe_Park
 *
 * @author xiangqian
 * @date 15:01 2019/12/22
 */
public class Main {

    public static void main(String[] args) {
        try {
            test2();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void test3() throws Exception {

    }

    // 线程挂起超时时间，单位ns, 1s = 1000 000 000 ns
    private static final long TIMEOUT = 1000 * 1000 * 1000 * 6L;

    public static void test2() throws Exception {

        Thread thread = new Thread() {
            @Override
            public void run() {
                int count = 0;
                while (count < 6) {
                    System.out.println("[" + Thread.currentThread().getName() + "] count=" + count);
                    LockSupport.parkNanos(TIMEOUT);
                    count++;
                }
            }
        };
        thread.start();
        thread.suspend();

        TimeUnit.SECONDS.sleep(2);

        LockSupport.unpark(thread);


        System.out.println("end");
    }

    public static void test1() throws Exception {
        TomcatServer tomcatServer = new TomcatServer();
        tomcatServer.registry("hello", new HelloHttpServlet(), "/hello");
        tomcatServer.startup();
    }

}
