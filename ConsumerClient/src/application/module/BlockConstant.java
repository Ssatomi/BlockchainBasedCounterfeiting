package application.module;

/**
 * 常量工具类
 *
 */
public class BlockConstant {

	//初始握手消息
	public final static int INIT_HANDSHAKE = 0;

	//准备完毕消息
	public final static int IS_READY = 1;

	//开始工作
	public final static int WORK_START = 2;

	//商品信息
	public final static int COMMODITY_INFO = 3;

	//新区块生成消息
	public final static int CALCULATED_NEW_BLOCK = 4;

	//仲裁新区块结果
	public final static int DECIDED_NEW_BLOCK = 5;

	//上链确认消息
	public final static int NEW_BLOCK_OK = 6;

	// // 查询最新的区块
	// public final static int QUERY_LATEST_BLOCK = 7;

	// // 返回最新的区块
	// public final static int RESPONSE_LATEST_BLOCK = 8;

	// // 查询整个区块链
	// public final static int QUERY_BLOCKCHAIN = 9;

	// // 返回整个区块链
	// public final static int RESPONSE_BLOCKCHAIN = 10;

	//客户端上传商品信息
	public final static int COMMODITY_UPLOAD = 11;

	//客户端查询已成功商品信息
	public final static int QUERY_SUCCESSED_COMMODITIES = 12;

	//返回已成功商品信息
	public final static int RESPONSE_SUCCESSED_COMMODITIES = 13;

	//消费者客户端请求验证商品信息
	public final static int COMMODITY_VERIFY = 14;

	//producer请求数字签名
	public final static int QUERY_DIGI_SIG = 15;

	//消费者客户端返回数字签名
	public final static int RESPONSE_DIGI_SIG = 16;

	//producer返回最终验证结果
	public final static int COMMODITY_VERIFY_RESULT = 17;

}
