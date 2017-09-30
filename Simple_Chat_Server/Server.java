import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame
{
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	
	public Server()
	{
		super("Instant Messenger");
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
		add(new JScrollPane(chatWindow));
		setSize(500, 500);
		setVisible(true);
	}
	
	public void startRunning()
	{
		try
		{
			server = new ServerSocket(6789, 100);
			while(true)
			{
				try
				{
					waitForConnection();
					setupStreams();
					whileChatting();
				}
				catch(EOFException eofException)
				{
					showMessage("\nServer ended the connection!");
				}
				finally
				{
					close();
				}
			}
		}
		catch(IOException ioException)
		{
			ioException.printStackTrace();
		}
	}
	
	private void waitForConnection() throws IOException
	{
		showMessage("Waiting for someone to connect...\n");
		connection = server.accept();
		showMessage("Now connected to " + connection.getInetAddress().getHostName());
	}
	
	private void setupStreams() throws IOException
	{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\nStreams are now setup!\n");
	}
	
	private void whileChatting() throws IOException
	{
		String message = "You are now connected!";
		sendMessage(message);
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
				showMessage("\nSomething has gone terribly wrong");
			}
		}while(!message.equals("CLIENT - END"));
	}
	
	private void close()
	{
		showMessage("\nClosing connections...\n");
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
			output.writeObject("SERVER - " + message);
			output.flush();
			showMessage("\nSERVER - " + message);
		}
		catch(IOException ioException)
		{
			chatWindow.append("\n ERROR: MESSAGE CANNOT BE SENT");
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