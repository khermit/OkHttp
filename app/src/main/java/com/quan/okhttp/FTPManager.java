package com.quan.okhttp;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by quandk on 17-12-20.
 */

public class FTPManager {
    public static final String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
    private FTPClient ftpClient = null;
    //构造函数
    public FTPManager(){
        ftpClient = new FTPClient();
    }

    //连接到ftp服务器
    public synchronized boolean connect(String ip, String username, String password) throws IOException {
        boolean bool = false;
        if (ftpClient.isConnected()){ //判断是否已经登录
            ftpClient.disconnect();
        }
        ftpClient.setDataTimeout(20000);//设置连接超时时间
        ftpClient.setControlEncoding("utf-8");
        ftpClient.connect(ip,21);
        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())){
            if (ftpClient.login(username,password)){
                bool = true;
                Log.e("FTPManager", "ftp connected succeed");
            }
        }
        return bool;
    }

    // 实现下载文件功能，可实现断点下载
    public synchronized boolean downloadFile(String localPath, String serverPath)
            throws Exception {
        // 先判断服务器文件是否存在
        FTPFile[] files = ftpClient.listFiles(serverPath);
        if (files.length == 0) {
            System.out.println("服务器文件不存在");
            return false;
        }
        System.out.println("远程文件存在,名字为：" + files[0].getName());
        localPath = localPath + files[0].getName();
        // 接着判断下载的文件是否能断点下载
        long serverSize = files[0].getSize(); // 获取远程文件的长度
//        File localFile = new File(localPath);
//        long localSize = 0;
//        if (localFile.exists()) {
//            localSize = localFile.length(); // 如果本地文件存在，获取本地文件的长度
//            if (localSize >= serverSize) {
//                System.out.println("文件已经下载完了");
//                File file = new File(localPath);
//                file.delete();
//                System.out.println("本地文件存在，删除成功，开始重新下载");
//                localSize = 0;
//                //return false;
//            }
//        }
        // 进度
        long step = serverSize / 100;
        long process = 0;
        long currentSize = 0;
        // 开始准备下载文件
        ftpClient.enterLocalActiveMode(); //设置被动模式
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE); //设置文件传输模式
//        OutputStream out = new FileOutputStream(localFile, true);
//        ftpClient.setRestartOffset(localSize); //设置恢复下载的位置
        ftpClient.setRestartOffset(0);
        InputStream input = ftpClient.retrieveFileStream(serverPath);
        byte[] b = new byte[1024];
        int length = 0;
        while ((length = input.read(b)) != -1) {
//            out.write(b, 0, length);
            currentSize = currentSize + length;
            if (currentSize / step != process) {
                process = currentSize / step;
                if (process % 10 == 0) {
                    System.out.println("下载进度：" + process);
                }
            }
        }
//        out.flush();
//        out.close();
        input.close();
        // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
        if (ftpClient.completePendingCommand()) {
            System.out.println("文件下载成功");
            return true;
        } else {
            System.out.println("文件下载失败");
            return false;
        }
    }

    // 如果ftp上传打开，就关闭掉
    public void closeFTP() throws Exception {
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
        }
    }

    // 创建文件夹
    public boolean createDirectory(String path) throws Exception {
        boolean bool = false;
        String directory = path.substring(0, path.lastIndexOf("/") + 1);
        int start = 0;
        int end = 0;
        if (directory.startsWith("/")) {
            start = 1;
        }
        end = directory.indexOf("/", start);
        while (true) {
            String subDirectory = directory.substring(start, end);
            if (!ftpClient.changeWorkingDirectory(subDirectory)) {
                ftpClient.makeDirectory(subDirectory);
                ftpClient.changeWorkingDirectory(subDirectory);
                bool = true;
            }
            start = end + 1;
            end = directory.indexOf("/", start);
            if (end == -1) {
                break;
            }
        }
        return bool;
    }

}
