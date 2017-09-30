import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame
{
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	
	public Client(String host)
	{
		super("Client");
		serverIP = host;
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
				{
					sendMessage(event.getActionCommand());
					userText.setText("");
				}
			}
		);
		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		setSize(500,500);
		setVisible(true);
	}
	
	public void startRunning()
	{
		try
		{
			connectToServer();
			setupStreams();
			whileChatting();
		}
		catch(EOFException eofException)
		{
			showMessage("\nClient terminated connection");
		}
		catch(IOException ioException)
		{
			ioException.printStackTrace();
		}
		finally
		{
			close();
		}
	}
	
	private void connectToServer() throws IOException
	{
		showMessage("Attempting connection...\n");
		connection = new Socket(InetAddress.getByName(serverIP), 6789);
		showMessage("Connected to: " + connection.getInetAddress().getHostName());
	}
	
	private void setupStreams() throws IOException
	{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are good to go.");
	}
	
	private void whileChatting() throws IOException
	{
		ableToType(true);
		do
		{
			try
			{
				message = (String)input.readObject();
				showMessage("\n" + message);
			}
			catch(ClassNotFoundException classNotFoundException)
			{
				showMessage("\n Something has gone terribly wrong.");
			}
		}while(!message.equals("SERVER - END"));
	}
	
	private void close()
	{
		showMessage("\nClsoing streams and sockets.");
		ableToType(false);
		try
		{
			output.close();
			input.close();
			connection.close();
		}
		catch(IOException ioException)
		{
			ioException.printStackTrace();
		}
		
	}
	
	private void sendMessage(String message)
	{
		try
		{
			output.writeObject("CLIENT - " + message);
			output.flush();
			showMessage("\nCLIENT - " + message);
		}
		catch(IOException ioException)
		{
			chatWindow.append("\nMessage could not be sent!");
		}
	}
	
	private void showMessage(final String TEXT)
	{
		SwingUtilities.invokeLater(
			new Runnable()
			{
				public void run()
				{
					chatWindow.append(TEXT);
				}
			}
		);
	}
	
	private void ableToType(final Boolean TOF)
	{
		SwingUtilities.invokeLater(
			new Runnable()
			{
				public void run()
				{
					userText.setEditable(TOF);
				}
			}
		);
	}
}