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
			titleRow.createCell(count++).setCellValue("Time(NanoSecond)");
			titleRow.createCell(count++).setCellValue("Size of Short Term Interest");
			titleRow.createCell(count++).setCellValue("Size of Long Term Interest");
			workbook.write(new FileOutputStream(excelSummary));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void execute() {
		try {
			this.experiment.initialize();
			String projectName = this.experiment.getProjectPath().getFileName().toString();
			this.totalMonitor = new PerformanceMonitor();
			File excel = this.experiment.getProjectPath().resolve(projectName+"_data.xls").toFile();
			HSSFWorkbook workbook = new HSSFWorkbook();
	        HSSFSheet sheet = workbook.createSheet("Data");
			sumTime = 0L;
			
			for (int dayN = 1; dayN <= this.experiment.experimentDays; dayN++) {
				Long time = System.currentTimeMillis();
				File userLog =experiment.getUserProfilePath().resolve("userLog.txt").toFile();
				this.experiment.run(dayN);
				totalMonitor.addUp(this.experiment.systemDailyPerformance);
				Long spendedTime = System.currentTimeMillis() - time;
				logger.info("Run a day {}, time: {}ms", dayN, spendedTime);
				sumTime += spendedTime;
				BufferedWriter writer = new BufferedWriter(new FileWriter(userLog, true)); 
				writer.append("Time spend:" + spendedTime);//Simple UserLog
				writer.close();
				int count = 1;
				HSSFRow dailyRow = sheet.createRow(dayN);//Excel log
				dailyRow.createCell(0).setCellValue(dayN);
				for(Entry<PerformanceType, Double> entry:totalMonitor.getResult().entrySet()){
					HSSFCell secCell = dailyRow.createCell(count);
					secCell.setCellValue(entry.getValue());
					count++;
		        }
				dailyRow.createCell(count++).setCellValue(spendedTime);
				dailyRow.createCell(count++).setCellValue(user.getShortTermcount());
				dailyRow.createCell(count++).setCellValue(user.getLongTermCount());

			}
			int count = 1;
			HSSFRow titlerow = sheet.createRow(0);
			titlerow.createCell(0).setCellValue("Day");
			HSSFRow finalRow = sheet.createRow(this.experiment.experimentDays+1);
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
			BufferedWriter writer = new BufferedWriter(new FileWriter(experiment
					.getProjectPath().resolve("setting.txt").toFile(), true));
			writer.append("Core Size:"+TopicTermGraph.MAXCORESIZE);
			writer.append("Total time: " + sumTime +" millsecond");
			writer.newLine();
			writer.append("Performance:" + totalMonitor.getResult());
			writer.close();
	
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