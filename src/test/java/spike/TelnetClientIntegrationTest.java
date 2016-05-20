package spike;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.junit.Test;

import java.io.*;

public class TelnetClientIntegrationTest {

    @Test
    public void connectToLocalhost() throws JSchException, IOException, InterruptedException {

        changeAccelerationAs("0:0:1");

        changeAccelerationAs("0:1:1");
    }

    private void changeAccelerationAs(String values) throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("twer", "127.0.0.1", 22);
        session.setPassword("P@ss123456");
        // It must not be recommended, but if you want to skip host-key check,
        session.setConfig("StrictHostKeyChecking", "no");

        session.connect(3000);

        Channel channel = session.openChannel("shell");

        channel.connect(3000);

        DataInputStream dataIn = new DataInputStream(channel.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(dataIn));
        DataOutputStream dataOut = new DataOutputStream(channel.getOutputStream());

        System.out.println("Starting telnet connection...");
        dataOut.writeBytes("telnet 127.0.0.1 5554\r\n");
        dataOut.writeBytes("sensor set acceleration " + values + "\r\n");

        dataOut.writeBytes("exit\r\n"); //exit from telnet
        dataOut.writeBytes("exit\r\n"); //exit from shell
        dataOut.flush();

        String line = reader.readLine();
        String result = line + "\n";
        while (!(line = reader.readLine()).contains("Connection closed by foreign host")) {
            System.err.println(line);
            result += line + "\n";
        }


        dataIn.close();
        dataOut.close();
        channel.disconnect();
        session.disconnect();
    }
}