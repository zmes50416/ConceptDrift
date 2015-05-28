package tw.edu.ncu.CJ102.CoreProcess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import tw.edu.ncu.CJ102.SettingManager;
import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.MemoryBasedUserProfile;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.CJ102.algorithm.impl.NgdReverseTfTopicSimilarity;
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;
import tw.edu.ncu.im.Util.HttpIndexSearcher;
import tw.edu.ncu.im.Util.IndexSearchable;

public class NewThresholdExperiment {
	Path rootDir;
	String topicPath = "Tom_reuters_0.4/single";
	Experiment exp ;
	AbstractUserProfile user;
	IndexSearchable searcher;
	private double topicSimliarityThreshold;
	private int experimentDays;
	private double removeRate;
	PerformanceMonitor totalMonitor;//record total performance
	File excelSummary;
	Logger logger = LoggerFactory.getLogger(this.getClass());
	Long sumTime = 0L;
	public int round;
	public double parama;
	private boolean debugMode;
	public static void main(String[] args) {
		//Environment setup
		EmbeddedIndexSearcher.SolrHomePath = SettingManager.getSetting("SolrHomePath");
		EmbeddedIndexSearcher.solrCoreName = SettingManager.getSetting("SolrCoreName");
		HttpIndexSearcher.url = "http://localhost/searchweb/";
		
		Path path = Paths.get(SettingManager.chooseProject());
		NewThresholdExperiment expController = new NewThresholdExperiment(path); 
		System.out.println("You Dir is:"+path);
		System.out.println("Which ThresholdExp you wanna run?");
		System.out.println("1.主題相關應得分數比例");
		System.out.println("2.興趣去除比例");
		System.out.println("3.效能實驗");
		System.out.println("4.核心數目實驗");
		System.out.println("5.概念飄移實驗");
		System.out.println("6.長期興趣門檻");
		String input;
		try{
			Scanner scanner = new Scanner(System.in);
				input = scanner.next();
				System.out.println("使用HTTP(1)或是嵌入式SOLR(2)?");
				if(scanner.nextInt()==1){
					expController.searcher = new HttpIndexSearcher();
				}else{
					expController.searcher = new EmbeddedIndexSearcher();
				}
				System.out.println("請填入核心數目");
				TopicTermGraph.MAXCORESIZE = scanner.nextInt();
				System.out.println("請填入遞迴回數:");
				expController.round = scanner.nextInt();
				
				if(input.equals("1")){
					System.out.println("請填入主題相關應得門檻起始值:");
					expController.parama = scanner.nextDouble();
					if(expController.parama > 1){
						throw new RuntimeException("輸入門檻值不得大於1");
					}
					for(int turn = 0;turn<expController.round;turn++){
						expController.TopicRelatedScore(turn);
						expController.recordThisRound(turn);
					}
					
				}else if(input.equals("2")){
					System.out.println("請填入移除門檻起始值:");
					expController.parama = scanner.nextDouble();
					if(expController.parama > 1){
						throw new RuntimeException("輸入門檻值不得大於1");
					}
					expController.removeThresholdExperiment();
				}else if(input.equals("3")){
					expController.performanceExperiment();
				}else if(input.equals("4")){
					for(int turn=0;turn<expController.round;turn++){
						expController.coreExperiment(turn);
						expController.corelessExperiment(turn);
						expController.recordThisRound(turn);
					}
				}else if(input.equals("5")){
					for(int turn =0;turn<expController.round;turn++){
						expController.conceptDriftExperiment(turn);

					}
				}
				System.out.println("Experimetn have been done!\n");
				System.exit(0);
		}catch(IOException e){
			System.err.println("IO Have been Interrupted. System stopped.");
			e.printStackTrace();
		}
		expController.searcher.shutdown();
		
	}
	
