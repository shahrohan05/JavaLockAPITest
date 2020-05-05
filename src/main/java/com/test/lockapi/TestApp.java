package com.test.lockapi;

import java.util.concurrent.locks.ReentrantLock;

public class TestApp {

	int i = 1;

	ReentrantLock lock = new ReentrantLock(true);
	// [2] Fairness: Setting boolean fair parameter ensures, longest waiting thread
	// is given access first after the lock is released.

	public static void main(String[] args) {

		int i = testIncrementOperation();

		System.out.println("final int value - (should be 82) :" + i);

	}

	public static int testIncrementOperation() {
		TestApp testApp = new TestApp();
		TestAppIncrementOperation incOp = new TestAppIncrementOperation(testApp);

		Thread t1 = new Thread(incOp, "Thread 1");

		Thread t2 = new Thread(incOp, "Thread 2");

		Thread t3 = new Thread(() -> {
			testApp.doubleThat();
		}, "Thread 3");

		t1.start();
		t2.start();
		
		
		try {
			t1.join();
			t2.join();
			
			t3.start();
			t3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return testApp.i;
	}

	private void doubleThat() {
		try {
			lock.lockInterruptibly();
			// [4] Interruptability: If interrupted, the thread waiting to acquire lock here
			// would abort acquiring lock and executing following operation.
			i*=2;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}

	}

	/**
	 * Increments the member i once, waits for 200 ms and increments again from
	 * another method.
	 */
	private void incrementTwice() {
		i++;
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		andIncrementAgain();
	}

	private void andIncrementAgain() {
		i++;
		lock.unlock();
		// [1] Flexibility: Lock and unlock methods can span across methods for
		// synchronization purpose, this is similar to synchronizing both methods but
		// more readable.
	}

	public boolean tryIncrementTwiceOperation() {
		boolean canLock = lock.tryLock();
		// [3] Efficiency: Thread doesn't have to indefinitely go on hold for waiting to
		// acquire lock, it can try acquiring it and if not acquired, can do some other
		// work.
		if (canLock) {
			incrementTwice();
		}
		return canLock;
	}

}

class TestAppIncrementOperation implements Runnable {

	TestApp testApp;

	public TestAppIncrementOperation(TestApp testApp) {
		this.testApp = testApp;
	}

	@Override
	public void run() {
		for (int i = 0; i < 10; i++) {
			System.out.println("incrementing from " + Thread.currentThread().getName());
			while (!testApp.tryIncrementTwiceOperation()) {
				System.out.println("The operation is still not available, doining some other work, meanwhile");

				try {
					Thread.sleep(200); // some other work simulation
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
