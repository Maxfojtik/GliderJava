

import java.io.File;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;

public class Joystick 
{	
	Controller control = null;
	public Joystick()
	{
		System.setProperty("net.java.games.input.librarypath", new File("/home/maxwell/eclipse-workspace/GliderComms/native/linux/x86_64").getAbsolutePath());
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
//        for(int i =0;i<ca.length;i++)
//        {
////        	System.out.println(ca[i].getName());
//        	if(ca[i].getName().contains("Controller"))
//			{
//        		control = ca[i];
//			}
//        }
	}
	Component[] getData()
	{
		if(control==null)
		{
//	        System.out.println("Cant connect to controller");
			Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
	        for(int i =0;i<ca.length;i++)
	        {
	        	System.out.println(ca[i].getName());
	        	if(ca[i].getName().contains("Controller"))
				{
	        		control = ca[i];
				}
	        }
		}
		if(control==null)
		{
			control.poll();
	    	Component[] c = control.getComponents();
	    	for(int ii = 0; ii < c.length; ii++)
			{
	//				System.out.println(ii+":"+c[ii].getPollData());
			}
	    	return c;
		}
		return null;
	}
	float[] oldData;
	boolean overriding = false;
	void logic()
	{
		Component[] c = getData();
		if(c!=null && MicroConnection.canSend)
		{
			if(oldData!=null)
			{
				float[] diffs = new float[c.length];
				for(int ii = 0; ii < c.length; ii++)
				{
					diffs[ii] = c[ii].getPollData()-oldData[ii];
					oldData[ii] = c[ii].getPollData();
				}
				//diffs is now the delta of all the floats, so 1 would be button pressed and -1 would be button released and 0 is no change
	//			System.out.println(c[5].getPollData()+":"+oldData[5]+":"+diffs[5]);
				
				if(diffs[3]>.5 || (c[3].getPollData()>.5 && overriding))//button 5 pressed, return control
				{
					MicroConnection.send("OF");
					overriding = false;
				}
				if(diffs[0]>.5)//button 5 pressed, return control
				{
					MicroConnection.send("ARMT");
				}
				if(diffs[1]>.5)//button 5 pressed, return control
				{
					MicroConnection.send("ARMF");
				}
			}
			else
			{
				oldData = new float[c.length];
				for(int ii = 0; ii < c.length; ii++)
				{
					oldData[ii] = c[ii].getPollData();
				}
			}
			if(Math.abs(c[3].getPollData())>.1 || Math.abs(c[0].getPollData())>.1 || Math.abs(c[2].getPollData())>.1 || overriding)//button 5 pressed, return control
			{
				if(!overriding)
				{
					MicroConnection.send("OT");
				}
				overriding = true;
				/*MicroConnection.send("A"+(int)(c[1].getPollData()*45));
				MicroConnection.send("E"+(int)(c[0].getPollData()*90));*/
				MicroConnection.send("AEP"+"A"+(int)(c[16].getPollData()*45)+":E"+(int)(c[17].getPollData()*90)+":"+":P"+(int)((c[14].getPollData()+1)/2*-90)+":");
			}
		}
	}
}