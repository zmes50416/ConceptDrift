package tw.edu.ncu.sia.action;

import tw.edu.ncu.sia.gui.MainGuiPanel;

public class Action {
	public static void main(String[] args) {
		MainGuiPanel gui = new MainGuiPanel();
		gui.initGui();
		gui.setBounds(100, 100, 600, 800);
		gui.setVisible(true);
	}

}
