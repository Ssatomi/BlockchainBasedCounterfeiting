package application.module;

import java.io.Serializable;

/**
 * 通讯消息
 * 
 */
public class Message implements Serializable {
	
	private static final long serialVersionUID = 1L;
	/**
	 * 消息类型
	 */
	private int type;
	/**
	 * 消息内容
	 */
	private String data;

	public Message() {
	}

	public Message(int type) {
		this.type = type;
	}

	public Message(int type, String data) {
		this.type = type;
		this.data = data;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
