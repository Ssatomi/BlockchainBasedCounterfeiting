package application;

import javafx.event.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;
import javafx.scene.control.PasswordField;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class CtrlLogin implements Initializable{
	@FXML
	private TextField txt_account;
	@FXML
	private PasswordField txt_pswd;
	
	//初始化函数
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		txt_account.setText("producer client");
	}
	
	//登录按钮单击触发函数
    @FXML
    private void btn_login_clicked(ActionEvent event)
    {
    	String account,pswd;
    	account = txt_account.getText();
    	pswd = txt_pswd.getText();
    	if(account.equals(""))
    	{
     		JOptionPane.showMessageDialog(new JFrame().getContentPane(), 
	         	"请先填写账号！", "警告", JOptionPane.WARNING_MESSAGE);
     		return;
    	}
    	if(pswd.equals(""))
    	{
     		JOptionPane.showMessageDialog(new JFrame().getContentPane(), 
    	         "请先填写密码！", "警告", JOptionPane.WARNING_MESSAGE);
         	return;
    	}
    	if(account.equals("producer client")&&pswd.equals("666"))
    	{
    		Main.setClientScene();
    	}
    	else
    	{
     		JOptionPane.showMessageDialog(new JFrame().getContentPane(), 
    	         "账号或密码错误！", "警告", JOptionPane.WARNING_MESSAGE);
         	return;
    	}
    }

}
