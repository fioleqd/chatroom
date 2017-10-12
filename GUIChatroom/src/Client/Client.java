package Client;
import java.awt.Color;
import java.io.InputStream;
import java.net.Socket;

import UI.ChatWindow;
import UI.UsersList;
import UI.WelcomePage;
import changer.DataChanger;

public class Client{
	public static void main(String args[]){
		try{
			Socket socket=new Socket("39.108.182.76",6666);
			boolean firstLogin=true;
			boolean notGetID=true;
			WelcomePage page=null;
			UsersList usersList=null;
			String uid=null;
			while(true){
				if(firstLogin){				//如果首次登录就实例化用户列表
					usersList=new UsersList(socket);
					page=new WelcomePage(socket,usersList);
					firstLogin=false;
				}
				else{		//如果不是登录，根据服务器传来的数据完成相依的操作
					InputStream in=socket.getInputStream();
					String str=null;
					byte[] bytes=new byte[1024];
					int len=0;
					len=in.read(bytes); //当客户端用循环接收来自服务器的消息时会阻塞在这一句
					str=new String(bytes,0,len);
					/*
					 * 分支用来获取当前登录用户的id，并赋值给userlist的myid属性
					 * 因为服务器开启了多个线程，每次处理不同线程时会导致不同用户的id相互交换，所以用notGetID属性来标记
					*/
					if(notGetID){           
						uid=str.substring(str.indexOf('@')+1);
						usersList.setMyID(uid);
						notGetID=false;
					}
					/*
					 * 通过读取缓冲区的服务器数据来完成具体操作
					 */
					String type=str.substring(0,str.indexOf('/'));
					str=str.substring(str.indexOf('/')+1);
					/*
					 * login是当前用户登录的指令
					 */
					if("login".equals(type)){
						str=str.substring(0,str.indexOf('@'));
						usersList.paint();
						usersList.updateList(DataChanger.returnIDName(str),DataChanger.returnID(str));
					}
					/*
					 * update是有其他用户上线，来通知所有用户
					 */
					if("update".equals(type)){
						usersList.updateList(DataChanger.returnIDName(str),DataChanger.returnID(str));
					}
					/*
					 * 这里的chat指令表示的是从用户列表中选择好友来聊天，而具体的输入聊天内容的操作是下面的send指令
					 * 当选择好友来聊天时会有两种不同的状态，即该好友发送的信息都已经阅读或者该好友发送的信息未读
					 * 用mode变量来模拟这两种模式，W表示没有未读的消息，R表示有未读的消息
					 * 
					 * 当前用户列表中的好友点击打开聊天窗口对于同一个好友只能打开一次，如果该窗口已经打开那么该操作没有意义
					 * 如果未打开则有两种情况：
					 * 一种是当前用户在本次登录之后未曾和该好友聊天，那就需要实例化一个新的ChatWindow对象
					 * 另一种是当前用户在本次登录之后和该好友聊过天（即已经实例化过ChatWindow对象，但是由于点击窗口关闭按钮导致窗口隐藏），
					 * 那就直接获取该对象并使得该对象可见即可
					 */
					if("chat".equals(type)){  
						char mode=str.charAt(0);
						if(mode=='W'){
							String senderID=str.substring(str.indexOf("from")+4,str.indexOf("to"));
							String receiverID=str.substring(str.indexOf("to")+2,str.indexOf(';'));
							String receiverName=str.substring(str.indexOf(';')+1);
							if(usersList.getOpenedWindow(receiverID)==null){
								ChatWindow chatWindow=new ChatWindow(socket,receiverID ,receiverName,senderID,page.returnName());
								chatWindow.paint();
								chatWindow.turnOn();
								usersList.openWindow(receiverID, chatWindow);
								usersList.setOpenState(receiverID);
							}
							else {
								usersList.getOpenedWindow(receiverID).showWindow();
							}
									
						}
						if(mode=='R'){
							String senderID=str.substring(str.indexOf("from")+4,str.indexOf("to"));
							String receiverID=str.substring(str.indexOf("to")+2,str.indexOf(';'));
							String receiverName=str.substring(str.indexOf(';')+1,str.indexOf("::"));
							String message=str.substring(str.indexOf("::")+2);
							ChatWindow chatWindow=null;
							if(usersList.getOpenedWindow(receiverID)==null){ //这个分支表示当前窗口未打开过，即尚未实例化
								chatWindow=new ChatWindow(socket,receiverID ,receiverName,senderID,page.returnName());
								chatWindow.paint();
								chatWindow.display( receiverName, message,true);
								usersList.openWindow(receiverID, chatWindow);
								usersList.setOpenState(receiverID);
								
							}
							else {		//这个分支表示当前窗口已经实例化，但是目前该窗口已经隐藏
								usersList.getOpenedWindow(receiverID).showWindow();
								usersList.getOpenedWindow(receiverID).display( receiverName, message,true);
							}
						}
								
					}
					/*
					 * send是在聊天窗口点击发送按钮之后的操作，对于向好友发送信息有两种情况：
					 * 一种是选择的该好友在其客户端上也打开了和当前用户的聊天窗口
					 * 另一种是选择的该好友未在其客户端上打开和当前用户的聊天窗口
					 * 和上面的chat指令不同，上面对于聊天窗口的操作都是传送的receiverID变量作为参数，而这里传送的是senderID变量作为参数
					 * 因为chat操作是主动打开好友聊天，而这里是有人发送了信息，我选择该好友的来查看信息，所以发送者和接收者是相反的
					 */
					if("send".equals(type)){
						String senderID=str.substring(str.indexOf('/')+1,str.indexOf(','));
						String senderName=str.substring(str.indexOf(',')+1,str.indexOf("::"));
						String sendMessage=str.substring(str.indexOf("::")+2);
						if(usersList.getOpenedWindow(senderID)==null||!usersList.getOpenedWindow(senderID).isOn()){
							usersList.getButtonManager().get(senderID).setBackground(new Color(204,232,207));
							usersList.setUnReadMessage(senderID, sendMessage);
							
						}
						else{
							usersList.getOpenedWindow(senderID).display(senderName, sendMessage,false);
						}
					}
					/*
					 * exit是下线指令，有某个用户下线之后就通知所有用户下线用户的id，并推出循环
					 */
					if("exit".equals(type)){
						if(!uid.equals(str))
							usersList.userLogout(str);
						else{
							socket.shutdownInput();
							socket.shutdownOutput();
							socket.close();
							break;
						}
					}
					/*
					 * 如果用户下线时聊天窗口未关闭，则通知正在聊天的用户
					 */
					if("senderror".equals(type)){
						usersList.getOpenedWindow(str).showWarning();
					}
					if("directExit".equals(type)){
						socket.shutdownInput();
						socket.shutdownOutput();
						socket.close();
						break;
					}
				}
			}
			System.exit(0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
