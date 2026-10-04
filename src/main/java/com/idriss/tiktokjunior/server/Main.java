package com.idriss.tiktokjunior.server;

import com.idriss.tiktokjunior.execution.Executor;
import com.idriss.tiktokjunior.sql.Parser;
import com.idriss.tiktokjunior.sql.Token;
import com.idriss.tiktokjunior.sql.TokenType;
import com.idriss.tiktokjunior.sql.Tokenizer;
import com.idriss.tiktokjunior.statement.SelectStatement;
import com.idriss.tiktokjunior.statement.Statement;
import com.idriss.tiktokjunior.storage.TableStorage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
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
                            if (c == '\'') {
                                state.insideString = !state.insideString;
                                state.command.append(c);
                            } else if (c == ';' && !state.insideString) {

                                String sql =
                                        state.command.toString().trim();
                                state.command.setLength(0);
                                if (sql.isEmpty()) {
                                    continue;
                                }
                                String response;

                                try {

                                    List<Token> tokens =
                                            tokenizer.tokenize(sql);
                                    TokenType commandType = tokens.get(0).getType();

                                    if (commandType == TokenType.BEGIN
                                            || commandType == TokenType.COMMIT
                                            || commandType == TokenType.ROLLBACK) {
                                        if (tokens.size() != 2
                                                || tokens.get(1).getType() != TokenType.EOF) {
                                            throw new IllegalArgumentException("Expected only " + commandType);
                                        }

                                        if (commandType == TokenType.BEGIN) {
                                            if (state.inTransaction) {
                                                throw new IllegalStateException("Transaction already started");
                                            }
                                            state.inTransaction = true;
                                            response = "Transaction started";
                                        } else if (commandType == TokenType.ROLLBACK) {
                                            if (!state.inTransaction) {
                                                throw new IllegalStateException("No active transaction");
                                            }
                                            state.pendingStatements.clear();
                                            state.inTransaction = false;
                                            response = "Transaction rolled back";
                                        } else {
                                            if (!state.inTransaction) {
                                                throw new IllegalStateException("No active transaction");
                                            }
                                            try {
                                                executor.executeTransaction(state.pendingStatements);
                                                response = "Transaction committed";
                                            } finally {
                                                state.pendingStatements.clear();
                                                state.inTransaction = false;
                                            }
                                        }
                                    } else {
                                        Statement statement = parser.parse(tokens);
                                        if (state.inTransaction) {
                                            if (statement instanceof SelectStatement) {
                                                throw new IllegalStateException(
                                                        "SELECT inside a transaction is not supported yet");
                                            }
                                            state.pendingStatements.add(statement);
                                            response = "Statement queued";
                                        } else {
                                            response = String.valueOf(executor.execute(statement));
                                        }
                                    }
                                } catch (IllegalArgumentException
                                         | IllegalStateException
                                         | IndexOutOfBoundsException
                                         | IOException e) {
                                    response = "ERROR: " + e.getMessage();
                                }

                                state.pendingWrites.add(
                                        ByteBuffer.wrap(
                                                (response + "\n").getBytes(StandardCharsets.UTF_8)
                                        )
                                );
                                selectionKey.interestOps(
                                        selectionKey.interestOps() | SelectionKey.OP_WRITE
                                );

                            } else {
                                state.command.append(c);
                            }
                        }
                        state.readBuffer.clear();
                    }
                } else if (selectionKey.isWritable()) {
                    SocketChannel client = (SocketChannel) selectionKey.channel();
                    ClientState state = (ClientState) selectionKey.attachment();

                    while (!state.pendingWrites.isEmpty()) {
                        ByteBuffer response = state.pendingWrites.peek();
                        client.write(response);

                        if (response.hasRemaining()) {
                            break;
                        }

                        state.pendingWrites.remove();
                    }

                    if (state.pendingWrites.isEmpty()) {
                        selectionKey.interestOps(SelectionKey.OP_READ);
                    }
                }
            }
        }
    }
}
