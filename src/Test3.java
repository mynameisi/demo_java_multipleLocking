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
 * Test3 考虑到task1只处理list1, task2只处理list2
 * 所以所有的task1同步到一个lock：lock1上
 * 所有的task2同步到一个lock: lokc2上
 * 这样不会出现线程1在做task1的时候，线程2不能做task2的情况了
 * 所以能够完成任务
 *
 */
public class Test3 {
	private final static Logger logger = LoggerFactory.getLogger(Test2.class);
	private Random random = new Random();

	private Object lock1 = new Object();
	private Object lock2 = new Object();

	//Lists.newArrayList()来自于Google Guava类库
	private List<Integer> list1 = Lists.newArrayList();
	private List<Integer> list2 = Lists.newArrayList();

	public void task1() {
		synchronized (lock1) {
			// 1. 睡1毫秒
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 2. 在list1中加入一个在[0,100)范围内的随机数
			list1.add(random.nextInt(100));
		}

	}

	public void task2() {
		synchronized (lock2) {
			// 1. 睡1毫秒
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 2. 在list2中加入一个在[0,100)范围内的随机数
			list2.add(random.nextInt(100));
		}

	}

	public void process() {
		// 整体会消耗一秒
		for (int i = 0; i < 1000; i++) {
			task1();// 每一个过程消耗1毫秒
			task2();// 每一个过程消耗1毫秒
		}
	}

	public static void main(String[] args) {

		final Test3 w = new Test3();
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
		long end = System.currentTimeMillis();
		logger.debug("结束计时");
		logger.debug("消耗时间: " + (end - start) + " 毫秒");
		logger.debug("完成任务: " + "List1: " + w.list1.size() + "; List2: " + w.list2.size());
	}

}