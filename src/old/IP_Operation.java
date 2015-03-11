import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IP_Operation {
	public static void main(String args[]) {
		
			IP_change();
		
	}

	public static void IP_change()  {
		String ip1;
		try {
			ip1 = InetAddress.getLocalHost().toString().split("/")[1];
			System.out.println("ip1=" + ip1);
			InetAddress test = InetAddress.getByName("140.115.1.254");
			while (ip1
					.equals(InetAddress.getLocalHost().toString().split("/")[1])
					|| !test.isReachable(5000)) {
				try {
					Process p = Runtime.getRuntime().exec("IP_1.bat");
					System.out.println(InetAddress.getLocalHost().toString()
							.split("/")[1]);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Thread.sleep(5000);
			}
			System.out.println("final ip="
					+ InetAddress.getLocalHost().toString().split("/")[1]);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
