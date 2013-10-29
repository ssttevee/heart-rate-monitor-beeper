/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package ca.thunderbirdcc.heartratebeeper;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.hardcode.jxinput.JXInputManager;
import de.hardcode.jxinput.directinput.DirectInputDevice;
import de.hardcode.jxinput.event.JXInputButtonEvent;
import de.hardcode.jxinput.event.JXInputButtonEventListener;
import de.hardcode.jxinput.event.JXInputEventManager;

public class BeeperStandalone extends JFrame implements Runnable{

    private boolean isLongBeep = false;
    private String currentFile = "short";
    private boolean isRunning = true;

    public BeeperStandalone() {
    }

    private void initUI() {

        setTitle("Heart Rate Monitor");
        
        NeonGreen s;
        add(s = new NeonGreen());
        s.startAnimation();
        
        addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent event) {
	            int keyCode = event.getKeyCode();
	            if (keyCode == KeyEvent.VK_D) NeonGreen.debug = !NeonGreen.debug;
	            else if (keyCode == KeyEvent.VK_ESCAPE) {
	            	isLongBeep = false;
	            	isRunning = false;
	            }
	            else isLongBeep = !isLongBeep;
			}
		});
        
        
		Toolkit tk = Toolkit.getDefaultToolkit();
		int xSize = ((int) tk.getScreenSize().getWidth());
		int ySize = ((int) tk.getScreenSize().getHeight());
		setSize(xSize,ySize);
		setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

	public void run() {
	    try {
			Clip clip = AudioSystem.getClip();
	    	clip.open(AudioSystem.getAudioInputStream(getClass().getResource("/short_beep.wav")));
        	System.out.println("Initializing...");
        	System.out.println("Loading Sounds...");
        	System.out.println("");
        	System.out.println("INSTRUCTIONS:");
        	System.out.println("=============");
        	System.out.println("Press enter to switch between short and long beeps");
        	System.out.println("");

			
			do {

		    	if(!currentFile.equals("long")) Thread.sleep(1500);
		    	
		    	if(isLongBeep) {
		    		if(currentFile.equals("short")) {
				    	currentFile = "long";
			        	System.out.print(currentFile);
		    			clip.close();
				    	clip.open(AudioSystem.getAudioInputStream(getClass().getResource("/long_beep.wav")));
				    	clip.loop(Clip.LOOP_CONTINUOUSLY);
		    		}
		    	} else {
		    		if(currentFile.equals("long")) {
				    	currentFile = "scream";
			        	System.out.println(currentFile);
		    			clip.stop();
		    			clip.close();
				    	clip.open(AudioSystem.getAudioInputStream(getClass().getResource("/scream.wav")));
				    	clip.loop(0);
		    		} else if(currentFile.equals("scream")) {
				    	currentFile = "short";
			        	System.out.print("waiting 3s");
				    	Thread.sleep(1000);System.out.print(".");
				    	Thread.sleep(1000);System.out.print(".");
				    	Thread.sleep(1000);System.out.println(".");
			        	System.out.print(currentFile);
		    			clip.close();
				    	clip.open(AudioSystem.getAudioInputStream(getClass().getResource("/short_beep.wav")));
				    	clip.loop(0);
		    		}
		    	}
		    	
		    	if(!isLongBeep) clip.setFramePosition(0);
		    	if(currentFile.equals("short")) NeonGreen.isBeeping = true;
		    	clip.start();
		    	

		    	while (isLongBeep) Thread.sleep(100);
		    	
			} while (isRunning);
			
			System.exit(0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void main(String[] args) throws Exception {
		
        
        final BeeperStandalone b = new BeeperStandalone();
        new Thread(b).start();
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	b.setBackground(Color.BLACK);
            	b.initUI();
                b.setVisible(true);
            }
        });
        



		System.out.println("loading " + System.getProperty("user.dir") + "/jxinput.dll");
        System.load(System.getProperty("user.dir") + "/jxinput.dll");
        
        JXInputEventManager.setTriggerIntervall( 50 );
        for(int i = 0; i < JXInputManager.getNumberOfDevices(); i++){
			System.out.println(JXInputManager.getJXInputDevice(i));
                if(JXInputManager.getJXInputDevice(i).getName().equals("Controller (XBOX 360 For Windows)")){
                        DirectInputDevice xbox = new DirectInputDevice(i);
                        
                        JXInputEventManager.addListener(
	                        new JXInputButtonEventListener() {
								public void changed(JXInputButtonEvent ev) {
						        	b.isLongBeep = !b.isLongBeep;
								}
							}, xbox.getButton(0));
                        
                }
        }
        
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line = in.readLine();
        
        while (line.equalsIgnoreCase("quit") == false) {
        	b.isLongBeep = !b.isLongBeep;

			line = in.readLine();
        }
        
        b.isRunning = false;
        
        in.close();

    }
}