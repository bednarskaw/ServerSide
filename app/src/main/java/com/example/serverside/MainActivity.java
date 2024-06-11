package com.example.serverside;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    ServerSocket serverSocket;
    TextView tvIP, tvPort;
    TextView tvMessages;
    public static String SERVER_IP = "";
    public static final int SERVER_PORT = 8080;
    List<ClientHandler> clients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvIP = findViewById(R.id.tvIP);
        tvPort = findViewById(R.id.tvPort);
        tvMessages = findViewById(R.id.tvMessages);

        try {
            SERVER_IP = getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(new ServerThread());
        thread.start();
    }

    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }

    class ServerThread implements Runnable {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                runOnUiThread(() -> {
                    tvMessages.setText("Not connected\n");
                    tvIP.setText("IP: " + SERVER_IP);
                    tvPort.setText("Port: " + String.valueOf(SERVER_PORT));
                });
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket, MainActivity.this);
                    clients.add(clientHandler);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;
        private Context context;

        ClientHandler(Socket socket, Context context) {
            //SharedPreferencesUtils.clearSharedPreferences(context);
            this.clientSocket = socket;
            this.context = context;
            try {
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                output = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                runOnUiThread(() -> tvMessages.append("Client connected: " + clientSocket.getInetAddress().getHostAddress() + "\n"));

                String message;
                while ((message = input.readLine()) != null) {
                    final String finalMessage = message;
                    if (!"HEARTBEAT".equals(finalMessage)) {
                        runOnUiThread(() -> tvMessages.append("Client: " + finalMessage + "\n"));
                        processClientMessage(finalMessage);
                    }
                    sendMessageToAllClients(message);
                }

                // Client disconnected
                clients.remove(this);
                runOnUiThread(() -> tvMessages.append("Client disconnected: " + clientSocket.getInetAddress().getHostAddress() + "\n"));
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private void processClientMessage(String message) {
            if (message.equals("REQUEST_FILE_LIST")) {
                List<String> titles = SharedPreferencesUtils.getAllTitles(context);
                String fileList = "FILE_LIST " + String.join(",", titles);
                sendMessage(fileList);
            } else if (message.startsWith("REQUEST_FILE_CONTENT")) {
                String title = message.substring("REQUEST_FILE_CONTENT ".length());
                String[] dateAndText = SharedPreferencesUtils.getTextByTitle(context, title);
                if (dateAndText != null && dateAndText[0] != null && dateAndText[1] != null) {
                    String date = dateAndText[0];
                    String text = dateAndText[1];
                    sendMessage("FILE_CONTENT " + title + " " + date + " " + text);
                }
            } else if (message.contains("SAVE_NEW_DOCUMENT")) {
                String[] parts = message.split(" ", 5);
                if (parts.length == 5) {
                    String ip = parts[0];
                    String date = parts[2];
                    String time = parts[3];

                    String dateTime = date + " " + time;
                    String[] titleAndText = parts[4].split("\\|", 2); // Split by '|'
                    String title = titleAndText[0];
                    String text = titleAndText[1];


                    List<String> titles = SharedPreferencesUtils.getAllTitles(context);
                    if (titles.contains(title)) {
                        sendMessage("The document already exists");
                    } else {
                        SharedPreferencesUtils.saveText(context, title, dateTime, text);
                        sendMessage("Document successfully saved");
                    }
                }
            }  else if (message.contains("REMOVE_DOCUMENT")) {
                String[] parts = message.split(" ", 3);
                if (parts.length == 3) {
                    String title = parts[2];
                    List<String> titles = SharedPreferencesUtils.getAllTitles(context);
                    if (titles.contains(title)) {
                        SharedPreferencesUtils.removeTextByTitle(context, title);
                        sendMessage("Document successfully deleted");
                    } else {
                        sendMessage("The document does not exist");
                    }
                }
            }else if (message.contains("CHANGE_DOCUMENT_TITLE")) {
                String[] parts = message.split(" TO ", 2);
                if (parts.length == 2) {
                    String[] titleParts = message.split("CHANGE_DOCUMENT_TITLE ")[1].split(" TO ", 2);
                    String oldTitle = titleParts[0];
                    String newTitle = parts[1];
                    List<String> titles = SharedPreferencesUtils.getAllTitles(context);
                    if (titles.contains(oldTitle)) {
                        String[] dateAndText = SharedPreferencesUtils.getTextByTitle(context, oldTitle);
                        String date = dateAndText[0];
                        String text = dateAndText[1];
                        SharedPreferencesUtils.removeTextByTitle(context, oldTitle);
                        SharedPreferencesUtils.saveText(context, newTitle, date, text);
                        sendMessage("Document title successfully changed");
                    }
                }
            }
            else if (message.contains("SAVE_DOCUMENT")) {

            }
            else {
                runOnUiThread(() -> tvMessages.append("Client: " + message + "\n"));
            }
        }

        void sendMessage(String message) {
            output.println(message);
        }

        void sendMessageToAllClients(String message) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }
}
