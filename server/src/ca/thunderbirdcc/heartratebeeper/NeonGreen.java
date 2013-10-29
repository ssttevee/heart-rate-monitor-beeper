package ca.thunderbirdcc.heartratebeeper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class NeonGreen extends JPanel {

	public static int FRAMES_PER_SECOND = 48;
	public static int CALCS_PER_SECOND = 100;
	public static List<Double> points = new ArrayList<Double>();
	public static Double[] slopes = new Double[] {-5D, 10D, -6D, 2D, -1D};
	public static Double Y_AXIS_OFFSET = 0D;
	public static Integer Y_AXIS_MULTIPLYER = 4;
	public static Integer X_AXIS_MULTIPLYER = 4;
	public static Boolean isBeeping = false;
	public static Boolean debug = false;
	
	int frame = 0;
	long prevFrame = System.nanoTime();
	long fps = 0;
	long prevCalc = System.nanoTime();
	long cps = 0;
	long rcps = 0;
	int calcsForBeep = 0;
	
	List<Line2D.Double> lines = new ArrayList<Line2D.Double>();

    
    static {
    	double h = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    	for (int i = 0; i < Toolkit.getDefaultToolkit().getScreenSize().getWidth()/X_AXIS_MULTIPLYER ; i++) {
			points.add(0D);
		}
    	Y_AXIS_OFFSET = h/2;
    }
    
    public NeonGreen() {
    	setBackground(Color.BLACK);
	}

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(0x83F52C));
        if(debug) {
	        g2d.drawString(frame + "", 5, 25);
	        g2d.drawString("FPS: " + fps, 5, 40);
	        g2d.drawString("CPS: " + cps, 5, 55);
	        g2d.drawString(System.out.toString(), 5, 70);
        }
        g2d.setStroke(new BasicStroke(10));
        
        calculateLines();
        for (int i = 0; i < lines.size(); i++) {
			g2d.draw(lines.get(i));
		}
        
//        for (int i = 0; i < points.size(); i++) {
//            g2d.draw(new Line2D.Double(i*X_AXIS_MULTIPLYER, (points.get(i) * Y_AXIS_MULTIPLYER) + X_AXIS_OFFSET, i*X_AXIS_MULTIPLYER, (points.get(i) * Y_AXIS_MULTIPLYER) + X_AXIS_OFFSET));
//		}
    }
    
    private void calculateLines() {
    	Integer firstX = null;
    	Integer lastX = null;
    	Double firstY = null;
    	Double lastY = null;
    	Double ptDiff = null;
    	lines.clear();
    	for (int i = 0; i < points.size(); i++) {
			if (firstY == null) {
				firstY = points.get(i);
				firstX = i;
			} else if (ptDiff == null) {
				lastY = points.get(i);
				lastX = i;
				ptDiff = firstY - lastY;
			} else if (ptDiff != firstY - points.get(i) || i == points.size() - 1) {
				lines.add(new Line2D.Double(firstX*X_AXIS_MULTIPLYER, (firstY * Y_AXIS_MULTIPLYER) + Y_AXIS_OFFSET, lastX*X_AXIS_MULTIPLYER, (lastY * Y_AXIS_MULTIPLYER) + Y_AXIS_OFFSET));
				firstY = lastY;
				firstX = lastX;
		    	lastX = null;
		    	lastY = null;
		    	ptDiff = null;
			} else if (ptDiff == firstY - points.get(i)) {
				lastY = points.get(i);
				lastX = i;
			}
		}
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }

    public void startAnimation() {
        new Thread(new Animator()).start();
        new Thread(new PointCalculator()).start();
    }

    private class Animator implements Runnable {
		public void run() {
	        long beforeTime, timeDiff, sleepTime;
	        beforeTime = System.currentTimeMillis();
	        while (true) {
	        	frame++;
	        	if(frame % 24 == 0) fps = 1000000000/(System.nanoTime() - prevFrame);
	        	if(frame % 24 == 0) cps = rcps;
	            NeonGreen.this.repaint();
	        	prevFrame = System.nanoTime();

	            timeDiff = System.currentTimeMillis() - beforeTime;
	            sleepTime = 1000/FRAMES_PER_SECOND - timeDiff;
	            
	            if (sleepTime <= 0L) { // Even if doing stuff took longer than
	                sleepTime = 0L; // sleep some anyway
	            }
	            
	            try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	            beforeTime = System.currentTimeMillis();
	        }
		}
    }

    private class PointCalculator implements Runnable {
        public void run() {
	        long beforeTime, timeDiff, sleepTime;
	        beforeTime = System.currentTimeMillis();
	        while (true) {
	        	prevCalc = System.nanoTime();
	        	calculatePoints();


	            timeDiff = System.currentTimeMillis() - beforeTime;
	            sleepTime = 1000/CALCS_PER_SECOND - timeDiff;

	            if (sleepTime <= 0L) { // Even if doing stuff took longer than
	                                    // period
	                sleepTime = 0L; // sleep some anyway
	            }

	            try {
	                Thread.sleep(sleepTime);
	            } catch (InterruptedException e) {
	            }

	            beforeTime = System.currentTimeMillis();
	            rcps = 1000000000/(System.nanoTime() - prevCalc);
	        }
        }
    }
    
    private void calculatePoints() {
    	points.remove(0);
    	if(isBeeping && calcsForBeep < 50) {
        	points.add(points.get(points.size() - 1) + slopes[(int) Math.floor(calcsForBeep/10)]);
        	calcsForBeep++;
        	if(calcsForBeep == 49) isBeeping = false;
    	} else {
    		points.add(0D);
    		calcsForBeep = 0;
    	}
    }
}