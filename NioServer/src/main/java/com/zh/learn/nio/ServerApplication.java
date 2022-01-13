package com.zh.learn.nio;


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Nio 的server端
 *
 * @author Zhouhu 15756874962@163.com
 */
public class ServerApplication {
    public static void main(String[] args) throws Exception {

        /**
         * {@link SelectionKey.OP_CONNECT}  只有 {@link SocketChannel) 支持 表示连接就绪
         * {@link SelectionKey.OP_ACCEPT 只有 {@link ServerSocketChannel) 表示有客户端连接请求进来
         * {@link SelectionKey.OP_READ {@link SelectionKey.OP_WRITE 关注读写事件
         *
         *
         */
        // 开启一个server socket对象
        ServerSocketChannel socketChannel = SelectorProvider.provider().openServerSocketChannel();
        // 设置非阻塞连接
        socketChannel.configureBlocking(false);
        // 创建 selector对象
        AbstractSelector selector = SelectorProvider.provider().openSelector();
        // 注册监听建立连接事件
        SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_ACCEPT, null);
        // 监听9468端口
        socketChannel.bind(new InetSocketAddress(9468));

        // 循环查询
        while (true) {
            System.out.println("轮询一次");
            int i = selector.select(1000L);
            if (i > 0) {
                // 获取关注事件的集合
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                // 获取事件的key
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    // 迭代遍历事件key
                    SelectionKey activeKey = iterator.next();
                    // 移除避免重复处理
                    iterator.remove();
                    // 获取key中的管道
                    SelectableChannel channel = activeKey.channel();
                    // 如果是请求连接事件
                    if (activeKey.isAcceptable()) {
                        // 请求连接事件一定发生在server socket
                        socketChannel = (ServerSocketChannel) channel;
                        // 接受 建立一个channel
                        SocketChannel client = socketChannel.accept();
                        // 设置非阻塞
                        client.configureBlocking(false);
                        // 将新的通道注册到selector,并监听读事件
                        client.register(selector, SelectionKey.OP_READ);

                    }
                    // 如果是读事件
                    if (activeKey.isReadable()) {
                        SocketChannel channel1 = (SocketChannel) channel;
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        int read = -1;
                        try {
                            read = channel1.read(byteBuffer);
                        } catch (Exception ex) {
                            System.out.println("DOWN");
                        }
                        List<Byte> ans = new ArrayList<>();
                        while (read > 0) {
                            // 切换为读操作
                            byteBuffer.flip();
                            byte[] array = byteBuffer.array();
                            for (int j = 0; j < read; j++) {
                                ans.add(array[j]);
                            }
                            // 切换为写操作
                            byteBuffer.compact();
                            read = channel1.read(byteBuffer);
                        }
                        // 如果read小于0,说明被通道被关闭了
                        if (read < 0) {
                            System.out.println(((SocketChannel) channel).getRemoteAddress().toString() + " === DOWN");
                            channel1.close();
                            continue;
                        }
                        byte[] arr = new byte[ans.size()];

                        for (int i1 = 0; i1 < ans.size(); i1++) {
                            arr[i1] = ans.get(i1);
                        }
                        System.out.println(new String(arr));
                        // 发送数据给客户端
                        ((SocketChannel) channel).write(ByteBuffer.wrap(("我是服务器:" + System.currentTimeMillis()).getBytes()));
                    }

                }
            } else {
                System.out.println("没有事件就绪");
            }
        }

    }
}
