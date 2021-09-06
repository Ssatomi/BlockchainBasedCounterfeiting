package com.dce.blockchain.web.model;

/**
 * 循环队列
 * 存储商品信息
 */
public class TQueue{
    /**
     * 商品数组
     */
    Transaction[] transactions;
    int front;//前标
    int rear;//后标

    public TQueue(int size){//构造函数
        transactions = new Transaction[size];
        front=0;
        rear=0;
    }
    /**
     * 入队列
     * @param Transaction 商品
     * @return 队列已满返回false，否则返回true
     */
    public boolean enQueue(Transaction Transaction){
        if((rear+1)%transactions.length==front){
            return false;
        }
        transactions[rear]=Transaction;
        rear=(rear+1)%transactions.length;
        return true;
    }
    /**
     * 出队列
     * @return 队列非空则返回一个商品对象，否则返回null
     */
    public Transaction deQueue(){
        if(rear==front){
            return null;
        }
        Transaction Transaction=transactions[front];
        front=(front+1)%transactions.length;
        return Transaction;
    }
}