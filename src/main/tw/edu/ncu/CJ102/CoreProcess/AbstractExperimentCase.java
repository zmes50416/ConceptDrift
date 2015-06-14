package tw.edu.ncu.CJ102.CoreProcess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.edu.ncu.CJ102.Data.AbstractUserProfile;
import tw.edu.ncu.CJ102.Data.TopicTermGraph;
import tw.edu.ncu.im.Util.IndexSearchable;

public class AbstractExperimentCase {

	protected Experiment experiment;
	protected AbstractUserProfile user;
	protected IndexSearchable searcher;
	protected double topicSimliarityThreshold;
	protected int experimentDays;
	protected double removeRate;
	protected PerformanceMonitor totalMonitor;
	protected Long sumTime = 0L;
	protected boolean debugMode;
	protected Path rootDir;
	File excelSummary;
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public AbstractExperimentCase(Path _projectDir) {
		this.rootDir = _projectDir;
		debugMode = true;
		excelSummary = this.rootDir.resolve(rootDir.getFileName()+"_summary.xls").toFile(); // using the recordthisround to add into summary
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
			titleRow.createCell(count++).setCellValue("Time(NanoSecond)");
			workbook.write(new FileOutputStream(excelSummary));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void execute() {
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

			}
			
	        FileOutputStream out = new FileOutputStream(excel);
	        workbook.write(out);
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

	/**
	 * record total number of experiment
	 */
	public void recordThisRound(int turn) {
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

}