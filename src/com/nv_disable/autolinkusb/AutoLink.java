package com.nv_disable.autolinkusb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AutoLink {
	static String acpiSource, XHCContent = "";
	static short XHC_HS = 0, XHC_SS = 0, lastXHCPort = 0;

	static char slash = File.separatorChar;
	private static final String BASE64_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final char[] BASE64_CHARS = BASE64_STRING.toCharArray();
	
	
	public static void main(String[] args) {
		

		
		
		
		if (args.length != 3) {
		System.out.println("AutoLink USB usage example: java -jar AutoLinkUSB.jar MacBookPro15,2 /path_to_disassembled_dsdt/DSDT.dsl /output_path/for_kext/generation/"
				+ "\nIf the creation of the kext is successful,  then it will appear in output folder as \"USBLink.kext\"");
		System.exit(1);
	}
		System.out.println("AutoLink USB is running on "+System.getProperty("os.name") + " platform.");
		

		try {
			acpiSource = Files.readAllLines(Paths.get(args[1])).toString();
		} catch (IOException e) {

			e.printStackTrace();
		}
		System.out.println("Checking USB 3.* XHC Ports...\n");

		if (acpiSource.contains("Device (HS01")) {
			while (true) {
				if (!acpiSource.contains("HS" + (String.format("%02d", ++XHC_HS)))) {
					break;
				}
			}

			--XHC_HS;
         for (int count = 1; XHC_HS >= count; count++) {
		XHCContent = XHCContent + "\n					<key>HS"+String.format("%02d", count)+"</key>\n" + 
				"					<dict>\n" + 
				"						<key>UsbConnector</key>\n" + 
				"						<integer>3</integer>\n" + 
				"						<key>port</key>\n" + 
				"<data>"+
				base64Value(++lastXHCPort)+
				"</data>"+
				"					</dict>";
         }
        
		}

		if (acpiSource.contains("Device (HSP1")) {
			while (true) {
				if (!acpiSource.contains("HS" + (String.format("%02d", ++XHC_HS)))) {
					break;
				}
			}

			--XHC_HS;
			
			  for (int count = 1; XHC_HS >= count; count++) {
					XHCContent = XHCContent + "\n					<key>HSP"+count+"</key>\n" + 
							"					<dict>\n" + 
							"						<key>UsbConnector</key>\n" + 
							"						<integer>3</integer>\n" + 
							"						<key>port</key>\n" + 
							"<data>"+
							base64Value(++lastXHCPort)+
							"</data>"+
							"					</dict>";
			         }
			  
		}

		if (acpiSource.contains("Device (SS01")) {
			while (true) {
				if (!acpiSource.contains("SS" + (String.format("%02d", ++XHC_SS)))) {
					break;
				}
			}

			--XHC_SS;
			
			for (int count = 1; XHC_SS >= count; count++) {
				XHCContent = XHCContent + "\n					<key>SS"+String.format("%02d", count)+"</key>\n" + 
						"					<dict>\n" + 
						"						<key>UsbConnector</key>\n" + 
						"						<integer>3</integer>\n" + 
						"						<key>port</key>\n" + 
						"<data>"+
						base64Value(++lastXHCPort)+
						"</data>"+
						"					</dict>";
		         }
		}

		if (acpiSource.contains("Device (SSP1")) {
			while (true) {
				if (!acpiSource.contains("SSP" + ++XHC_SS)) {
					break;
				}
			}

			--XHC_SS;
			
			  for (int count = 1; XHC_SS >= count; count++) {
					XHCContent = XHCContent + "\n					<key>SSP"+count+"</key>\n" + 
							"					<dict>\n" + 
							"						<key>UsbConnector</key>\n" + 
							"						<integer>3</integer>\n" + 
							"						<key>port</key>\n" + 
							"<data>"+
							base64Value(++lastXHCPort)+
							"</data>"+
							"					</dict>";
			         }
			  
		}

		
		

		System.out.println("XHC_HS Ports detected: " + XHC_HS);
		System.out.println("XHC_SS Ports detected: " + XHC_SS + "\n");
		
		
		 
		 if (!XHCContent.isEmpty()) {
			XHCContent = "<key>"+args[0]+"-XHC</key>\n" + 
					"		<dict>\n" + 
					"			<key>CFBundleIdentifier</key>\n" + 
					"			<string>com.apple.driver.AppleUSBMergeNub</string>\n" + 
					"			<key>IOClass</key>\n" + 
					"			<string>AppleUSBMergeNub</string>\n" + 
					"			<key>IONameMatch</key>\n" + 
					"			<string>XHC</string>\n" + 
					"			<key>IOProviderClass</key>\n" + 
					"			<string>AppleUSBXHCIPCI</string>\n" + 
					"			<key>IOProviderMergeProperties</key>\n" + 
					"			<dict>\n" + 
					"				<key>kUSBSleepPortCurrentLimit</key>\n" + 
					"				<integer>2100</integer>\n" + 
					"				<key>kUSBSleepPowerSupply</key>\n" + 
					"				<integer>5100</integer>\n" + 
					"				<key>kUSBWakePortCurrentLimit</key>\n" + 
					"				<integer>2100</integer>\n" + 
					"				<key>kUSBWakePowerSupply</key>\n" + 
					"				<integer>5100</integer>\n" + 
					"				<key>port-count</key>\n" + 
					"<data>"+
					base64Value(XHC_HS+XHC_SS)+
					"</data>"+
					"				<key>ports</key>\n" + 
					"				<dict>" + XHCContent + 
					"\n				</dict>\n" + 
							"			</dict>\n" + 
							"			<key>model</key>\n" + 
							"			<string>"+args[0]+"</string>\n" + 
							"		</dict>" ;
		}


		File infoPlistFile = new File(args[2]+slash+"USBLink.kext"+slash+"Contents"+slash+"Info.plist");
		if (!infoPlistFile.exists()) {
			infoPlistFile.getParentFile().mkdirs();
			try {
				infoPlistFile.createNewFile();
				FileWriter writer = new FileWriter(infoPlistFile); 
			      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
			      		"<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" + 
			      		"<plist version=\"1.0\">\n" + 
			      		"<dict>\n" + 
			      		"	<key>CFBundleDevelopmentRegion</key>\n" + 
			      		"	<string>English</string>\n" + 
			      		"	<key>CFBundleIdentifier</key>\n" + 
			      		"	<string>nv_disable.USBLink</string>\n" + 
			      		"	<key>CFBundleInfoDictionaryVersion</key>\n" + 
			      		"	<string>6.0</string>\n" + 
			      		"	<key>CFBundleName</key>\n" + 
			      		"	<string>USBLink</string>\n" + 
			      		"	<key>CFBundlePackageType</key>\n" + 
			      		"	<string>KEXT</string>\n" + 
			      		"	<key>CFBundleShortVersionString</key>\n" + 
			      		"	<string>1.0</string>\n" + 
			      		"	<key>CFBundleSignature</key>\n" + 
			      		"	<string>????</string>\n" + 
			      		"	<key>CFBundleVersion</key>\n" + 
			      		"	<string>1.0</string>\n" + 
			      		"	<key>IOKitPersonalities</key>\n" + 
			      		"	<dict>\n" + XHCContent + "	</dict>\n" + 
			      		"	<key>OSBundleRequired</key>\n" + 
			      		"	<string>Root</string>\n" + 
			      		"</dict>\n" + 
			      		"</plist>\n" 
			      		); 
			      writer.flush();
			      writer.close();
			      
			      System.out.println("Kext was successfully generated. You can find it in your output folder.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		else {
			System.out.println("[ERR]: File \"" + infoPlistFile.getPath() + "\" is already exists. Specify another output folder. Aborting...");
			System.exit(1);
		}
		

	}
	static String base64encode(byte[] bytes) {
		StringBuilder builder = new StringBuilder(((bytes.length + 2) / 3) * 4);
		for (int i = 0; i < bytes.length; i += 3) {
			byte b0 = bytes[i];
			byte b1 = i < bytes.length - 1 ? bytes[i + 1] : 0;
			byte b2 = i < bytes.length - 2 ? bytes[i + 2] : 0;
			builder.append(BASE64_CHARS[(b0 & 0xFF) >> 2]);
			builder.append(BASE64_CHARS[((b0 & 0x03) << 4) | ((b1 & 0xF0) >> 4)]);
			builder.append(i < bytes.length - 1 ? BASE64_CHARS[((b1 & 0x0F) << 2) | ((b2 & 0xC0) >> 6)] : "=");
			builder.append(i < bytes.length - 2 ? BASE64_CHARS[b2 & 0x3F] : "=");
		}
	
		return builder.toString();
	}
	static byte[] bigIntToByteArray(  int i ) {
	    BigInteger bigInt = BigInteger.valueOf(i);      
	    return bigInt.toByteArray();
	}
	static String base64ChangeOrder(String originString) {
		String movingPart = originString.substring(4, 6);
		originString = originString.replace(movingPart, "");
		originString = movingPart + originString;
       // System.out.println(originString);
        return originString;
		
	}
	static String base64Value(int value) {
		byte[] bytes = ByteBuffer.allocate(4).putInt(value).array();
		return base64ChangeOrder(base64encode(bytes));
		
	}
}
