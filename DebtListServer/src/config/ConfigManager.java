package config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import network.ServerConnection;

import database.DatabaseUnit;

import requests.xml.XMLSerializable;

public class ConfigManager {
	
	public static Config loadConfig(String path) {
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			return (Config) XMLSerializable.toObject(br.readLine());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e) {}
			try {
				fr.close();
			} catch (Exception e) {}
		}
		return null;
	}
	
	public static void saveConfig(Config config, String path) throws IOException {
		FileWriter fw = null;
		try {
			fw = new FileWriter(path);
			fw.write(config.toXML());
		} finally {
			try {
				fw.close();
			} catch (Exception e) {}
		}
	}

	public static final String DB_HOST_NAME = "mysql://localhost";
	public static final int DB_PORT = 3306;
	public static final String DB_NAME = "DebtList";
	public static final String DB_USERNAME = "SENSURED";
	public static final String DB_PASSWORD = "SENSURED";
	
	public static final String SUPPORT_EMAIL = "webmaster.debtlist@gmail.com";
	public static final String SUPPORT_EMAIL_USERNAME = "webmaster.debtlist"; // @gmail.com
	public static final String SUPPORT_EMAIL_PASSWORD = "SENSURED";
	
	
	public static void main(String[] args) {
		Config config = new Config();
		config.setMySQLHostName(DB_HOST_NAME);
		config.setMySQLPort(DB_PORT);
		config.setMySQLDBName(DB_NAME);
		config.setMySQLUsername(DB_USERNAME);
		config.setMySQLPassword(DB_PASSWORD);
		
		config.setSupportEmail(SUPPORT_EMAIL);
		config.setSupportEmailUsername(SUPPORT_EMAIL_USERNAME);
		config.setSupportEmailPassword(SUPPORT_EMAIL_PASSWORD);
		
		try {
			saveConfig(config, ServerConnection.CONFIG_FILE);
			System.out.println("Config file saved.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
