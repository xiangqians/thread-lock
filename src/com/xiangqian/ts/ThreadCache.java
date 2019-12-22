package com.xiangqian.ts;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 线程缓存
 *
 * @author xiangqian
 * @date 12:39 2019/12/21
 */
public class ThreadCache {

    // 锁state
    private volatile int state;

    // 头结点
    private Node headNode;

    // 尾结点
    private Node tailNode;

    // 当前遍历节点
    private Node currentNode;

    // 存放Node节点，hash数据结构在不发生哈希冲突的情况下，时间复杂度为O(1)
    private Map<Thread, Node> nodeMap;

    public ThreadCache() {
        this.state = 0;
        this.headNode = null;
        this.tailNode = null;
        this.currentNode = null;
        this.nodeMap = new HashMap<>(16, 0.95f);
    }

    /**
     * 添加线程到等待区
     *
     * @param thread
     * @return
     */
    public boolean addWaiting(Thread thread) {
        return add(thread, State.WAITING_ACQUIRE_LOCK);
    }

    /**
     * 添加线程到持有区
     *
     * @param thread
     * @return
     */
    public boolean addHolding(Thread thread) {
        return add(thread, State.HOLDING_LOCK);
    }

    /**
     * 添加缓存线程
     *
     * @param thread
     * @param state
     * @return
     */
    private boolean add(Thread thread, State state) {
        CAS.stateTo1(this);
        try {
            // modify node
            Node node = nodeMap.get(thread);
            if (node != null) {
                if (node.state == State.HOLDING_LOCK) {
                    return false;
                }

                node.state = state;
                return true;
            }

            // init node
            node = new Node(thread, state);
            nodeMap.put(thread, node);

            //
            if (tailNode == null) {
                headNode = node;
                tailNode = node;

            } else {
                Node next = node;
                tailNode.next = next;
                tailNode = next;
            }

            return true;
        } finally {
            CAS.stateTo0(this);
        }
    }

    /**
     * 更新已经执行了的缓存线程节点状态
     *
     * @param thread
     * @return
     */
    public boolean beenExecuted(Thread thread) {
        CAS.stateTo1(this);
        try {
            Node node = nodeMap.get(thread);
            if (node != null && node.state == State.HOLDING_LOCK) {
                node.state = State.BEEN_EXECUTED;
                return true;
            }
            return false;
        } finally {
            CAS.stateTo0(this);
        }
    }

    /**
     * 获取将要唤醒的线程
     *
     * @return
     */
    public Thread get() {
        CAS.stateTo1(this);
        try {
            if (currentNode == null) {
                currentNode = headNode;
            }

            Node targetNode = null;
            while (currentNode != null) {
                if (currentNode.thread.isAlive() && currentNode.state == State.WAITING_ACQUIRE_LOCK) {
                    targetNode = currentNode;
                }

                // 指向下一个节点
                currentNode = currentNode.next;

                if (targetNode != null) {
                    targetNode.state = State.HOLDING_LOCK;
                    return targetNode.thread;
                }
            }
            return null;

        } finally {
            CAS.stateTo0(this);
        }
    }

    /**
     * 线程节点
     */
    private class Node {
        Thread thread;
        State state;
        Node next;

        Node(Thread thread, State state) {
            this.thread = thread;
            this.state = state;
        }

        @Override
        public String toString() {
            return "Node{" + "thread=" + thread + ", state=" + state + '}';
        }
    }

    /**
     * 线程状态
     */
    private enum State {

        BEEN_EXECUTED(-1), // 已执行
        WAITING_ACQUIRE_LOCK(0), // 等待获取锁
        HOLDING_LOCK(1), // 持有锁
        ;
        final int value;

        State(int value) {
            this.value = value;
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

                STATE_OFFSET = UNSAFE.objectFieldOffset(ThreadCache.class.getDeclaredField("state"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }

        static void stateTo1(ThreadCache threadCache) {
            while (!UNSAFE.compareAndSwapInt(threadCache, STATE_OFFSET, 0, 1))
                ;
        }

        static boolean stateTo0(ThreadCache threadCache) {
            return UNSAFE.compareAndSwapInt(threadCache, STATE_OFFSET, 1, 0);
        }
    }

}
