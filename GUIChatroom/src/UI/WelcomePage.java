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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;



public class WelcomePage extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JButton login=new JButton("登录");
	JLabel hint=new JLabel("(按alt+Enter登录)");
	JLabel label=new JLabel("客户端名称：");
	JTextField text=new JTextField();
	String name;
	Socket socket;
	UsersList usersList;
	public WelcomePage(Socket socket,UsersList usersList){
		super("欢迎页面");
		this.usersList=usersList;
		this.socket=socket;
		setSize(380,280);
		setVisible(true);
		setResizable(false);
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				try {
					OutputStream out=socket.getOutputStream();
					out.write("directExit/".getBytes());
					out.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }
		});
		centered(this);
		setLayout(null);
		hint.setForeground(Color.GRAY);
		label.setBounds(80, 120, 100, 20);
		hint.setBounds(215,200,100,30);
		login.setBounds(150, 200, 60, 30);
		login.setMnemonic(KeyEvent.VK_ENTER); 
		text.setBounds(160,120,120,20);
		add(hint);
		this.add(login);
		add(text);
		add(label);
		login.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				name=text.getText().toString();
				try {
					OutputStream op=socket.getOutputStream();
					String order="login/"+name;
					op.write(order.getBytes());
					op.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setVisible(false);
			}
		});
		validate();
	}
	
	public void centered(Container container) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int w = container.getWidth();
        int h = container.getHeight();
        container.setBounds((screenSize.width - w) / 2,
                (screenSize.height - h) / 2, w, h);
    }
	public String returnName(){
		return name;
	}

	
}
