package Server;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerThread extends Thread{
	Socket socket=null;
	static HashMap<String, ServerThread> idThreadMap=new HashMap<>();
	static HashMap<String, String> idNameMap=new HashMap<>();
	static ArrayList<String> uidList=new ArrayList<>();
	String uid=null;
	byte[] bytes=new byte[1024];
	boolean firstLogin=true;
	ServerThread(Socket socket,String uid){
		this.socket=socket;
		this.uid=uid;
	}
	@SuppressWarnings("resource")
	public void run(){
		InputStream is=null;
		OutputStream out=null;
		try{
			while(true){
				is=socket.getInputStream();
				int len=0;
				len=is.read(bytes);
				String order=new String(bytes,0,len); 
				String type=order.substring(0,order.indexOf('/'));
				if("login".equals(type)){
					uidList.add(uid);
					String tempName=order.substring(order.indexOf('/')+1);
					System.out.println("用户"+tempName+"已登录");
					idNameMap.put(uid,tempName);
					idThreadMap.put(uid, this);
					updateOnlineUsers(out);
				}
				if("chat".equals(type)){
					char mode=order.charAt(5);
					if(mode=='W'){
						String id=order.substring(order.indexOf('/')+2,order.indexOf(','));
						String chatWithID=order.substring(order.indexOf(',')+1,order.indexOf(';'));
						String chatWithName=order.substring(order.indexOf(';')+1);
						out=idThreadMap.get(id).socket.getOutputStream();
						out.write(("chat/Wfrom"+id+"to"+chatWithID+";"+chatWithName).getBytes());
						out.flush();
					}
					if(mode=='R'){
						String id=order.substring(order.indexOf('/')+2,order.indexOf(','));
						String chatWithID=order.substring(order.indexOf(',')+1,order.indexOf(';'));
						String chatWithName=order.substring(order.indexOf(';')+1,order.indexOf("::"));
						String message=order.substring(order.indexOf("::")+2);
						out=idThreadMap.get(id).socket.getOutputStream();
						out.write(("chat/Rfrom"+id+"to"+chatWithID+";"+chatWithName+"::"+message).getBytes());
						out.flush();
					}
				}
				if("send".equals(type)){
					String senderID=order.substring(order.indexOf("from")+4,order.indexOf("to"));
					String senderName=order.substring(order.indexOf('/')+1,order.indexOf("from"));
					String receiverID=order.substring(order.indexOf("to")+2,order.indexOf("::"));
					String sendMessage=order.substring(order.indexOf("::")+2);
					if(idThreadMap.get(receiverID)==null){
						out=idThreadMap.get(senderID).socket.getOutputStream();
						out.write(("senderror/"+receiverID).getBytes());
						out.flush();
					}
					else{
						out=idThreadMap.get(receiverID).socket.getOutputStream();
						out.write(("send/"+senderID+","+senderName+"::"+sendMessage).getBytes());
						out.flush();
					}
				}
				if("exit".equals(type)){
					String exitID=order.substring(order.indexOf('/')+1);
					uidList.remove(exitID);
					idNameMap.remove(exitID);
					notifyAllUsers(out, exitID);
					out=socket.getOutputStream();
					out.write(("exit/"+exitID).getBytes());
					idThreadMap.get(exitID).socket.shutdownOutput();
					idThreadMap.get(exitID).socket.shutdownInput();
					idThreadMap.remove(exitID);
					break;
				}
				if("directExit".equals(type)){
					out=socket.getOutputStream();
					out.write("directExit/".getBytes());
					out.flush();
					socket.shutdownOutput();
					socket.shutdownInput();
					break;
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	void updateOnlineUsers(OutputStream out){
		try{
			String packagedData=packageData();
			for(String uid:uidList){
				out=idThreadMap.get(uid).socket.getOutputStream();
				out.write(packagedData.getBytes());
				out.flush();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	void notifyAllUsers(OutputStream out,String exitID){
		try{
			for(String uid:uidList){
				out=idThreadMap.get(uid).socket.getOutputStream();
				out.write(("exit/"+exitID).getBytes());
				out.flush();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	String packageData(){
		StringBuffer names=new StringBuffer();
		StringBuffer ids=new StringBuffer();
		StringBuffer users=new StringBuffer();
		int i=0;
		if(firstLogin){
			users.append("login/");
			firstLogin=false;
		}
		else{
			users.append("update/");
		}
		for(String tempID:uidList){
			names.append(idNameMap.get(tempID));
			ids.append(tempID);
			i++;
			if(i!=uidList.size()){
				names.append(",");
				ids.append(',');
			}
		}
		users.append(ids+"|"+names);
		users.append("@"+uid);
		return users.toString();
	}
}
