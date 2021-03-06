package View;

import java.awt.Robot;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.swing.JOptionPane;

import Controller.Check;
import Controller.Perform;
import Controller.Scan;

/** This is is the main bot class that determines which actions to take. 
 * 	The actions are ran in the specified order listed in this class 
 * 	for better readability and convenience when changes to company procedures 
 * 	are implemented. **/
public class MainBot implements Runnable
{
	//TODO Should turn public variables into getters. Temporarily using public to test out new functions. 	
	private Frames frames;	
	public boolean release = false;
	public String pinArea[];
	public int currentPin = 0;
	public boolean skipNext = false;
	public String choice = "";
	public Robot robot;
	public Type typeString;

	//public MainBot(Frames frames, int nPins, int interval)
	public MainBot(Frames frames, String choice, boolean release, String pinArea)
	{
		try
		{
			robot = new Robot();
			//this.interval = interval;
			this.frames = frames;
			this.choice = choice;
			this.release = release;
			typeString = new Type();
			this.pinArea = pinArea.split("\n");
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		try
		{
			frames.setProgressState(false);
			Thread.sleep(1000);
			
			
			Perform perform = new Perform(MainBot.this);
			Scan scan = new Scan(MainBot.this, perform);
			Check check = new Check(MainBot.this, perform, scan);

			//Sets out the order of steps to take from the beginning
			for (;currentPin < pinArea.length; currentPin++)
				{				
						perform.searchPin();
						check.searchResults();
						check.noRead();
						perform.exam(choice);
						check.goBack();
						perform.fixError();
						scan.fixedError();
						perform.goBack();
						check.home();
						perform.refreshCDS();
						updatePin();
						toLog();
						
						synchronized(this)
						{
							while (!release)
							{
								wait();
							}
						}
				}
			
			frames.setProgressState(true);
			JOptionPane.showMessageDialog(null, "Done.");
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
	}
	
	/** Allows the other classes to use sleep functions with bot.sleep(n) easily 
	 * 	without having to throw and catch exceptions every time. **/
	public void sleep(int n)
	{
		try
		{
			Thread.sleep(n);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	/** Stops the entire program temporarily. **/
	public void stop()
	{
		System.out.println("Stopping. ");
		release = false;
	}
	
	/** Resumes the program where it left off, but not recommended for regular use because screen may be different and the program may not detect it. **/
	synchronized void resume()
	{
		System.out.println("Resuming. ");
		release = true;
		notify();		//notify wakes up the first thread that called for wait(). 
	}
	
	/** Helps stop the program even if a method is in a loop. ie. checking for pixels. **/
	public void stopped()
	{
		while (!release)
		{
			sleep(1000);
		}
	}
	
	/** Links to Type class for typing string function **/
	public void type(String str)
	{
		typeString.type(str);
	}
	
	/** Appends the current pin number to the no-read text box. **/
	public void appendPin()
	{
		frames.pinArea2.append(pinArea[currentPin]+ "\n");
	}
	
	/** Updates that pins to be released text box by removing pins already released. **/
	public void updatePin()
	{
		String[] updatedPin = Arrays.copyOfRange(pinArea, currentPin + 1, pinArea.length); //+1 for current pins to clear whole box after everything is done. 
		frames.pinArea.setText(Arrays.toString(updatedPin).replace(",", "\n").replace("[", "").replace("]", "").replace(" ", "")); //Default display of toString from array shows square brackets with commas separating each value. This replaces it. 
	}	
	
	/** Creates/updates the log file for pins that need to be released. 
	 * 	This should be equal to the pinArea and pinArea2. **/
	public void toLog()
	{
		//TODO Change path to same as .jar path. 
		try (PrintWriter out = new PrintWriter("h:\\p\\log.txt"))
		{
			String[] rawPinText = Arrays.copyOfRange(pinArea, currentPin + 1, pinArea.length); 
			String currentPinText = Arrays.toString(rawPinText).replace(",", "\r\n").replace("[", "").replace("]", "").replace(" ", "");
			
			//TODO check if noread pin is writing correctly onto file. 
			String[] rawNoreadText = Arrays.copyOfRange(frames.pinArea2.getText().split("\n"), 0, frames.pinArea2.getText().split("\n").length); 
			String currentNoreadText = Arrays.toString(rawNoreadText).replace(",", "\r\n").replace("[", "").replace("]", "").replace(" ", "");
			
			out.print("@@@@@ to be @@@@@. " + "\r\n" + "===================" + "\r\n" + currentPinText
					+ "\r\n\r\n" + "@@@@@" + "\r\n" + "===================" + "\r\n" + currentNoreadText);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
}