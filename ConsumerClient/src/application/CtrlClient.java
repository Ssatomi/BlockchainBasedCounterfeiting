package application;

import javafx.event.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.WindowEvent;

import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.net.URL;
import java.time.LocalDate;
import java.net.URISyntaxException;

import com.alibaba.fastjson.JSON;

import application.module.*;

public class CtrlClient implements Initializable{
	//当前验证过程所处阶段，用于验证按钮触发函数
	//0为第一阶段，1为第二阶段
	private int verifyStatus = 0;
	//ClientSocket对象
	ClientSocket clientSocket = new ClientSocket();
	//绑定UI界面上的组件，通过名称的相同
	@FXML
	private TextField txt_id,txt_publickey,txt_name,txt_factory,txt_time,
		txt_size,txt_orig_text,txt_encr_text;
	//重写初始化函数
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//部分TextField设置为不可输入
		txt_publickey.setEditable(false);
		txt_name.setEditable(false);
		txt_factory.setEditable(false);
		txt_time.setEditable(false);
		txt_size.setEditable(false);
		txt_orig_text.setEditable(false);
		//初始化clientSocket
		try {
			clientSocket.InitAll(this);
		} catch (URISyntaxException e) {
			System.out.println("clientSocket InitAll failed");
		}
	}
	
	//使用说明按钮单击触发函数
	@FXML
	private void btn_doc_clicked(ActionEvent event) {
		JOptionPane.showMessageDialog(new JFrame().getContentPane(), 
			"输入商品码后点击验证，之后根据提示操作。",
     		"说明", JOptionPane.INFORMATION_MESSAGE);
	}
	
	//验证按钮单击触发函数
	@FXML
	private void btn_verify_clicked(ActionEvent event) {
		Message message = new Message();
		//验证处于第一阶段
		if(verifyStatus == 0) {
			//清空其它文本框
			txt_publickey.clear();
			txt_name.clear();
			txt_factory.clear();
			txt_time.clear();
			txt_size.clear();
			txt_orig_text.clear();
			txt_encr_text.clear();
			message.setType(BlockConstant.COMMODITY_VERIFY);
			message.setData(txt_id.getText().trim());
			clientSocket.verifyCommodity(JSON.toJSONString(message));
		}
		//验证处于第二阶段
		else {
			message.setType(BlockConstant.RESPONSE_DIGI_SIG);
			message.setData(txt_encr_text.getText().trim());
			clientSocket.digitalSigSend(JSON.toJSONString(message));
		}
	}
	
	//清空按钮单击触发函数
	@FXML
	private void btn_clr_clicked(ActionEvent event) {
		txt_id.clear();
		txt_publickey.clear();
		txt_name.clear();
		txt_factory.clear();
		txt_time.clear();
		txt_size.clear();
		txt_orig_text.clear();
		txt_encr_text.clear();
	}
	
    //退出按钮单击触发函数
    @FXML
    private void btn_exit_clicked(ActionEvent event) {
    	Event.fireEvent(Main.getPrimaryStage(), 
    			new WindowEvent(Main.getPrimaryStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }
	
	//验证阶段设为1
	public void verifyStatusSetOne() {
		verifyStatus = 1;
	}
	
	//验证阶段设为0
	public void verifyStatusSetZero() {
		verifyStatus =0;
	}
	
	//获得txt_orig_text以便显示信息
	public TextField getTxt_orig_text() {
		return txt_orig_text;
	}
	
	//各文本框显示信息
	public void commodityInfoShow(Transaction transaction) {
		txt_publickey.setText(transaction.getPublicKey());
		txt_name.setText(transaction.getName());
		txt_factory.setText(transaction.getFactory());
		txt_time.setText(transaction.getTime());
		txt_size.setText(transaction.getSize());
	}
}















