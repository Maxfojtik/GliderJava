
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class Frame 
{
	static JFrame f = new JFrame("Airplane");
	static JTextArea console = new JTextArea(20,50);
	public Frame()
	{
		f.add(console);
		f.pack();
		f.setResizable(false);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent e) {
		        e.getWindow().dispose();
		        DataLogging.stop();
		    }
		});
	}
	static void print(String out)
	{
		System.out.println(out);
		String text = console.getText()+out+"\n";
		String[] lines = text.split("\n");
		if(lines.length>=console.getRows())
		{
//			System.out.println("too many lines");
			text = "";
			for(int i = lines.length-console.getRows(); i < lines.length; i++)
			{
				text += lines[i]+"\n";
			}
		}
		console.setText(text);
	}
}
