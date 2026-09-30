package com.idriss.tiktokjunior.server;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class ClientState {
    ByteBuffer readBuffer = ByteBuffer.allocate(8);
    StringBuilder command = new StringBuilder();
    Queue<ByteBuffer> pendingWrites =
            new ArrayDeque<>();
}
