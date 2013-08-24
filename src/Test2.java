import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * 有2个list：list1 和 list2
 * 有2个任务： task1 和 task2
 * task1运行后先睡一秒，然后在list1中加入一个在[0,100)范围内的随机数
 * ===================
 * 有一个总任务: process
 * process循环1000次，每一次先运行 task1 再运行 task2
 * 因为task1要消耗≈1毫秒， task2要消耗≈1毫秒
 * 所以process总消耗：1000*(1+1)≈2000毫秒
 * ===================
 * 现在要在main中开两个线程
 * 每一个线程中运行一次: process
 * 要求：
 * 1. 运行结束后，list1 和 list2中各有2000个随机数
 * 2. 消耗的总时间≈2000毫秒【这里放宽一点要求：<3000 毫秒】
 * ===================
 * Test2 虽然使用了 synchronized 关键字
 * 但仍然无法在3秒之内完成任务
 * 因为：如果直接在方法体上用Synchronized关键字的话
 * 就相当于 synchronized(this){...}
 * 因为所有的操作都synchronized到了一个lock: this 上
 * 那么只要有一个操作在对list1, 或者list2 添加内容
 * 其他线程不管相对list1, 还是list2再操作，都需要等待
 * 这样2个process, 每个process2000个操作，消耗的时间就≈需要4000毫秒了
 * 显然在3000毫秒之内无法完成任务
 *
 */
public class Test2 {
	private final static Logger logger = LoggerFactory.getLogger(Test2.class);

	private Random random = new Random();

	private List<Integer> list1 = Lists.newArrayList();
	private List<Integer> list2 = Lists.newArrayList();

	public synchronized void task1() {
		// 1. 睡1毫秒
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 2. 在list1中加入一个在[0,100)范围内的随机数
		list1.add(random.nextInt(100));

	}

	public synchronized void task2() {
		// 1. 睡1毫秒
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 2. 在list2中加入一个在[0,100)范围内的随机数
		list2.add(random.nextInt(100));

	}

	public void process() {
		// 整体会消耗一秒
		for (int i = 0; i < 1000; i++) {
			task1();// 每一个过程消耗1毫秒
			task2();// 每一个过程消耗1毫秒
		}
	}

	public static void main(String[] args) {

		final Test2 w = new Test2();
		Runnable r1 = new Runnable() {
			public void run() {
				w.process();
			}
		};

		Runnable r2 = new Runnable() {
			public void run() {
				w.process();
			}
		};

		ExecutorService tPool = Executors.newFixedThreadPool(2);
		logger.debug("开始计时");

		long start = System.currentTimeMillis();
		tPool.submit(r1);
		tPool.submit(r2);

		tPool.shutdown();
		try {
			tPool.awaitTermination(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis(); // 结束计时

		logger.debug("结束计时");
		logger.debug("消耗时间: " + (end - start) + " 毫秒");
		logger.debug("完成任务: " + "List1: " + w.list1.size() + "; List2: " + w.list2.size());
	}

}