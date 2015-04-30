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
	
	@Test
	public void testMultiThread(){
		ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
		CompletionService<Double> cs = new ExecutorCompletionService<Double>(executor);
		List<Future<Double>> list = new ArrayList<>();
		for(int i=0;i<=round;i++){
				Future<Double> f = executor.submit(new NgdReverseTfComputingTask("Google", "Yahoo", i, i));
				list.add(f);
		}
		for(Future<Double> f:list){
			
			try {
				System.out.println(f.isCancelled()+":"+f.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	@Test
	public void testOneThread(){
		for(int i=0;i<=round;i++){
			double a = SolrSearcher.getHits("\"Google\"");
			double b = SolrSearcher.getHits("\"Yahoo\"");
			double mValue = SolrSearcher.getHits("+\"google\" +\"yahoo\"");
			double ngdDistance = NGD_calculate.NGD_cal(a, b, mValue);
			double termScore = (1 - ngdDistance)
					* ((i + i) / 2);
			System.out.println(termScore);
		}
	}

}
