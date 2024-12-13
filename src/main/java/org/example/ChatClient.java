package org.example;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class ChatClient {

    // Variáveis relacionadas com a interface gráfica --- * NÃO MODIFICAR *
    JFrame frame = new JFrame("Chat Client");
    private JTextField chatBox = new JTextField();
    private JTextArea chatArea = new JTextArea();
    // --- Fim das variáveis relacionadas coma interface gráfica

    // Se for necessário adicionar variáveis ao objecto ChatClient, devem
    // ser colocadas aqui

    // A pre-allocated buffer for the received data
    static private final ByteBuffer buffer = ByteBuffer.allocate( 16384 );

    // Decoder for incoming text -- assume UTF-8
    static private final Charset charset = Charset.forName("UTF8");
      static private final CharsetDecoder decoder = charset.newDecoder();

    private SocketChannel socketChannel;
    private Selector selector;
    private BufferedWriter bufferedWriter;

    // Método a usar para acrescentar uma string à caixa de texto
    // * NÃO MODIFICAR *
    public void printMessage(final String message) {
        chatArea.append(message);
    }

    
    // Construtor
    public ChatClient(String server, int port) throws IOException {

        // Inicialização da interface gráfica --- * NÃO MODIFICAR *
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chatBox);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.setSize(500, 300);
        frame.setVisible(true);
        chatArea.setEditable(false);
        chatBox.setEditable(true);
        chatBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    newMessage(chatBox.getText());
                } catch (IOException ex) {
                } finally {
                    chatBox.setText("");
                }
            }
        });
        frame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                chatBox.requestFocusInWindow();
            }
        });
        // --- Fim da inicialização da interface gráfica

        // Se for necessário adicionar código de inicialização ao
        // construtor, deve ser colocado aqui

        // Inicialização da conexão NIO

        try {
            this.socketChannel = SocketChannel.open();
            this.socketChannel.configureBlocking(false);
            this.socketChannel.connect(new InetSocketAddress(server, port));
           
            this.selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
            closeContact();
        }

    }


    // Método invocado sempre que o utilizador insere uma mensagem
    // na caixa de entrada
    public void newMessage(String message) throws IOException {
        // PREENCHER AQUI com código que envia a mensagem ao servidor
        message = evaluateMessage(message);
        
        if (bufferedWriter != null) {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }

        printMessage(message + "\n");
    }

    private String evaluateMessage(String message){
        String[] arrayMsg = message.split(" ", 2);
    
        if ("/nick".equals(arrayMsg[0])) {
            return message;
        } else if ("/join".equals(arrayMsg[0])) {
            return message;
        } else if ("/leave".equals(arrayMsg[0])) {
            return message;
        } else if ("/bye".equals(arrayMsg[0])) {
            closeContact();  // Fechar a conexão
            System.exit(0);
        } else if ("/priv".equals(arrayMsg[0])) {
            return message;
        } else if (arrayMsg[0].charAt(0) == '/') {
            return "/" + message;
        }
        return message;
    }


    // Método principal do objecto
    public void run() throws IOException {
        // PREENCHER AQUI
        try{
            while (true) {
                selector.select();

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isConnectable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        if (sc.finishConnect()) {
                            // Conexão estabelecida, registre para leitura
                            sc.register(selector, SelectionKey.OP_READ);
                            printMessage("Conectado ao servidor.\n");
                        } else {
                            // Se não conseguiu conectar, fechar
                            key.cancel();
                            closeContact();
                        }
                    } else if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        buffer.clear();
                        int bytesRead = sc.read(buffer);
                        if (bytesRead == -1) {
                            key.cancel();
                            closeContact();
                            break;
                        }
                        buffer.flip();
                        String message = decoder.decode(buffer).toString();
                        printMessage(message + "\n");
                    }
                }
            }
        } catch (IOException ie){
            System.err.println(ie);
        }
    }
    

    private void closeContact() {
        try {
            if (socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
            }
            if (selector != null) {
                selector.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Instancia o ChatClient e arranca-o invocando o seu método run()
    // * NÃO MODIFICAR *
    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
        client.run();
    }

}