	public NewThresholdExperiment(Path _projectDir) {
		this.rootDir = _projectDir;
		debugMode = true;
		excelSummary = this.rootDir.resolve("totalData.xls").toFile();
		try (HSSFWorkbook workbook = new HSSFWorkbook()) {
			HSSFSheet sheet = workbook.createSheet("Summary");
			HSSFRow titleRow = sheet.createRow(0);
			titleRow.createCell(0).setCellValue("Turn");
			int count = 1;
			for (PerformanceType type : PerformanceType.values()) {
				HSSFCell cell = titleRow.createCell(count);
				cell.setCellValue(type.toString());
				count++;
			}
			titleRow.createCell(count).setCellValue("Time(NanoSecond)");
			workbook.write(new FileOutputStream(excelSummary));
		} catch (IOException e) {

		}
	}
	
	public void TopicRelatedScore(int turn) throws IOException{
		experimentDays = 10;
		removeRate = 0.1;
		topicSimliarityThreshold = parama + (turn/10.0);
		TopicMappingTool maper = new TopicMappingTool(
					new NgdReverseTfTopicSimilarity(),
					this.topicSimliarityThreshold);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(removeRate);
		Path experimentDir = this.rootDir.resolve("turn_"+turn);
		exp = new Experiment(experimentDir.toString(),maper,user);
		exp.debugMode = debugMode;
		this.exp.experimentDays = experimentDays;

		RouterNewsPopulator populater = new RouterNewsPopulator(experimentDir.toString(),topicPath){
				@Override
				public void setGenarationRule() {
					this.setTrainSize(5);
					this.setTestSize(5);	
					
				}
				
			};
		exp.newsPopulater = populater;
		for(String topic:RouterNewsPopulator.test){
			populater.addTestingTopics(topic);
		}
		populater.addTrainingTopics("acq");
		execute();
		
	}

	public void removeThresholdExperiment() throws IOException{
		topicSimliarityThreshold = 0.7;
		experimentDays = 14;
		for (int i = 0; i < round; i++) {
			Path tempDir = this.rootDir.resolve("turn_" + i);
			TopicMappingTool maper = new TopicMappingTool(
					new NgdReverseTfTopicSimilarity(),
					this.topicSimliarityThreshold);
			user = new MemoryBasedUserProfile();
			removeRate = parama + (i / 10.0);
			user.setRemoveRate(removeRate);

			this.exp = new Experiment(tempDir.toString(), maper, user);
			this.exp.setExperimentDays(experimentDays);
			exp.debugMode = debugMode;


			RouterNewsPopulator populater = new RouterNewsPopulator(
					tempDir.toString(), topicPath) {
				@Override
				public void setGenarationRule() {
					this.setTrainSize(5);
					this.setTestSize(5);

				}

			};
			exp.newsPopulater = populater;
			populater.addTrainingTopics("acq");
			for (String topic : RouterNewsPopulator.test) {
				populater.addTestingTopics(topic);
			}
			execute();
			this.recordThisRound(i);
		}
	}
	
	public void timeExperiment() throws IOException{
		//TODO unfinished Experiment
		topicSimliarityThreshold = 0.4;
		removeRate = 0.1;
		user.setRemoveRate(removeRate);
		exp.setUser(user);

		this.exp.experimentDays = 10;

		RouterNewsPopulator populater = new RouterNewsPopulator(this.rootDir.toString(),topicPath){
			@Override
			public void setGenarationRule() {
				this.setTrainSize(4);
				this.setTestSize(4);	
				
			}
			
		};

		File copyData = new File("DEMODATA");
		populater = new ManualRouterNewsPopulater(this.rootDir.toString(), copyData.toPath());
		exp.newsPopulater = populater;
		populater.addTrainingTopics("acq");
		populater.addTestingTopics("earn");
		execute();
	}
	
	public void coreExperiment(int turn){
		this.topicSimliarityThreshold = 0.6;
		this.removeRate = 0.5;
		for(int j= 0;j<=2;j++){//examine 3 different method
				TopicTermGraph.METHODTYPE = j;
				String methodName = null;
				if(j==0){
					methodName = "Degree";
				}else if(j==1){
					methodName = "LP";
				}else if(j==2){
					methodName = "Betweenness";
				}
				Path tempProject = this.rootDir.resolve("Methode_"+methodName).resolve("round_"+turn);
				TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),this.topicSimliarityThreshold);
				user = new MemoryBasedUserProfile();
				user.setRemoveRate(this.removeRate);
				TopicTermGraph.MAXCORESIZE = 5 + turn*5;
				
