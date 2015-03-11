import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class distanceGUI extends JFrame {
	DrawPanel dpanel = new DrawPanel();
	HashMap<String, Point> nodes = new HashMap<String, Point>();
	HashMap<String, Integer> core = new HashMap<String, Integer>();
	HashMap<String, Double> core2 = new HashMap<String, Double>();
	ArrayList<String> links = new ArrayList<String>();
	ArrayList<String> nodelist = new ArrayList<String>();
	String[] k=new String[100];
	JPanel control = new JPanel();
	JButton renew = new JButton("Renew");
	JButton reclc = new JButton("Reclc");
	JLabel label1 = new JLabel("檔案編號");
	JLabel label2 = new JLabel("門檻值");
	JTextField index = new JTextField("0");
	JTextField threshold = new JTextField("0.5");
	JCheckBox filter = new JCheckBox("過濾");
	JCheckBox cut = new JCheckBox("區隔");
	JCheckBox arrange = new JCheckBox("整理");
	JComboBox k_value;
	Random rand = new Random();
	int no = 0;
	double max = 0.5;

	distanceGUI() throws IOException {
		for(int i=0;i<50;i++)
		{
			k[i]=String.valueOf(i+1);
		}
		k_value=new JComboBox(k); 
		control.setLayout(new GridLayout(1, 9));
		dpanel.setBackground(Color.WHITE);
		add(dpanel, BorderLayout.CENTER);
		control.add(renew);
		control.add(reclc);
		control.add(label1);
		control.add(index);
		control.add(label2);
		control.add(threshold);
		control.add(arrange);
		control.add(filter);
		control.add(k_value);
		control.add(cut);
		add(control, BorderLayout.SOUTH);
		renew.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub

				try {
					no = Integer.parseInt(index.getText());
					max = Double.parseDouble(threshold.getText());
					getData(no, max);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dpanel.repaint();
			}
		});
		reclc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				K_core kcore = new K_core();
				no = Integer.parseInt(index.getText());
				max = Double.parseDouble(threshold.getText());
				kcore.simMin = max;
				try {
					
					kcore.K_core_cal(no);
					
					getData(no, max);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				dpanel.repaint();
			}
		});

	}

	public void getData(int no, double max) throws IOException {
		this.no = no;
		this.max = max;
		nodes = new HashMap<String, Point>();
		links = new ArrayList<String>();
		nodelist = new ArrayList<String>();
		// FileReader fileStream = new FileReader("K_core/"+no + "_" + "k_core.txt");
		 FileReader fileStream = new FileReader("CC/"+no + "_" + "cc.txt");
		BufferedReader buffer = new BufferedReader(fileStream);

		String e = "";
		while ((e = buffer.readLine()) != null) {
			nodelist.add(e);
		}
		fileStream = new FileReader("Rank/"+no + "_" + "Rank.txt");
		buffer = new BufferedReader(fileStream);

		e = "";
		while ((e = buffer.readLine()) != null) {
			if (Double.parseDouble(e.split(",")[2]) <= this.max)
				links.add(e);
		}
	}

	public void updateGUI() {
		index.setText(String.valueOf(no));
		threshold.setText(String.valueOf(max));
		dpanel.repaint();
	}

	class DrawPanel extends JPanel {
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			//Object[] keys= nodes.keySet().toArray();
			int corehold=Integer.parseInt((String)k_value.getSelectedItem());
			for (String k : nodelist) {
				// int k_core = Integer.parseInt(k.split("=")[1]);
				//System.out.println(k);
				 double k_core = Double.parseDouble(k.split(",")[1]);
				int x;
				int y;
				if(arrange.isSelected())
				{
					// if(k_core < 2)
					 if(k_core<10)
					{
						 x = rand.nextInt(getWidth()/2);
						 y = rand.nextInt(getHeight()/2);
					}
					//else if(k_core == 2)
					 else if(k_core >= 10 && k_core < 40)
					{
						 x = getWidth()/2+rand.nextInt(getWidth()/2);
						 y = rand.nextInt(getHeight()/2);
					}
					//else if(k_core == 3) 
					else if(k_core >= 40 && k_core < 80)
					{
						 x = rand.nextInt(getWidth()/2);
						 y = getHeight()/2+rand.nextInt(getHeight()/2);
					}
					else
					{
						 x = getWidth()/2+rand.nextInt(getWidth()/2);
						 y = getHeight()/2+rand.nextInt(getHeight()/2);
					}
				}else
				{
					 x = rand.nextInt(getWidth());
					 y = rand.nextInt(getHeight());
				}
				
				
				//nodes.put(k.split("=")[0], new Point(x, y));
				 nodes.put(k.split(",")[0], new Point(x, y));
			}
			for (String k : nodelist) {
				Point p = nodes.get(k.split(",")[0]);
				// int k_core = Integer.parseInt(k.split("=")[1]);
				 double k_core = Double.parseDouble(k.split(",")[1]);
				// core.put(k.split("=")[0], k_core);
				 core2.put(k.split(",")[0], k_core);
				// if (k_core == 2)
				 if(k_core<10)
					g.setColor(Color.GREEN);
				else if(k_core >= 10 && k_core < 40)
					g.setColor(Color.BLUE);
				else if(k_core >= 40 && k_core < 80)
					g.setColor(Color.RED);
				else
					g.setColor(Color.DARK_GRAY);
				if(filter.isSelected())
				{
					if (k_core >= corehold) {
						g.fillOval(p.x, p.y, 10, 10);
						g.drawString(k, p.x - 5, p.y - 5);
					}
				}else{
					g.fillOval(p.x, p.y, 10, 10);
					g.drawString(k, p.x - 5, p.y - 5);
				}
				
			}
			for (String l : links) 
			{
				Point p = nodes.get(l.split(",")[0]);
				Point p2 = nodes.get(l.split(",")[1]);
				if(Double.valueOf(l.split(",")[2].substring(0,3)) < 0.5)
				{
					//如果大於門檻植就不畫線了
					if (filter.isSelected())
					{
						if (core2.get(l.split(",")[0]) >= corehold
								&& core2.get(l.split(",")[1]) >= corehold)
						{
							g.setColor(Color.DARK_GRAY);	
							if(cut.isSelected())
							{
								if (core2.get(l.split(",")[0])!=core2.get(l.split(",")[1]))
								{}
								else
								{
									g.drawLine(p.x + 5, p.y + 5, p2.x + 5, p2.y + 5);
									g.drawString(l.split(",")[2].substring(0, 3), (p.x
											+ p2.x + 10) / 2, (p.y + p2.y + 10) / 2);
								}
							}
							else
							{
								g.drawLine(p.x + 5, p.y + 5, p2.x + 5, p2.y + 5);
								g.drawString(l.split(",")[2].substring(0, 3), (p.x
										+ p2.x + 10) / 2, (p.y + p2.y + 10) / 2);
							}
						}
					}
					else
					{
						g.setColor(Color.DARK_GRAY);
						if(cut.isSelected())
						{
							if (core2.get(l.split(",")[0])!=core2.get(l.split(",")[1]))
							{}
							else
							{
								g.drawLine(p.x + 5, p.y + 5, p2.x + 5, p2.y + 5);
								g.drawString(l.split(",")[2].substring(0, 3), (p.x
										+ p2.x + 10) / 2, (p.y + p2.y + 10) / 2);
							}
						}
						else
						{
							g.drawLine(p.x + 5, p.y + 5, p2.x + 5, p2.y + 5);
							g.drawString(l.split(",")[2].substring(0, 3), (p.x
									+ p2.x + 10) / 2, (p.y + p2.y + 10) / 2);
						}
					}
				}
			}
		}
	}

	public static void main(String args[]) throws IOException {
		distanceGUI gui = new distanceGUI();
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setSize(1000, 700);
		gui.setVisible(true);
	}
}