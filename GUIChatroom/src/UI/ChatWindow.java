package UI;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChatWindow extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HashMap<String, ImageIcon> expressionManager=new HashMap<>();
	ImageIcon expression=new ImageIcon("./pic/Smile.png");
	JPanel expressionArea=new JPanel();
	JButton send=new JButton("发送");
	JButton bar=new JButton();
	JTextPane inputArea=new JTextPane();
	JTextPane outputArea=new JTextPane();
	JScrollPane inputScroll=new JScrollPane(inputArea);
	JScrollPane outputScroll=new JScrollPane(outputArea);
	JLabel hint=new JLabel("(按alt+Enter发送)");
	SimpleAttributeSet attrset = new SimpleAttributeSet();
    StyledDocument outputDoc;
	Socket socket;
	String myName;
	String chatWithID;
	String myID;
	boolean isOpening=false;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  
	public ChatWindow(Socket socket,String chatWithID,String chatWithName,String myID,String myName){
		super("与"+chatWithName+"聊天中...");
		this.socket=socket;
		this.chatWithID=chatWithID;
		this.myName=myName;
		this.myID=myID;
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
	             setVisible(false);
	             turnOff();
	        }
		});
		StyleConstants.setFontSize(attrset,16);
		outputDoc=outputArea.getStyledDocument();
		inputArea.setText("");
		outputArea.setText("");
		isOpening=true;
		expressionArea.setBounds(10, 205, 250, 110);
		expressionArea.setBackground(Color.WHITE);
		expressionArea.setLayout(null);
		expressionArea.setVisible(false);
		add(expressionArea);
		int count=1;
		for(int i=0;i<3;i++)
			for(int j=0;j<7;j++){
				JButton b=new JButton();
				b.setBackground(Color.WHITE);
				b.setBorderPainted(false);
				b.setBounds(5+j*35, 5+i*35, 30, 30);
				b.setIcon(new ImageIcon("./pic/emoji_"+String.format("%02d", count)+".png"));
				b.setName("emoji_"+String.format("%02d", count));
				b.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						int position = inputArea.getCaretPosition();
						try{
							ImageIcon image=new ImageIcon("./pic/"+b.getName()+".png");
							image.setDescription(b.getName());
							inputArea.insertIcon(image);
							inputArea.setCaretPosition(position+1);
							inputArea.requestFocus();
							expressionArea.setVisible(false);
						}catch(Exception e1){
							inputArea.setCaretPosition(position);
							return ;
						}
					}
				});
				expressionManager.put(b.getName(), new ImageIcon("./pic/emoji_"+String.format("%02d", count)+".png"));
				expressionArea.add(b);
				count++;
			}
	}
	public void paint(){
		inputArea.requestFocus();
		setSize(550,500);
		setVisible(true);
		centered(this);
		setResizable(false);
		outputArea.setEditable(false);
		hint.setForeground(Color.GRAY);
		bar.setBounds(15, 320, 25, 25);
		bar.setBackground(Color.WHITE);
		bar.setIcon(expression);
		bar.setBorderPainted(false);
		bar.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if(expressionArea.isVisible()){
					expressionArea.setVisible(false);
				}
				else expressionArea.setVisible(true);
			}
		});
		send.setMnemonic(KeyEvent.VK_ENTER); 
		send.setBounds(350, 430, 60, 30);
		hint.setBounds(410,430,100,30);
		add(hint);
		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				try {
					OutputStream out=socket.getOutputStream();
					String neededSendMessage="";
					if(!"".equals(inputArea.getText())){
				        try {
				        	outputDoc.insertString(outputDoc.getLength(), "我\t"+sdf.format(new Date())+"\n", attrset);
							StyledDocument inputDoc=inputArea.getStyledDocument();
							for(int i=0;i<inputDoc.getLength();i++){
								if(inputDoc.getCharacterElement(i).getName().equals("icon")){
									neededSendMessage+=">";
									Element ele=inputDoc.getCharacterElement(i);
									ImageIcon icon=(ImageIcon)StyleConstants.getIcon(ele.getAttributes());
									neededSendMessage+=icon.getDescription();
									neededSendMessage+=">";
									outputArea.insertIcon(icon);
								}
								else {
									String str =inputDoc.getText(i, 1);
									neededSendMessage+=str;
									outputDoc.insertString(outputArea.getCaretPosition(), str,attrset);
								}
							}
							outputDoc.insertString(outputArea.getCaretPosition(), "\n", attrset);
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        outputArea.setCaretPosition(outputArea.getStyledDocument().getLength());
						out.write(("send/"+myName+"from"+myID+"to"+chatWithID+"::"+neededSendMessage).getBytes());
						inputArea.setText("");
						inputArea.requestFocus();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		add(bar);
		inputScroll.setBounds(8,345,530,80);
		outputScroll.setBounds(8, 4, 530, 315);
		inputScroll.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		outputScroll.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.getContentPane().add(inputScroll);
		this.getContentPane().add(outputScroll);
		setLayout(null);
		
		add(send);
		validate();
	}
	public void display(String sender,String message,boolean hasUnRead){
		StringTokenizer stringToken=new StringTokenizer(message, ">");
		if(hasUnRead){
			try {
				outputDoc.insertString(outputDoc.getLength(),sender+"\t", attrset);
				while(stringToken.hasMoreTokens()){
					String tempStr=stringToken.nextToken();
					ImageIcon image=expressionManager.get(tempStr);
					System.out.println(tempStr);
					if(image!=null){
						outputArea.setCaretPosition(outputDoc.getLength());
						outputArea.insertIcon(image);
						outputDoc.insertString(outputDoc.getLength()," ", attrset);
					}
					else {
						outputDoc.insertString(outputDoc.getLength(),tempStr, attrset);
					}
				}
				outputDoc.insertString(outputDoc.getLength(),"\n", attrset);
				
			} catch (BadLocationException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			try {
				outputDoc.insertString(outputDoc.getLength(), sender+"\t"+sdf.format(new Date())+"\n", attrset);
				while(stringToken.hasMoreTokens()){
					String tempStr=stringToken.nextToken();
					ImageIcon image=expressionManager.get(tempStr);
					System.out.println(tempStr);
					if(image!=null){
						outputArea.setCaretPosition(outputDoc.getLength());
						outputArea.insertIcon(image);
						outputDoc.insertString(outputDoc.getLength()," ", attrset);
					}
					else {
						outputDoc.insertString(outputDoc.getLength(),tempStr, attrset);
					}
				}
				outputDoc.insertString(outputDoc.getLength(),"\n", attrset);
			} catch (BadLocationException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		outputArea.setCaretPosition(outputArea.getStyledDocument().getLength());
	}
	
	public void centered(Container container) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int w = container.getWidth();
        int h = container.getHeight();
        container.setBounds((screenSize.width - w) / 2,
                (screenSize.height - h) / 2, w, h);
    }
	public void showWarning(){
		try {
			outputDoc.insertString(outputDoc.getLength(), "系统消息：对方已下线，将无法接受您发送的消息！", attrset);
		} catch (BadLocationException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void showWindow(){
		setVisible(true);
	}
	public void turnOn(){
		isOpening=true;
	}
	public void turnOff(){
		isOpening=false;
	}
	public boolean isOn(){
		return isOpening;
	}
}
