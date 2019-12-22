package com.xiangqian.test;

import com.xiangqian.ts.CASLock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 测试自定义Java线程锁
 *
 * @author xiangqian
 * @date 14:17 2019/10/20
 */
public class CASLockTest {

    public static void main(String[] args) {
        try {
            int loopNumber = 100;
            int threadNumber = 100;

            for (int i = 0; i < 60; i++) {
                testCASLock(loopNumber, threadNumber);
            }

//            testCASLock(loopNumber, threadNumber);
//            testSyn(loopNumber, threadNumber);
//            testLock(loopNumber, threadNumber);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final boolean IS_DEBUG = false;

    public static void testCASLock(int loopNumber, int threadNumber) throws Exception {
        start(new CASLockTask(loopNumber), threadNumber);
    }

    public static void testSyn(int loopNumber, int threadNumber) throws Exception {
        start(new SynTask(loopNumber), threadNumber);
    }

    public static void testLock(int loopNumber, int threadNumber) throws Exception {
        start(new LockTask(loopNumber), threadNumber);
    }

    public static void start(Runnable r, int threadNumber) throws Exception {
        long beginTime = System.currentTimeMillis();

        Thread[] ts = new Thread[threadNumber];
        for (int i = 0; i < threadNumber; i++) {
            ts[i] = new Thread(r, ("t" + i));
            ts[i].start();
        }

        for (int i = 0; i < threadNumber; i++) {
            ts[i].join();
            if (IS_DEBUG) {
                System.err.println("join [" + ts[i].getName() + "] 执行结束");
            }
        }

        System.err.println("(r " + r.getClass().getSimpleName() + "), End of execution, time: " + (System.currentTimeMillis() - beginTime) + " ms");
    }

    public static class CASLockTask implements Runnable {
        private static final CASLock LOCK;

        static {
            LOCK = new CASLock();
        }

        private static volatile int count;

        private int loopNumber;

        public CASLockTask(int loopNumber) {
            this.loopNumber = loopNumber;
        }

        @Override
        public void run() {
            for (int i = 0; i < loopNumber; i++) {
                LOCK.lock();
                try {
                    if (IS_DEBUG) {
                        System.out.println("[" + Thread.currentThread().getName() + "] count=" + count++);
                    }
                } finally {
                    LOCK.unlock();
                }
            }
        }
    }

    public static class SynTask implements Runnable {
        private static final Object LOCK;

        static {
            LOCK = new Object();
        }

        private static volatile int count;

        private int loopNumber;

        public SynTask(int loopNumber) {
            this.loopNumber = loopNumber;
        }

        @Override
        public void run() {
            for (int i = 0; i < loopNumber; i++) {
                synchronized (LOCK) {
                    if (IS_DEBUG) {
                        System.out.println("[" + Thread.currentThread().getName() + "] count=" + count++);
                    }
                }
            }
        }
    }

    public static class LockTask implements Runnable {
        private static final Lock LOCK;

        static {
            LOCK = new ReentrantLock();
        }

        private static volatile int count;

        private int loopNumber;

        public LockTask(int loopNumber) {
            this.loopNumber = loopNumber;
        }

        @Override
        public void run() {
            for (int i = 0; i < loopNumber; i++) {
                LOCK.lock();
                try {
                    if (IS_DEBUG) {
                        System.out.println("[" + Thread.currentThread().getName() + "] count=" + count++);
                    }
                } finally {
                    LOCK.unlock();
                }
            }
        }
    }

}
