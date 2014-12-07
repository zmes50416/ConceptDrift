package tw.edu.ncu.CJ102.CoreProcess;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

public class UserProfileTopicViewer {
	JFrame mFrame = new JFrame("UserProfile Graph View");

	public static void main(String[] args) {
		JFileChooser chooser = new JFileChooser(new File("."));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if(!(chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)){
			JOptionPane.showMessageDialog(null, "You need input file");
			return;
		}
		File file = chooser.getSelectedFile();

		ConceptDrift_Forecasting c = new ConceptDrift_Forecasting(file.getPath()+"/");
		try {
			c.readFromProject();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		c.forecastingByNGD();
		Layout<String, Double> layout = new CircleLayout<String, Double>(c.getTopicCooccurGrahp());
		BasicVisualizationServer<String,Double> graphPanel = new BasicVisualizationServer<String,Double>(layout) ;
		graphPanel.setPreferredSize(new Dimension(300,300));
		graphPanel.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<Double>());
		graphPanel.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
		JFrame mFrame =  new UserProfileTopicViewer().mFrame;
		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mFrame.getContentPane().add(graphPanel);
		mFrame.pack();
		mFrame.setVisible(true);
		

	}

}
