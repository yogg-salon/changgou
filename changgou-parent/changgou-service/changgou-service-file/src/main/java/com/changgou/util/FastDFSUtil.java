package com.changgou.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 实现DFS的文件管理
 * 文件上传
 * 文件删除
 * 文件下载
 * 文件信息获取
 * stroge 信息获取
 * Tracker信息获取
 */
public class FastDFSUtil {


    /**
     * 加载tracker 连接信息
     */
    static{
        try {
            //查找classpath文件路径
            //String path = new ClassPathResource("classpath:\\fasfDFS\\fdfs_client.conf").getPath();
            String path ="E:\\IDEA\\changgou\\changgou-parent\\changgou-service\\changgou-service-file\\src\\main\\resources\\fasfDFS\\fdfs_client.conf";
            ClientGlobal.init(path);
            //加载tracker连接信息
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }


    /** 文件上传
     * @param fastDFSFile
     */
    public static String[] upload(FastDFSFile fastDFSFile){
        //附加参数
        NameValuePair[] meta_list =new NameValuePair[1];
        //fastDFSFile.getAuthor()
        meta_list[0] =new NameValuePair("author","123");

        //1加载clientGlobal

        // 创建一个Tracker客户端对象TrackerClient，获取连接信息

        try {

            StorageClient storageClient = getStorageClient();

            /**
             * 通过StorageClient访问Storgae，实现文件上传，并且获取文件上传后的存储信息
             * uploadFile 三个参数
             * 1：上传文件的字节数组
             * 2： 文件的扩展名 jpg
             * 3：附加参数： 比如： 拍摄地址： 拍摄时间
             */

            String[] strings = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);
            return strings;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        return null;

    }
    /**
     * 获取文件信息名
     * @param groupName     文件组名
     * @param remoteFileName  文件的存储路径名
     */

    public static FileInfo getFile(String groupName, String remoteFileName) throws Exception{
        StorageClient storageClient =getStorageClient();

        //获取文件信息
        FileInfo file_info = storageClient.get_file_info(groupName, remoteFileName);
        return file_info;

    }

    /**
     *  文件下载
     * @param groupName
     * @param remoteFileName
     * @return
     * @throws Exception
     */
    public static ByteArrayInputStream downloadFile(String groupName, String remoteFileName) throws Exception{
        StorageClient storageClient = getStorageClient();
        byte[] bytes = storageClient.download_file(groupName, remoteFileName);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 文件下载
     * @param groupName
     * @param remoteFileName
     * @throws Exception
     */
    public static void deleteFile(String groupName, String remoteFileName) throws Exception{
        StorageClient storageClient = getStorageClient();
        storageClient.delete_file(groupName,remoteFileName);
    }

    public static void main(String[] args) throws Exception {
       /* FileInfo fileInfo = getFile("group1", "M00/00/00/wKjThF-ziyaAOdQpAABGQxR-F3U477.jpg");
        System.out.println(fileInfo.getSourceIpAddr());
        System.out.println(fileInfo.getCrc32());
        System.out.println(fileInfo.getFileSize());
        FileOutputStream fileOutputStream=null;
        ByteArrayInputStream file=null;
        try {
            file= downloadFile("group1", "M00/00/00/wKjThF-ziyaAOdQpAABGQxR-F3U477.jpg");
            fileOutputStream=new FileOutputStream("E:/1.jpg");
            byte[] buffer =new byte[1024];
            int i=0;
            while(((i=file.read(buffer))!=-1)){
                fileOutputStream.write(buffer,0,i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(file!=null){
                file.close();
            }
            if(fileOutputStream!=null){
                fileOutputStream.close();
            }
        }*/
//        deleteFile("group1", "M00/00/00/wKjThF-zmPSAb9mdAANpC0-g_Vw697.png");
       /* StorageServer stoage = getStoage();
        System.out.println(stoage.getStorePathIndex());
        System.out.println(stoage.getInetSocketAddress());
        System.out.println(stoage.getSocket());*/
        System.out.println(getTrackerInfo());
    }


    /**
     * 获取Storage信息
     * @return
     * @throws Exception
     */

    public static StorageServer getStoage() throws Exception{
        //创建TrackerClient对象，通过TrackerClient对象访问trackerServer
        TrackerClient trackerClient =new TrackerClient();
        //通过TrackerClient 获取TrackerServer的连接对象
        TrackerServer trackerServer = trackerClient.getConnection();
      return trackerClient.getStoreStorage(trackerServer);
    }

    /**
     * 获取tracker信息
     * @return
     * @throws Exception
     */

    public static String getTrackerInfo() {

        TrackerServer trackerServer=getTrackerServer();
        try {
            int g_tracker_http_port = ClientGlobal.getG_tracker_http_port();
            String ip = trackerServer.getInetSocketAddress().getHostString();
            String url="http://"+ip+":"+g_tracker_http_port;
            return url;
        }catch (Exception e){
            e.printStackTrace();
        }
      return null;
    }

    /**
     * 获取trackerServer
     * @return
     */
    public static  TrackerServer getTrackerServer(){
        TrackerClient trackerClient =new TrackerClient();
        //通过TrackerClient 获取TrackerServer的连接对象
        TrackerServer trackerServer=null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trackerServer;
    }

    /**
     * 获取StorageClient
     * @return
     */
    public static StorageClient getStorageClient(){
        StorageClient storageClient = new StorageClient(getTrackerServer(),null);
        return storageClient;
    }
}
