package entity;

/**
 * @author yanming
 * @version 1.0 2021/1/18
 */
public class SystemConstants {
    //防止订单重复订购
    public static final String  USER_QUEUE_COUNT="UserQueueCount";
    /**
     * 秒杀订单队列
     */
    public static final String SEC_KILL_ORDER_QUEUE="SeckillOrderQueue";
    /**
     * 秒杀订单抢单用户队列
     */
    public static final String USER_QUEUE_STATUS="UserQueueStatus";
    /**
     * 秒杀商品存储到前缀的KEY
     */
    public static final String SEC_KILL_GOODS_PREFIX = "SeckillGoods_";
    /**
     * 商品数据队列存储，防止高并发超卖
     */
    public static final String SEC_KILL_GOODS_COUNT_LIST="SecKillGoodsCountList_";;




    public static final String  SEC_KILL_GOODS_COUNT="SecKillGoodsCount";;




    /**
     * 存储域订单的hash的大key
     */

    public static final String SEC_KILL_ORDER_KEY = "SeckillOrder";

    /**
     * 用户排队的队列的KEY
     */
    public static final String SEC_KILL_USER_QUEUE_KEY = "SeckillOrderQueue";
}