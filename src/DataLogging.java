import java.io.FileWriter;
import java.io.IOException;

public class DataLogging 
{
	static FileWriter fileWriter;
	public DataLogging()
	{
		
	}
	static void start()
	{
		Frame.print("LOG START");
		try {
			fileWriter = new FileWriter("/home/maxwell/GliderData/"+System.currentTimeMillis()+".txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static void stop()
	{
		Frame.print("LOG STOP");
		if(fileWriter!=null)
		{
		    try {
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	static void log(String line)
	{
		if(line.contains("ping"))
		{
			return;
		}
		if(fileWriter==null)
		{
			start();
		}
		try {
			fileWriter.write(line+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static int num = 0;
	static void logTelem(String line)
	{
		if(num%5==0)
		{
			if(fileWriter==null)
			{
				start();
			}
			try {
				fileWriter.write(line+"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		num++;
	}
}
