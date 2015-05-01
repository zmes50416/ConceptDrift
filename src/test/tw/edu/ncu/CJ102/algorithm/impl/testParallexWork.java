package tw.edu.ncu.CJ102.algorithm.impl;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import tw.edu.ncu.CJ102.NGD_calculate;
import tw.edu.ncu.CJ102.SolrSearcher;
import tw.edu.ncu.CJ102.Data.TermNode;
@RunWith(Parameterized.class)
public class testParallexWork {
	@Parameters
	public static Collection<Object[]> prepareData(){
		return Arrays.asList(new Object[][]{
				{10,1000},{10,1000},{50,1000},{50,1000},{100,1000},{100,10000},{300,10000}
		});
	}
	@Parameter
	public int threadNumber;
	
	@Parameter(value =1)
	public int round;
	
	public TermNode termA, termB;
	
	@Before
	public void setup() throws Exception{
		termA = new TermNode("Google");
		termB = new TermNode("Yahoo");
	}
	@Test
	public void testMultiThread(){
		ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
		CompletionService<Double> cs = new ExecutorCompletionService<Double>(executor);
		List<Future<Double>> list = new ArrayList<>();
		for(int i=0;i<=round;i++){
				termA.termFreq = 1;
				termB.termFreq = 1;
				Future<Double> f = cs.submit(new NgdReverseTfComputingTask(termA,termB));
				list.add(f);
		}
		int errorCount = 0;
		double sum = 0;
		int count = 0;
		while (count < list.size()) {
			try {
				Future<Double> task = cs.take();
				if(!task.isDone()){
					errorCount++;
				}
				sum += task.get();
				count++;
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				fail("System are not up, please check!");
				break;
			}
		}
//		for(Future<Double> task:list){
//			if(!task.isDone()){
//				errorCount++;
//			}
//			try {
//				task.get();
//			} catch (InterruptedException | ExecutionException e) {
//				e.printStackTrace();
//			}
//		}
		System.out.println(sum);
		System.out.println("ErrorCount:"+errorCount);
	}
	
	@Test
	public void testUnstableMultiThread(){
		ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
		List<Future<Double>> list = new ArrayList<>();
		for(int i=0;i<=round;i++){
				termA.termFreq = 1;
				termB.termFreq = 1;
				Future<Double> f = executor.submit(new NgdReverseTfComputingTask(termA,termB));
				list.add(f);
		}
		double sum = 0;
		for(Future<Double> task:list){
			try {
				sum += task.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		System.out.println(sum);
	}
	@Test
	public void testMultiThreadWithCachedPool(){
		ExecutorService executor = Executors.newCachedThreadPool();
		CompletionService<Double> cs = new ExecutorCompletionService<Double>(executor);
		List<Future<Double>> list = new ArrayList<>();
		for(int i=0;i<=round;i++){
				termA.termFreq = 1;
				termB.termFreq = 1;
				Future<Double> f = cs.submit(new NgdReverseTfComputingTask(termA,termB));
				list.add(f);
		}
		int errorCount = 0;
//		int count = 0;
//		while (count < list.size()) {
//			try {
//				Future<Double> task = cs.take();
//				if(!task.isDone()){
//					errorCount++;
//				}
//				task.get();
//				count++;
//			} catch (InterruptedException | ExecutionException e) {
//				e.printStackTrace();
//				fail("System are not up, please check!");
//				break;
//			}
//		}
		double sum = 0;
		for(Future<Double> task:list){
			try {
				sum += task.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		System.out.println(sum);
	}
	
	@Test
	public void testOneThread(){
		for(int i=0;i<=round;i++){
			double a = SolrSearcher.getHits("\"Google\"");
			double b = SolrSearcher.getHits("\"Yahoo\"");
			double mValue = SolrSearcher.getHits("+\"google\" +\"yahoo\"");
			double ngdDistance = NGD_calculate.NGD_cal(a, b, mValue);
			double termScore = (1 - ngdDistance);
//			assertTrue(termScore)
//			System.out.println(termScore);
		}
	}

}
