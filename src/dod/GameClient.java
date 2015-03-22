package dod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class GameClient{
	
	static Socket sckt;
	static BufferedReader rdr;
	static PrintWriter wrtr;
	static Scanner scn;
	
	public static void main(String[] args) {
		try {
			scn = new Scanner(System.in);
			sckt = new Socket(getAddress(), 49155);
			rdr = new BufferedReader(new InputStreamReader(sckt.getInputStream()));
			wrtr = new PrintWriter(new OutputStreamWriter(sckt.getOutputStream()), true);
			
		} catch (UnknownHostException e) {
			System.err.println("Host not found.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Input/output error connecting to host.");
			System.exit(1);
		}
		
		(new Thread(){
			public void run(){
				while(true){
					try {
						System.out.println(rdr.readLine());
					} catch (IOException e) {
						//Something has gone wrong.
						System.err.println("Error reading from the host.");
						System.exit(1);
					}
				}
			}
		}).start();
		
		
		(new Thread(){
			public void run(){
				
				Scanner scn = new Scanner(System.in);
				
				while(true){
					
					String cmd = scn.nextLine();
					
					//Check for eof
					if(cmd == null){
						System.exit(0); //Exit nicely.
					}
					
					wrtr.println(cmd);
				}
			}
		}).start();
	}
	
	
	private static String getAddress(){
		System.out.println("Enter the address to connect to:");
		String address = scn.nextLine();
		return address;
	}
}