package tw.edu.ncu.CJ102.CoreProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * 將指定資料夾的內容直接當作實驗的資料集，來重現以前的實驗
 * @author TingWen
 *
 */
public class ManualRouterNewsPopulater extends RouterNewsPopulator {

	Path listFile;
	LinkedHashMap<Integer,Set<File>> manulData;

	public ManualRouterNewsPopulater(String dir,Path copyFilePath) {
		super(dir);
		this.listFile = copyFilePath;
	}
	
	@Override
	public void setGenarationRule() {
		this.setTestSize(0);//let the router it self not produce any random doc
		this.setTrainSize(0);
		Path manualTrainData = listFile.resolve(Paths.get(
				RouterNewsPopulator.TRAININGPATH, "day_" + theDay));
		Path manualTestData = listFile.resolve(Paths.get(
				RouterNewsPopulator.TESTINGPATH, "day_" + theDay));
		try {
			for(File data:manualTrainData.toFile().listFiles()){
				Files.copy(
						data.toPath(),
						this.projectDir.resolve(
								Paths.get(RouterNewsPopulator.TRAININGPATH, "day_"
										+ theDay)).resolve(data.getName()));
			}
			for(File testData:manualTestData.toFile().listFiles()){

				Files.copy(testData.toPath(), this.projectDir.resolve(
						Paths.get(RouterNewsPopulator.TESTINGPATH, "day_"
								+ theDay)).resolve(testData.getName()));
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
