package tw.edu.ncu.CJ102.CoreProcess;

import java.io.File;

public class CiteUlikePopulater implements ExperimentFilePopulater {
	private String projectDir;
	private String realPeople;// 選擇citeulike資料流 讀者的成員編號 ex."626838af45efa5ca465683ab3b3f303e"
	private TrainingTools trainerTom = new TrainingTools();

	public CiteUlikePopulater(String dir) {
		this.projectDir = dir;
	}
	
	public void setPeople(String person){
		this.realPeople = person;
	}

	@Override
	public boolean populateExperiment(int days) {

		int train_days = 0, test_days = -1; // citeulike-實驗天數，0為全部，-1為不使用

		System.out.println("Real Word資料流為: " + realPeople);
		trainerTom.real_word_generateSet(
				"citeulike/citeulike_Tom_citeulike_0.4/", projectDir,
				realPeople, train_days, test_days); 
		realPeople = trainerTom.get_real_people();
		
		return true;
	}

	@Override
	public String getTopics(File document) {
		// TODO Auto-generated method stub
		return null;
	}

}
