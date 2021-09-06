package application.module;

import java.io.Serializable;

/**
 * 商品信息
 *
 */
public class Transaction implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * 商品信息
	 */
		/**
	 * 商品码
	 */
	private String id;
	/**
	 * 商品IC卡公钥
	 */
	private String public_key;
	/**
	 * 商品名称
	 */
	private String name;
	/**
	 * 商品生产厂商
	 */
	private String factory;
	/**
	 * 商品生产时间
	 */
	private String time;
	/**
	 * 商品尺寸
	 */
	private String size;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPublicKey() {
		return public_key;
	}

	public void setPublicKey(String public_key) {
		this.public_key = public_key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFactory() {
		return factory;
	}

	public void setFactory(String factory) {
		this.factory = factory;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
	
	public void setAll(String id,String public_key,String name,String factory,String time,String size){
		this.id=id;
		this.public_key=public_key;
		this.name=name;
		this.factory=factory;
		this.time=time;
		this.size=size;
	}

}