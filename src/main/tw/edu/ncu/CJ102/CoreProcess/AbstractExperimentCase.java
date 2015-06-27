package tw.edu.ncu.CJ102.CoreProcess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.edu.ncu.CJ102.SettingManager;
import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.im.Util.EmbeddedIndexSearcher;
import tw.edu.ncu.im.Util.HttpIndexSearcher;
import tw.edu.ncu.im.Util.IndexSearchable;

public abstract class AbstractExperimentCase implements Runnable{
	public String topicPath;
	public double parama;//different meaning in different experiment, the control variable
	protected Experiment experiment;
	protected AbstractUserProfile user;
	protected static IndexSearchable searcher;
	protected double topicSimliarityThreshold;
	protected int experimentDays;
	protected double removeRate;
	protected PerformanceMonitor totalMonitor = new PerformanceMonitor();
	protected Long sumTime = 0L;
	protected boolean debugMode;
	protected Path rootDir;
	File excelSummary;
	Logger logger = LoggerFactory.getLogger(this.getClass());
	protected int roundNumber;//For excel recording
	public AbstractExperimentCase(Path _projectDir) {
		this.rootDir = _projectDir;
		debugMode = true;
		excelSummary = this.rootDir.resolve(rootDir.getFileName()+"_summary.xls").toFile(); // using the recordthisround to add into summary
		try (HSSFWorkbook workbook = new HSSFWorkbook()) {
			HSSFSheet sheet = workbook.createSheet("Summary");
			HSSFRow titleRow = sheet.createRow(0);
			titleRow.createCell(0).setCellValue("Turn");
			int count = 1;
			for (Entry<PerformanceType,Double> type : totalMonitor.getResult().entrySet()) {
				HSSFCell cell = titleRow.createCell(count);
				cell.setCellValue(type.getKey().toString());
				count++;
			}
			titleRow.createCell(count++).setCellValue("Time(NanoSecond)");
			workbook.write(new FileOutputStream(excelSummary));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * record total number of experiment
	 */
	private void recordThisRound(int turn) {
		try (HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(excelSummary));) {
			HSSFSheet sheet = book.getSheetAt(0);
			HSSFRow turnRow = sheet.createRow(turn+1);
			turnRow.createCell(0).setCellValue(turn);
			int count = 1;
			for (Entry<PerformanceType, Double> performance : this.totalMonitor.getResult().entrySet()) {
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

	@Override
	public void run() {
		try(HSSFWorkbook workbook = new HSSFWorkbook()) {
			this.experiment.initialize();
			String projectName = this.experiment.getProjectPath().getFileName().toString();
			this.totalMonitor = new PerformanceMonitor();
			File excel = this.experiment.getProjectPath().resolve(projectName+"_data.xls").toFile();
			
	        HSSFSheet sheet = workbook.createSheet("Data");
	        int cellCount = 1;
			HSSFRow titlerow = sheet.createRow(0);
			titlerow.createCell(0).setCellValue("Day");
			for(Entry<PerformanceType, Double> entry:totalMonitor.getResult().entrySet()){
				 
			     HSSFCell cell = titlerow.createCell(cellCount);
			     cell.setCellValue(entry.getKey().toString());
				 cellCount++;
	        }
			titlerow.createCell(cellCount).setCellValue("Time(NanoSeconds)");
			titlerow.createCell(++cellCount).setCellValue("Size of Short Term Interest");
			titlerow.createCell(++cellCount).setCellValue("Size of Long Term Interest");
			titlerow.createCell(++cellCount).setCellValue("Drifted");
	        
	        sumTime = 0L;
			
			for (int dayN = 1; dayN <= this.experiment.experimentDays; dayN++) { //execute every day and record performance
				Long time = System.currentTimeMillis();
				this.experiment.run(dayN);
				totalMonitor.addUp(this.experiment.systemDailyPerformance);
				Long spendedTime = System.currentTimeMillis() - time;
				logger.info("Run a day {}, time: {}ms", dayN, spendedTime);
				sumTime += spendedTime;

				cellCount = 1;
				HSSFRow dailyRow = sheet.createRow(dayN);//Excel log
				dailyRow.createCell(0).setCellValue(dayN);
				for(Entry<PerformanceType, Double> entry:totalMonitor.getResult().entrySet()){
					HSSFCell secCell = dailyRow.createCell(cellCount);
					secCell.setCellValue(entry.getValue());
					cellCount++;
		        }
				dailyRow.createCell(cellCount++).setCellValue(spendedTime);
				dailyRow.createCell(cellCount++).setCellValue(user.getShortTermcount());
				dailyRow.createCell(cellCount++).setCellValue(user.getLongTermCount());
				dailyRow.createCell(cellCount++).setCellValue(this.experiment.numberOfDrifted);
				FileOutputStream out = new FileOutputStream(excel);
				workbook.write(out);
			}
			this.recordThisRound(roundNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	static AbstractExperimentCase newExpController(int type,Path path){
		if(type==0){
			return new RouterExperimentCase(path);
		}else{
			return new BBCExperimentsCase(path);
		}
	}
	
	public static void main(String[] args) {
		//Environment setup
		EmbeddedIndexSearcher.SolrHomePath = SettingManager.getSetting("SolrHomePath");
		EmbeddedIndexSearcher.solrCoreName = SettingManager.getSetting("SolrCoreName");
		HttpIndexSearcher.url = "http://localhost/searchweb/";
		ArrayList<AbstractExperimentCase> exps = new ArrayList<>();
		try(Scanner scanner = new Scanner(System.in)){
			System.out.println("使用HTTP(1)或是嵌入式SOLR(2)?");
			if(scanner.nextInt()==1){
				AbstractExperimentCase.searcher = new HttpIndexSearcher();
			}else{
				AbstractExperimentCase.searcher = new EmbeddedIndexSearcher();
			}
			do{
				System.out.println("BBC(1) or 路透社(0)?");
				int type = scanner.nextInt();
				Path path = Paths.get(SettingManager.chooseProject());
				System.out.println("You Dir is:" + path);
				System.out.println("Which Experiment you wanna run?");
				System.out.println("1.主題相關應得分數比例");
				System.out.println("2.興趣去除比例");
				System.out.println("3.效能實驗");
				System.out.println("4.核心數目實驗");
				System.out.println("5.概念飄移實驗");
				System.out.println("6.長期興趣門檻");
				String input = scanner.next();
				TopicTermGraph.MAXCORESIZE = 15;
				System.out.println("預設核心數目:"+TopicTermGraph.MAXCORESIZE);
				System.out.println("請填入遞迴回數:");
				int round = scanner.nextInt();
				if(input.equals("1")){
					System.out.println("請填入主題相關應得門檻起始值:");
					double initTopicThreshold = scanner.nextDouble();
					if(initTopicThreshold > 1){
						throw new RuntimeException("輸入門檻值不得大於1");
					}
					for(int turn = 0;turn<round;turn++){
						AbstractExperimentCase expController = newExpController(type,path);
						expController.parama = initTopicThreshold;
						expController.roundNumber = turn;
						expController.TopicRelatedScore(turn);
						exps.add(expController);
					}
					
				}else if(input.equals("2")){
					System.out.println("請填入移除門檻起始值:");
					double initRemoveThreshold = scanner.nextDouble();
					if(initRemoveThreshold > 1){
						throw new RuntimeException("輸入門檻值不得大於1");
					}
					for(int turn = 0;turn<round;turn++){
						AbstractExperimentCase expController = newExpController(type,path);
						expController.roundNumber = turn;
						expController.removeThresholdExperiment(turn);
						expController.parama = initRemoveThreshold;
						exps.add(expController);
					}
				}else if(input.equals("3")){
					for(int turn = 0;turn<round;turn++){
						AbstractExperimentCase expController = newExpController(type,path);
						expController.performanceExperiment(turn);
						expController.roundNumber = turn;
						exps.add(expController);
					}
				}else if(input.equals("4")){
					System.out.println("初始核心數目為5");
					for(int turn=0;turn<round;turn++){
						for(int j= 0;j<=2;j++){//examine 3 different method
							TopicTermGraph.METHODTYPE = j;
							AbstractExperimentCase expController = newExpController(type,path); 
							expController.roundNumber = turn*4+j;
							expController.coreExperiment(turn);
							exps.add(expController);
						}
						AbstractExperimentCase expController = newExpController(type,path);
						expController.roundNumber = turn*4+4;
						expController.corelessExperiment(turn);
						exps.add(expController);
					}
				}else if(input.equals("5")){
					System.out.println("請填入長期門檻值參數:");
					double parama = scanner.nextDouble();
					System.out.println("請填入隨機次數:");
					int times = scanner.nextInt();
					
					for(int turn =0;turn<round;turn++){
						for(int j = 1;j<=times;j++){
							AbstractExperimentCase expController = newExpController(type,path); 
							expController.parama = parama;
							expController.conceptDriftExperiment(turn,j);
							expController.roundNumber = turn*j+j;
							exps.add(expController);
						}
						

					}
				}else if(input.equals("6")){
					System.out.println("請填入長期門檻值參數:");
					double parama = scanner.nextDouble();
					System.out.println("請填入隨機次數:");
					int times = scanner.nextInt();
					
					for(int turn =0;turn<round;turn++){
						for(int j = 1;j<=times;j++){
							AbstractExperimentCase expController = newExpController(type,path); 
							expController.parama = parama;
							expController.oldConceptDriftExperiment(turn,j);
							expController.roundNumber = turn*j+j;
							exps.add(expController);
						}
						

					}
				}
				System.out.println("Do you continue add up next exp?(0==Exit)");
			}while(scanner.nextInt()!=0);
			ExecutorService tasker = Executors.newSingleThreadExecutor();
			for(AbstractExperimentCase exp:exps){
				tasker.submit(exp);
			}
			tasker.shutdown();
			tasker.awaitTermination(Integer.MAX_VALUE, TimeUnit.HOURS);
			System.out.println("All exp have been executed!");
		}catch(IOException | InterruptedException e){
			if(e.getClass()==InterruptedException.class){
				System.err.println("Interrupted thread pool. System stopped.");
			}else{
				System.err.println("IO Have been Interrupted. System stopped.");
			}
			e.printStackTrace();
		}
		
		System.exit(0);
		
	}
	abstract void removeThresholdExperiment(int turn);
	abstract void TopicRelatedScore(int turn) throws IOException;
	abstract void coreExperiment(int turn) throws IOException;
	abstract void corelessExperiment(int turn) throws IOException ;
	abstract void performanceExperiment(int turn) throws IOException;
	abstract void conceptDriftExperiment(int turn,int seed);
	abstract void oldConceptDriftExperiment(int turn,int seed);
}