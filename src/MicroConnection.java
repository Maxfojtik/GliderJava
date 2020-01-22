

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

import org.json.JSONObject;

import com.fazecast.jSerialComm.SerialPort;


public class MicroConnection
{
	static SerialPort commPort;
	static BufferedReader input;
	static BufferedWriter output;
	static boolean connected = false;
	static LinkedList<String> toSend = new LinkedList<String>();
	static boolean canSend = true;
	public MicroConnection()
	{
		tryConnection();
	}
	static void tryConnection()
	{
//		System.out.println("Trying to connect");
		connected = false;
		try
		{
//			System.out.println(.GET);
			SerialPort attemptPort = SerialPort.getCommPorts()[0];//SerialPort.getCommPort("Arduino Uno");
			attemptPort.setBaudRate(115200);
//			System.out.println("Trying "+attemptPort.getDescriptivePortName());
			Frame.print("Trying "+attemptPort.getDescriptivePortName());
			attemptPort.openPort();
//			Thread.sleep(1000);
			output = new BufferedWriter(new OutputStreamWriter(attemptPort.getOutputStream()));
			output.write("ping\n");
			output.flush();
			InputStreamReader ISR = new InputStreamReader(attemptPort.getInputStream());
			input = new BufferedReader(ISR);
//			Thread.sleep(100);
			if(input.ready())
			{
				String str = input.readLine();
//				System.out.println(str);
				if(str.equals("pongRF") || true)
				{
					commPort = attemptPort;
					connected = true;
				}
			}
			else
			{
				input.close();
				output.close();
				attemptPort.closePort();
			}
		}
		catch(Exception e) {}
		if(connected)
		{
//			System.out.println("CONNECTED TO SERIAL");
			Frame.print("CONNECTED TO SERIAL");
		}
		else
		{
			input = null;
			Frame.print("Cant connect");
//			System.out.println("Cant connect");
		}
	}
	static void disconnect()
	{
		if(commPort!=null)
		{
			commPort.closePort();
		}
	}
	static boolean sendRaw(String event)
	{
		if(connected)
		{
			Frame.print("->"+event);
			DataLogging.log("->"+event);
//			System.out.println("<-"+event);
			try {
				output.write(event+"\n");
				output.flush();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		tryConnection();
		return false;
	}
	static boolean send(String event)
	{
		if(connected)
		{
			toSend.add(event);
			return true;
		}
		tryConnection();
		return false;
	}
	static String building = "";
	static String readLine()
	{
		int time = 0;
		while(!read() && time<2000)
		{
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			time++;
		}
		if(time==2000)
		{
			return null;
		}
		String copy = building;
		building = "";
		return copy;
	}
	static boolean read()
	{
		try {
			while(input.ready())
			{
				char ch = (char) input.read();
				if(ch=='\n')
				{
					return true;
				}
				else
				{
					if(ch!='\r')
					{
						building += ch;
					}
				}
			}
		} catch (IOException e) {
//			e.printStackTrace();
		}
		return false;
	}
	static boolean sbool(String on)
	{
		return on.equals("T");
	}
	static void parseG(String[] subTelem)
	{
		JSONObject GPS = new JSONObject();
		boolean goodGPS = true;
		for(int i = 0; i < subTelem.length; i++)
		{
			String[] subTelemParts = subTelem[i].split(":");
			if(subTelemParts[0].equals("A"))
			{
				GPS.put("fix", sbool(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("B"))
			{
				double lat = Double.parseDouble(subTelemParts[1]);
				if(Math.abs(lat)<10)
				{
					goodGPS = false;
				}
				GPS.put("lat", lat);
			}
			else if(subTelemParts[0].equals("C"))
			{
				double lon = Double.parseDouble(subTelemParts[1]);
				if(Math.abs(lon)<10)
				{
					goodGPS = false;
				}
				GPS.put("lon", lon);
			}
			else if(subTelemParts[0].equals("D"))
			{
				GPS.put("sat", Integer.parseInt(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("E"))
			{
				GPS.put("alt", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("F"))
			{
				GPS.put("speed", Double.parseDouble(subTelemParts[1]));
			}
		}
		if(!goodGPS)
		{
			GPS.put("fix", false);
		}
		GliderComms.lastTelem.put("GPS", GPS);
	}
	static void parseA(String[] subTelem)
	{
		JSONObject telem = GliderComms.lastTelem;
		for(int i = 0; i < subTelem.length; i++)
		{
			String[] subTelemParts = subTelem[i].split(":");
			if(subTelemParts[0].equals("Y"))
			{
				telem.put("AY", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("P"))
			{
				telem.put("AP", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("R"))
			{
				telem.put("AR", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("H"))
			{
				telem.put("H", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("GY"))
			{
				telem.put("GY", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("GP"))
			{
				telem.put("GP", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("GR"))
			{
				telem.put("GR", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("GT"))
			{
				telem.put("GT", Long.parseLong(subTelemParts[1]));
			}
		}
	}
	static void parseT(String[] subTelem)
	{
		JSONObject telem = GliderComms.lastTelem;
		for(int i = 0; i < subTelem.length; i++)
		{
			String[] subTelemParts = subTelem[i].split(":");
			if(subTelemParts[0].equals("S"))
			{
				telem.put("targetSpeed", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("H"))
			{
				telem.put("targetHeading", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("R"))
			{
				telem.put("targetRoll", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("P"))
			{
				telem.put("targetPitch", Double.parseDouble(subTelemParts[1]));
			}
		}
	}
	static void parseS(String[] subTelem)
	{
		JSONObject telem = GliderComms.lastTelem;
		for(int i = 0; i < subTelem.length; i++)
		{
			String[] subTelemParts = subTelem[i].split(":");
			if(subTelemParts[0].equals("P"))
			{
				telem.put("pressure", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("T"))
			{
				telem.put("temperature", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("A"))
			{
				telem.put("altitude", Double.parseDouble(subTelemParts[1]));
			}
		}
	}
	static void parseM(String[] subTelem)
	{
		JSONObject telem = GliderComms.lastTelem;
		for(int i = 0; i < subTelem.length; i++)
		{
			String[] subTelemParts = subTelem[i].split(":");
			if(subTelemParts[0].equals("V"))
			{
				telem.put("voltage", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("T"))
			{
				telem.put("targetHeading", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("A"))
			{
				boolean armed = sbool(subTelemParts[1]);
				if(armed && !GliderComms.toArm)
				{
					MicroConnection.send("ARMF");
				}
				if(!armed && GliderComms.toArm)
				{
					MicroConnection.send("ARMT");
				}
				telem.put("arm", armed);
			}
			else if(subTelemParts[0].equals("O"))
			{
				telem.put("over", sbool(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("L"))
			{
				telem.put("averageLoopTime", Double.parseDouble(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("I"))
			{
				telem.put("millis", Long.parseLong(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("N"))
			{
				telem.put("motorL", Integer.parseInt(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("P"))
			{
				telem.put("motorR", Integer.parseInt(subTelemParts[1]));
			}
			else if(subTelemParts[0].equals("S"))
			{
				int state = Integer.parseInt(subTelemParts[1]);
				if(state==-1 && GliderComms.toEnable)
				{
					MicroConnection.send("ENAT");
				}
				if(state!=-1 && GliderComms.toEnable)
				{
					GliderComms.toEnable = false;
				}
				telem.put("state", state);
			}
		}
	}
	
	static void logic()
	{
		try 
		{
			if(input!=null && input.ready())
			{
				String line = readLine();
				if(line!=null)
				{
					Frame.print("<-"+line);
//					System.out.println(line);
					if(line.equals("ok"))
					{
						canSend = true;
					}
					else if(line.contains("->"))//ignore it
					{
						
					}
					else
					{
						if(line.startsWith("G,"))
						{
							String[] subTelem = line.split(",");
							parseG(subTelem);
						}
						if(line.startsWith("A,"))
						{
							String[] subTelem = line.split(",");
							parseA(subTelem);
						}
						if(line.startsWith("T,"))
						{
							String[] subTelem = line.split(",");
							parseT(subTelem);
						}
						if(line.startsWith("M,"))
						{
							String[] subTelem = line.split(",");
							parseM(subTelem);
						}
						if(line.startsWith("S,"))
						{
							String[] subTelem = line.split(",");
							parseS(subTelem);
						}
						if(line.startsWith("R,"))
						{
							GliderComms.lastTelem.put("RSSI", Double.parseDouble(line.split(",")[1]));
						}
						else
						{
							GliderComms.lastTeleRece = System.currentTimeMillis();
							GliderComms.lastTelem.put("Connected", true);
							GliderComms.lastTelem.put("Time", System.currentTimeMillis());
							DataLogging.logTelem(GliderComms.lastTelem.toString());
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(canSend && toSend.size()>0)
		{
			sendRaw(toSend.pop());
//			canSend = false;
		}
		while(!MicroConnection.connected)
		{
			MicroConnection.tryConnection();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