				exp = new Experiment(tempProject.toString(),maper,user);
				exp.experimentDays = 14;
				exp.debugMode = this.debugMode;
				
				RouterNewsPopulator populater = new RouterNewsPopulator(tempProject.toString(),topicPath){
					@Override
					public void setGenarationRule() {
						this.setTrainSize(10);
						this.setTestSize(5);	
						
					}
					
				};
				populater.addTrainingTopics("acq");
				populater.addTestingTopics("acq");
				ArrayList<String> topics = Lists.newArrayList(RouterNewsPopulator.test);
				String randomTopic = topics.get(new Random(turn).nextInt(topics.size()));
				populater.addTestingTopics(randomTopic);
				exp.newsPopulater = populater;
				execute();
				
				this.recordThisRound(turn*4+j);
		}
	}
	
	public void  corelessExperiment(int turn){
		Path tempProject = this.rootDir.resolve("Method_coreless").resolve("round_"+turn);
		TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),this.topicSimliarityThreshold);//Will be the same of Core exp
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(this.removeRate);
		TopicTermGraph.MAXCORESIZE = 1000;
		
		exp = new Experiment(tempProject.toString(),maper,user);
		exp.experimentDays = 14;
		exp.debugMode = this.debugMode;
		
		RouterNewsPopulator populater = new RouterNewsPopulator(tempProject.toString(),topicPath){
			@Override
			public void setGenarationRule() {
				this.setTrainSize(10);
				this.setTestSize(5);	
				
			}
			
		};
		populater.addTrainingTopics("acq");
		populater.addTestingTopics("acq");
		ArrayList<String> topics = Lists.newArrayList(RouterNewsPopulator.test);
		topics.remove("acq");
		String randomTopic = topics.get(new Random(turn).nextInt(topics.size()));
		populater.addTestingTopics(randomTopic);
		exp.newsPopulater = populater;
		execute();
		this.recordThisRound(turn*4+4);
	}
	
	public void conceptDriftExperiment(int turn){
		Path project = this.rootDir.resolve("turn_"+turn);
		TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),0.5);
		user = new MemoryBasedUserProfile();
		user.setRemoveRate(1);
				
		exp = new Experiment(project.toString(),maper,user);
		exp.debugMode = debugMode;
		exp.experimentDays = 14;
		
		RouterNewsPopulator populater = new RouterNewsPopulator(project.toString()){

			@Override
			public void setGenarationRule() {
				this.setTrainSize(10);
				this.setTestSize(5);
				this.trainTopics.clear();
				if(this.theDay<=7){
					this.addTrainingTopics("acq");
				}else{
					this.addTrainingTopics("earn");
				}
				
			}
			
		};
		populater.addTrainingTopics("acq");//only to avoid warning
		populater.addTestingTopics("acq");
		populater.addTestingTopics("trade");
		ArrayList<String> topics = Lists.newArrayList(RouterNewsPopulator.test);
		topics.remove("acq");
		topics.remove("trade");
		String randomTopic = topics.get(new Random(turn).nextInt(topics.size()));
		populater.addTestingTopics(randomTopic);
		exp.newsPopulater = populater;
		execute();
		this.recordThisRound(turn);
	}
	
	public void performanceExperiment(){
		String[][] trainTopic={{"acq","earn"},{"crude","coffee"},{"sugar","trade"},{"acq","cocoa"},{"crude","trade"}};
		
		for(int i = 0;i<round;i++){
			Path tempProject = this.rootDir.resolve("round_"+i);
			TopicMappingTool maper = new TopicMappingTool(new NgdReverseTfTopicSimilarity(),0.8);
			user = new MemoryBasedUserProfile();
			user.setRemoveRate(0.5);
			
			exp = new Experiment(tempProject.toString(),maper,user);
			exp.debugMode = debugMode;
			exp.experimentDays = 10;
			
			RouterNewsPopulator populater = new RouterNewsPopulator(tempProject.toString(),topicPath){
				@Override
				public void setGenarationRule() {
					this.setTrainSize(5);
					this.setTestSize(5);	
					
				}
				
			};
			populater.addTrainingTopics(trainTopic[i][0]);
			populater.addTrainingTopics(trainTopic[i][1]);
			for(String topic:RouterNewsPopulator.test){
				populater.addTestingTopics(topic);
			}
			exp.newsPopulater = populater;
			execute();
		}
	}

	private void execute(){
		try {
			this.exp.initialize();
			String projectName = this.exp.getProjectPath().getFileName().toString();
			this.totalMonitor = new PerformanceMonitor();
			File excel = this.exp.getProjectPath().resolve(projectName+"_data.xls").toFile();
			HSSFWorkbook workbook = new HSSFWorkbook();
	        HSSFSheet sheet = workbook.createSheet("Data");
			sumTime = 0L;
			
			for (int dayN = 1; dayN <= this.exp.experimentDays; dayN++) {
				Long time = System.currentTimeMillis();
				File userLog =exp.getUserProfilePath().resolve("userLog.txt").toFile();
				this.exp.run(dayN);
				totalMonitor.addUp(this.exp.systemDailyPerformance);
				Long spendedTime = System.currentTimeMillis() - time;
				logger.info("Run a day {}, time: {}ms", dayN, spendedTime);
				sumTime += spendedTime;
				BufferedWriter writer = new BufferedWriter(new FileWriter(userLog, true)); 
				writer.append("Time spend:" + spendedTime);//Simple UserLog
				writer.newLine();
				writer.append("Performance to date:F-measure"
							+ totalMonitor.computeFmeasure() + "Recall:"
							+ totalMonitor.computeRecall() + ",Percision:"
							+ totalMonitor.computePrecision() + ",Accuracy:"
							+ totalMonitor.computeAccuracy());
				writer.newLine();
				writer.close();
				int count = 1;
				HSSFRow dailyRow = sheet.createRow(dayN);//Excel log
				dailyRow.createCell(0).setCellValue(dayN);
				for(Entry<PerformanceType, Double> entry:totalMonitor.getResult().entrySet()){
					HSSFCell secCell = dailyRow.createCell(count);
					secCell.setCellValue(entry.getValue());
					count++;
		        }
				dailyRow.createCell(count).setCellValue(spendedTime);
			}
			int count = 1;
			HSSFRow titlerow = sheet.createRow(0);
			titlerow.createCell(0).setCellValue("Day");
			HSSFRow finalRow = sheet.createRow(this.exp.experimentDays+1);
			for(Entry<PerformanceType, Double> entry:totalMonitor.getResult().entrySet()){
				 
			     HSSFCell cell = titlerow.createCell(count);
			     cell.setCellValue(entry.getKey().toString());
				 HSSFCell secCell = finalRow.createCell(count);
				 secCell.setCellValue(entry.getValue());
				 count++;
	        }
			titlerow.createCell(count).setCellValue("Time");
			finalRow.createCell(count).setCellValue(sumTime);
            FileOutputStream out = new FileOutputStream(excel);
            workbook.write(out);
            out.close();
            workbook.close();
			BufferedWriter writer = new BufferedWriter(new FileWriter(exp
					.getProjectPath().resolve("setting.txt").toFile(), true));
			writer.append("Core Size:"+TopicTermGraph.MAXCORESIZE);
			writer.append("Total time: " + sumTime +" millsecond");
			writer.newLine();
			writer.append("Performance:" + totalMonitor.getResult());
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//this.exp.simplelog(experimentDays);

	}
	/**
	 * record total number of experiment
	 */
	public void recordThisRound(int turn){
		try (HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(excelSummary));) {
			HSSFSheet sheet = book.getSheetAt(0);
			HSSFRow turnRow = sheet.createRow(turn+1);
			turnRow.createCell(0).setCellValue(turn);
			int count = 1;
			Set<Entry<PerformanceType, Double>> endResults = this.totalMonitor.getResult().entrySet();
			for (Entry<PerformanceType, Double> performance : endResults) {
				HSSFCell cell = turnRow.createCell(count++);
				cell.setCellValue(performance.getValue());
			}
			turnRow.createCell(count).setCellValue(this.sumTime);
			FileOutputStream fileOut = new FileOutputStream(excelSummary);
			book.write(fileOut);
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
