package com.xiangqian.ts;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * 使用CAS自定义Java线程锁
 *
 * @author xiangqian
 * @date 12:39 2019/12/21
 */
public final class CASLock {

    // 锁state
    private volatile int state;

    // 当前拥有资源的线程
    private volatile Thread ownerThread;

    // 工作线程缓存
    private volatile ThreadCache workThreadCache;

    // 线程挂起超时时间，单位ns, 1s = 1000 000 000 ns
    private final long TIMEOUT = 1000 * 1000 * 1000 * 2L;

    public CASLock() {
        this.state = 0;
        this.workThreadCache = new ThreadCache();
    }

    public void lock() {
        // 获取锁成功
        if (CAS.stateTo1(this)) {
            Thread currentThread = Thread.currentThread();
            workThreadCache.addHolding(currentThread);
            ownerThread = currentThread;
        }
        // 获取锁失败，挂起线程并放入线程缓存区等待调度
        else {
            Thread currentThread = Thread.currentThread();
            if (workThreadCache.addWaiting(currentThread)) {
                CAS.UNSAFE.park(false, TIMEOUT);
//                System.err.println("线程超时等待被唤醒 " + currentThread.getName());
                lock();
            }
        }
    }

    public void unlock() {
        Thread currentThread = Thread.currentThread();
        if (ownerThread == currentThread) {

            // 更新已经执行了的缓存线程节点状态
            workThreadCache.beenExecuted(currentThread);

            // 获取缓存中的工作线程
            ownerThread = workThreadCache.get();

            // 唤醒缓存中工作线程
            if (ownerThread != null) {
//                System.err.println("唤醒线程 " + currentThread.getName() + " to " + ownerThread.getName());
                CAS.UNSAFE.unpark(ownerThread);
            }
            // 释放锁资源
            else {
//                System.err.println("释放锁资源 by " + currentThread.getName());
                CAS.stateTo0(this);
            }
        }
    }

    private static class CAS {
        // unsafe
        static final Unsafe UNSAFE;

        // state offset
        static final long STATE_OFFSET;

        static {
            try {
                Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                UNSAFE = (Unsafe) field.get(null);

                STATE_OFFSET = UNSAFE.objectFieldOffset(CASLock.class.getDeclaredField("state"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }

        static boolean stateTo1(CASLock casLock) {
            return UNSAFE.compareAndSwapInt(casLock, STATE_OFFSET, 0, 1);
        }

        static boolean stateTo0(CASLock casLock) {
            return UNSAFE.compareAndSwapInt(casLock, STATE_OFFSET, 1, 0);
        }
    }

}
