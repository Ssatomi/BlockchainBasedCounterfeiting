package application.module;

import javafx.beans.property.SimpleStringProperty;

public class TransactionForShow {
	private final SimpleStringProperty id;
	private final SimpleStringProperty public_key;
	private final SimpleStringProperty name;
	private final SimpleStringProperty factory;
	private final SimpleStringProperty time;
	private final SimpleStringProperty size;
	
	public TransactionForShow(String id,String public_key,String name,
			String factory,String time,String size) {
		this.id = new SimpleStringProperty(id);
		this.public_key = new SimpleStringProperty(public_key);
		this.name = new SimpleStringProperty(name);
		this.factory = new SimpleStringProperty(factory);
		this.time = new SimpleStringProperty(time);
		this.size = new SimpleStringProperty(size);
	}
	
	public TransactionForShow(Transaction transaction) {
		this.id = new SimpleStringProperty(transaction.getId());
		this.public_key = new SimpleStringProperty(transaction.getPublicKey());
		this.name = new SimpleStringProperty(transaction.getName());
		this.factory = new SimpleStringProperty(transaction.getFactory());
		this.time = new SimpleStringProperty(transaction.getTime());
		this.size = new SimpleStringProperty(transaction.getSize());
	}
	
	public String getId() {
		return id.get();
	}
	public void setId(String id) {
		this.id.set(id);
	}
	
	public String getPublic_key() {
		return public_key.get();
	}
	public void setPublic_key(String public_key) {
		this.public_key.set(public_key);
	}
	
	public String getName() {
		return name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}
	
	public String getFactory() {
		return factory.get();
	}
	public void setFactory(String factory) {
		this.factory.set(factory);
	}
	
	public String getTime() {
		return time.get();
	}
	public void setTime(String time) {
		this.time.set(time);
	}
	
	public String getSize() {
		return size.get();
	}
	public void setSize(String size) {
		this.size.set(size);
	}
}
