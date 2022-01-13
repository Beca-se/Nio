package com.zh.learn.nio;


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author ZhouHu 15756874962@163.com
 */
public class ClientApplication {
    public static String auth(String password) {

        StringBuilder str = new StringBuilder("");
        str.append("*2\r\n").append("$4\r\nAUTH\r\n$").append(password.length()).append("\r\n").append(password).append("\r\n");
        return str.toString();
    }

    public static String test(String password) {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("*1\r\n$").append(password.length()).append("\r\n"
        ).append(password).append("\r\n");
        return stringBuilder.toString();
    }

    public static void main(String[] args) throws Exception {

        SocketChannel socketChannel = SelectorProvider.provider().openSocketChannel();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9468));
        AbstractSelector abstractSelector = SelectorProvider.provider().openSelector();
//        socketChannel.connect(new InetSocketAddress("159.138.106.243", 6379));
        socketChannel.register(abstractSelector, SelectionKey.OP_CONNECT, null);
        while (true) {
            int select = abstractSelector.select(1000);
            if (select > 0) {
                Iterator<SelectionKey> iterator = abstractSelector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey next = iterator.next();
                    iterator.remove();
                    SocketChannel socketChannel1 = (SocketChannel) next.channel();
                    if (next.isConnectable()) {
//                        socketChannel.connect(new InetSocketAddress("192.168.0.82", 6379));
//                        while(!socketChannel1.finishConnect()){
//                            System.out.println("连接失败");
//                        }
                        socketChannel1.finishConnect();
                        socketChannel1.configureBlocking(false);
                        socketChannel1.register(abstractSelector, SelectionKey.OP_READ);
                        socketChannel1.write(ByteBuffer.wrap(auth("Ste123456").getBytes()));
                        System.out.println(socketChannel1.equals(socketChannel));
                    }
                    if (next.isReadable()) {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        int read = socketChannel1.read(byteBuffer);
                        if (read < 0) {
                            System.out.println((socketChannel1).getRemoteAddress().toString() + " === DOWN");
                            socketChannel1.close();
                            continue;
                        }
                        List<Byte> ans = new ArrayList<>();
                        while (read > 0) {
                            // 切换为读操作
                            byteBuffer.flip();
                            byte[] array = byteBuffer.array();
                            for (int j = 0; j < read; j++) {
                                ans.add(array[j]);
                            }
                            byteBuffer.compact();
                            read = socketChannel1.read(byteBuffer);
                        }

                        byte[] arr = new byte[ans.size()];

                        for (int i1 = 0; i1 < ans.size(); i1++) {
                            arr[i1] = ans.get(i1);
                        }
                        System.out.println(new String(arr));
                        Thread.sleep(1500);
                        socketChannel1.write(ByteBuffer.wrap((test("DSFsdfsd")).getBytes()));
                    }
                }
            }
        }
    }
}
