package UI;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class UsersList extends JFrame{
	/**
	 * 该类是用户列表的类
	 */
	private static final long serialVersionUID = 1L;
	Socket socket;
	String myID;
	JScrollPane scroll=new JScrollPane();
	HashMap<String, String> chatWindowState=new HashMap<>(); //记录当前用户和其好友的聊天窗口的打开情况
	HashMap<String,JButton> buttonManager=new HashMap<>();//好友列表中的好友展示都是通过JButton组件，这个属性就是记录当前用户和好友选项的映射
	HashMap<String, ArrayList<String>> unReadMessage=new HashMap<>();//记录当前用户和某一个好友之间有没有未读消息
	HashMap<String, ChatWindow> openedWindows=new HashMap<>(); //当前用户的好友的id和其聊天窗口的映射
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	boolean isSend=true;
	public UsersList(Socket socket){
		super("用户列表");
		this.socket=socket;
	}
	public void paint(){
		/*
		 * 该方法用来绘制该frame，并重新定义了该frame的关闭按钮操作，点击关闭之后表示用户下线，则向服务器发送exit指令
		 */
		setBounds(900,100,280,500);
		setVisible(true);
		setResizable(false);
		setLayout(null);
		this.getContentPane().add(scroll);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				try {
					OutputStream out=socket.getOutputStream();
					out.write(("exit/"+myID).getBytes());
					out.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }
		});
		validate();
	}
	public void updateList(HashMap<String, String> map,String[] ids){
		int i=0;
		chatWindowState.clear();
		for(String id:ids){
			chatWindowState.put(id, "close");
			if(id.equals(myID)){
				continue;
			}
			buttonManager.put(id, new JButton());
			setButton(buttonManager.get(id),map.get(id),id, i);
			i++;
		}
	}
	private void setButton(JButton button,String nickName,String chatWithID,int sort){
		button.setText(nickName);
		button.setBounds(2,5+45*sort, 270,40);
		button.setBorderPainted(false);
		button.setName(chatWithID);
		button.setBackground(Color.white);
		add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(getOpenedWindow(button.getName())==null||!getOpenedWindow(button.getName()).isOpening){
					if(getOpenedWindow(button.getName())!=null)
						getOpenedWindow(button.getName()).turnOn();
					if(button.getBackground()==Color.white){
						try{
							OutputStream out=socket.getOutputStream();
							out.write(("chat/W"+myID+","+button.getName()+";"+button.getText()).getBytes());
							out.flush();
						}catch(IOException e1){
							e1.printStackTrace();
						}
					}
					else {
						try{
							button.setBackground(Color.WHITE);
							OutputStream out=socket.getOutputStream();
							ArrayList<String> arrayMessage=unReadMessage.get(button.getName());
							String messages="";
							for(String s:arrayMessage){
								messages+=s;
							}
							unReadMessage.put(button.getName(), null);
							out.write(("chat/R"+myID+","+button.getName()+";"+button.getText()+"::"+messages).getBytes());
							out.flush();
						}catch(IOException e1){
							e1.printStackTrace();
						}
					}
				}
			}
		});
	}
	public void setMyID(String id){
		myID=id;
	}
	public void setOpenState(String uid){
		chatWindowState.put(uid, "open");
	}
	public boolean isClosed(String uid){
		if("close".equals(chatWindowState.get(uid))){
			return true;
		}
		return false;
	}
	public HashMap<String, JButton> getButtonManager() {
		return buttonManager;
	}
	public void setButtonManager(HashMap<String, JButton> buttonManager) {
		this.buttonManager = buttonManager;
	}
	public void setUnReadMessage(String uid,String message){
		ArrayList<String> messages=unReadMessage.get(uid);
		if(messages==null){
			messages=new ArrayList<>();
			messages.add(sdf.format(new Date()));
		}
		messages.add("\n·"+message);
		unReadMessage.put(uid, messages);
	}
	public void userLogout(String uid){
		this.remove(buttonManager.get(uid));
		buttonManager.remove(uid);
		this.validate();
		this.paint();
	}
	public void openWindow(String uid,ChatWindow window){
		openedWindows.put(uid, window);
	}
	public ChatWindow getOpenedWindow(String uid){
		return openedWindows.get(uid);
	}
	
}

