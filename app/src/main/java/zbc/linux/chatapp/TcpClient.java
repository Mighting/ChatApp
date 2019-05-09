package zbc.linux.chatapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient implements Runnable {

    public interface OnMessageReceived{
        public void messageReceived(String message);
    }



    public static final String SERVER_IP = "192.168.0.15";
    public static final int SERVER_PORT = 4444;
    private String mServerMessage;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;

    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    public void sendMessage(String message){
        if(mBufferOut != null && !mBufferOut.checkError()){
            mBufferOut.println(message);
            mBufferOut.flush();
        }

    }

    public void stopClient(){
        sendMessage(Constants.CLOSED_CONNECTION+" bye");

        mRun = false;

        if (mBufferOut != null){
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;

    }

    public void run(){
        mRun = true;

        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.e("TCP Client", "C: Connecting");


                Socket socket = new Socket(serverAddr,SERVER_PORT);


            try {
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));

                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                sendMessage(Constants.LOGIN_NAME+"Kazy");

                while (mRun){
                    mServerMessage = mBufferIn.readLine();

                    if (mServerMessage != null && mMessageListener != null){
                        mMessageListener.messageReceived(mServerMessage);
                    }
                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message '" + mServerMessage + "'");
            }catch (Exception e){
                Log.e("TCP", "S: Error", e);
            }finally {
                socket.close();
            }
        }catch (Exception e){
            Log.e("TCP","S: Error", e);
        }
    }


}
