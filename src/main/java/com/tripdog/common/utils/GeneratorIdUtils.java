package com.tripdog.common.utils;

import java.util.UUID;

/**
 * 雪花算法ID生成器工具类
 *
 * ID结构：
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 *
 * 1位符号位（固定0）+ 41位时间戳 + 5位数据中心ID + 5位机器ID + 12位序列号 = 64位
 *
 * @author: iohw
 * @date: 2025/9/26 18:47
 * @description: 基于Twitter的雪花算法实现分布式唯一ID生成
 */
public class GeneratorIdUtils {

    /**
     * 起始时间戳 (2021-01-01 00:00:00)
     */
    private static final long START_TIMESTAMP = 1609459200000L;

    /**
     * 机器ID所占的位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 数据中心ID所占的位数
     */
    private static final long DATACENTER_ID_BITS = 5L;

    /**
     * 支持的最大机器ID，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 支持的最大数据中心ID，结果是31
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /**
     * 序列在ID中占的位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 机器ID向左移12位
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据中心ID向左移17位(12+5)
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间戳向左移22位(5+5+12)
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * 工作机器ID(0~31)
     */
    private static long workerId = 1L;

    /**
     * 数据中心ID(0~31)
     */
    private static long datacenterId = 1L;

    /**
     * 毫秒内序列(0~4095)
     */
    private static long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private static long lastTimestamp = -1L;

    /**
     * 静态同步锁
     */
    private static final Object LOCK = new Object();

    /**
     * 初始化雪花算法参数
     *
     * @param workerId     工作机器ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public static void init(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                String.format("Worker ID can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                String.format("Datacenter ID can't be greater than %d or less than 0", MAX_DATACENTER_ID));
        }
        GeneratorIdUtils.workerId = workerId;
        GeneratorIdUtils.datacenterId = datacenterId;
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }
    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public static long nextId() {
        synchronized (LOCK) {
            long timestamp = timeGen();

            // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，这个时候应当抛出异常
            if (timestamp < lastTimestamp) {
                throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                        lastTimestamp - timestamp));
            }

            // 如果是同一时间生成的，则进行毫秒内序列
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & SEQUENCE_MASK;
                // 毫秒内序列溢出
                if (sequence == 0) {
                    // 阻塞到下一个毫秒，获得新的时间戳
                    timestamp = tilNextMillis(lastTimestamp);
                }
            }
            // 时间戳改变，毫秒内序列重置
            else {
                sequence = 0L;
            }

            // 上次生成ID的时间截
            lastTimestamp = timestamp;

            // 移位并通过或运算拼到一起组成64位的ID
            return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
        }
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected static long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    protected static long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 获取下一个ID的字符串形式
     *
     * @return ID字符串
     */
    public static String nextIdStr() {
        return String.valueOf(nextId());
    }

    /**
     * 解析雪花ID，返回ID的各个组成部分
     *
     * @param id 雪花ID
     * @return ID解析结果
     */
    public static SnowflakeIdInfo parseId(long id) {
        long timestamp = (id >> TIMESTAMP_LEFT_SHIFT) + START_TIMESTAMP;
        long datacenterId = (id >> DATACENTER_ID_SHIFT) & ~(-1L << DATACENTER_ID_BITS);
        long workerId = (id >> WORKER_ID_SHIFT) & ~(-1L << WORKER_ID_BITS);
        long sequence = id & SEQUENCE_MASK;

        return new SnowflakeIdInfo(timestamp, datacenterId, workerId, sequence);
    }

    /**
     * 雪花ID信息类
     */
    public static class SnowflakeIdInfo {
        private final long timestamp;
        private final long datacenterId;
        private final long workerId;
        private final long sequence;

        public SnowflakeIdInfo(long timestamp, long datacenterId, long workerId, long sequence) {
            this.timestamp = timestamp;
            this.datacenterId = datacenterId;
            this.workerId = workerId;
            this.sequence = sequence;
        }

        public long getTimestamp() { return timestamp; }
        public long getDatacenterId() { return datacenterId; }
        public long getWorkerId() { return workerId; }
        public long getSequence() { return sequence; }

        @Override
        public String toString() {
            return String.format("SnowflakeIdInfo{timestamp=%d, datacenterId=%d, workerId=%d, sequence=%d}",
                timestamp, datacenterId, workerId, sequence);
        }
    }
}
