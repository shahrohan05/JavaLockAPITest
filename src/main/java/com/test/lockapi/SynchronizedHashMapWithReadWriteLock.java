package com.test.lockapi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

/**
 * Limited thread safe hash map sample using ReadWriteLock, with only
 * get, put and keySet operations.
 */
public class SynchronizedHashMapWithReadWriteLock<K, V> {

	private Map<K, V> hashMap = new HashMap<K, V>();

	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private Lock writeLock = lock.writeLock();
	private Lock readLock = lock.readLock();

	public void put(K key, V value) {
		try {
			System.out.println("acquiring write lock to put [THREAD :" + Thread.currentThread().getName() + "]");
			writeLock.lock();
			try {
				Thread.sleep(100); // simulated delay in putting an element
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			hashMap.put(key, value);
		} finally {
			writeLock.unlock();
		}
	}

	public V get(K key) {
		try {

			System.out.println("acquiring read lock to get [THREAD :" + Thread.currentThread().getName() + "]");
			readLock.lock();
			return hashMap.get(key);
		} finally {
			readLock.unlock();
		}

	}

	public Set<K> keySet() {
		try {

			System.out.println(
					"acquiring read lock to get the list of keys [THREAD :" + Thread.currentThread().getName() + "]");
			readLock.lock();
			return new HashSet<K>(hashMap.keySet()); // sending a copy so that readers iterate over the current copy of
														// the keyset and do not throw ConcurrentException despite
														// concurrent modifications going on.
		} finally {
			readLock.unlock();
		}

	}

	public static void main(String[] args) {
		SynchronizedHashMapWithReadWriteLock<String, Integer> studentRanks = new SynchronizedHashMapWithReadWriteLock<String, Integer>();

		Thread producerThread1 = new Thread(
				() -> IntStream.iterate(1, i -> i++).limit(10)
						.forEach(i -> studentRanks.put("Cat 1 Student " + i, (new Random().nextInt(10) + 1))),
				"PRODUCER 1");

		Thread producerThread2 = new Thread(
				() -> IntStream.iterate(1, i -> ++i).limit(10)
						.forEach(i -> studentRanks.put("Cat 2 Student " + i, (new Random().nextInt(10) + 1))),
				"PRODUCER 2");

		Thread consumerThread1 = new Thread(
				() -> studentRanks.keySet().forEach(key -> System.out.println(key + " : " + studentRanks.get(key))),
				"CONSUMER 1");

		Thread consumerThread2 = new Thread(
				() -> studentRanks.keySet().forEach(key -> System.out.println(key + " : " + studentRanks.get(key))),
				"CONSUMER 2");

		producerThread1.start();
		producerThread2.start(); // both producers will wait for each other to finish putting an element, the
									// readers will also be waiting (for their read locks) while write lock is held
									// by either of these threads.

		try {
			Thread.sleep(500); // simulated delay, some head start for the producers
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		consumerThread1.start();
		consumerThread2.start(); // both readers can acquire the read lock simultaneously, provided there is no
									// write lock held at the moment.

	}

}
