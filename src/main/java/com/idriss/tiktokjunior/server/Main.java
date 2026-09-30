package com.idriss.tiktokjunior.server;

import com.idriss.tiktokjunior.execution.Executor;
import com.idriss.tiktokjunior.sql.Parser;
import com.idriss.tiktokjunior.sql.Token;
import com.idriss.tiktokjunior.sql.Tokenizer;
import com.idriss.tiktokjunior.statement.Statement;
import com.idriss.tiktokjunior.storage.TableStorage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;

public class Main {
    public static void main(String [] args) throws IOException {
        Tokenizer tokenizer = new Tokenizer();
        int PORT = 6389;
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(PORT));
        serverChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        Parser parser = new Parser();
        Executor executor = new Executor(new TableStorage());
        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();


            while(iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if(selectionKey.isAcceptable()){
                    SocketChannel client = serverChannel.accept();
                    client.configureBlocking(false);
                    client.register(
                            selector,
                            SelectionKey.OP_READ,
                            new ClientState()
                    );

                    System.out.println(
                            "Client connected: "
                                    + client.getRemoteAddress()
                    );
                } else if (selectionKey.isReadable()){
                    SocketChannel client = (SocketChannel) selectionKey.channel();

                    ClientState state = (ClientState) selectionKey.attachment();

                    int bytesRead = client.read(state.readBuffer);

                    if (bytesRead > 0) {

                        state.readBuffer.flip();

                        while (state.readBuffer.hasRemaining()) {
                            char c = (char) state.readBuffer.get();
                            if (c == '\n') {

                                String sql =
                                        state.command.toString().trim();

                                List<Token> tokens =
                                        tokenizer.tokenize(sql);

                                Statement statement =
                                        parser.parse(tokens);

                                executor.execute(statement);

                                state.command.setLength(0);
                            } else if(c != '\r') {
                                state.command.append(c);
                            }
                        }
                        state.readBuffer.clear();
                    }
                }
            }
        }
    }
}
