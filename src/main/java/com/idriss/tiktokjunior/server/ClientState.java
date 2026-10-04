package com.idriss.tiktokjunior.server;

import com.idriss.tiktokjunior.statement.Statement;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ClientState {
    ByteBuffer readBuffer = ByteBuffer.allocate(8);
    StringBuilder command = new StringBuilder();
    boolean insideString;
    boolean inTransaction;
    List<Statement> pendingStatements = new ArrayList<>();
    Queue<ByteBuffer> pendingWrites =
            new ArrayDeque<>();
}
