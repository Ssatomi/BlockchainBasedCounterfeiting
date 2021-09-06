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
	//ClientSocket对象
	ClientSocket clientSocket = new ClientSocket();
	//绑定UI界面上的组件，通过名称的相同
	@FXML
	public DatePicker datepicker;
	@FXML
	private Tab tab_uploading,tab_uploaded;
	@FXML
	private TextField txt_id,txt_publickey,txt_name,txt_factory,txt_size;
	@FXML
	private TableColumn<?, ?> col_id,col_publickey,col_name,col_factory,
	col_time,col_size;
	@FXML
	private TableView<TransactionForShow> table_uploaded;
	//重写初始化函数
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		datepicker.setValue(LocalDate.now());
		//TableColumn绑定数据，根据TableView绑定的类中的变量名
		col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
		col_publickey.setCellValueFactory(new PropertyValueFactory<>("public_key"));
		col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
		col_factory.setCellValueFactory(new PropertyValueFactory<>("factory"));
		col_time.setCellValueFactory(new PropertyValueFactory<>("time"));
		col_size.setCellValueFactory(new PropertyValueFactory<>("size"));
		//初始化clientSocket
		try {
			clientSocket.InitAll(this);
		} catch (URISyntaxException e) {
			System.out.println("clientSocket InitAll failed");
		}
	}
	
	//tab_uploading被选择触发函数
	@FXML
	private void tab_uploading_sel(Event event) {
		System.out.println("tab_uploading selected");
	}
	
	//上传按钮单击触发函数
	@FXML
	private void btn_ok_clicked(ActionEvent event) {
		String id,public_key,name,factory,time,size;
		//获得各组件的数据
		id = txt_id.getText().trim();
		public_key = txt_publickey.getText().trim();
		name = txt_name.getText().trim();
		factory = txt_factory.getText().trim();
		time = datepicker.getValue().toString();
		size = txt_size.getText().trim();
		//所有信息均不能为空
		if(id.isEmpty()||public_key.isEmpty()||name.isEmpty()||
				factory.isEmpty()||size.isEmpty()) {
    		JOptionPane.showMessageDialog(new JFrame().getContentPane(), 
         			"有信息未填！", "警告", JOptionPane.WARNING_MESSAGE);
    		return;
		}
		else {
			//输出，便于测试
			System.out.println("id:"+id);
			System.out.println("public_key:"+public_key);
			System.out.println("name:"+name);
			System.out.println("factory:"+factory);
			System.out.println("time:"+time);
			System.out.println("size:"+size);
			//生成交易(商品)对象和消息对象，发送至producer
			Transaction transaction = new Transaction();
			Message message = new Message();
			transaction.setAll(id, public_key, name, factory, time, size);
			message.setType(BlockConstant.COMMODITY_UPLOAD);
			message.setData(JSON.toJSONString(transaction));
			clientSocket.SendCommodity(JSON.toJSONString(message));
			//提示信息
    		JOptionPane.showMessageDialog(new JFrame().getContentPane(), 
         		"商品信息发送成功！上链需要几秒，请稍候", 
         		"提示", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	//清空按钮单击触发函数
	@FXML
	private void btn_clr_clicked(ActionEvent event) {
		txt_id.clear();
		txt_publickey.clear();
		txt_name.clear();
		txt_factory.clear();
		txt_size.clear();
	}
	
	//注销按钮单击触发函数
	@FXML
	private void btn_logout_clicked(ActionEvent event) {
		Main.setLoginScene();
	}
	
    //退出按钮单击触发函数
    @FXML
    private void btn_exit_clicked(ActionEvent event) {
    	Event.fireEvent(Main.getPrimaryStage(), 
    			new WindowEvent(Main.getPrimaryStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }
	
	//tab_uploaded被选择触发函数
	@FXML
	private void tab_uploaded_sel(Event event) {
		if(tab_uploaded.isSelected()==false) {
			return;
		}
		System.out.println("tab_uploaded selected");
		//向producer请求已上传商品信息
		//页面更新在收到producer回复后进行
		clientSocket.QuerySuccessedCommodities();
	}
	
	public TableView<TransactionForShow> get_table_uploaded() {
		return table_uploaded;
	}
}















