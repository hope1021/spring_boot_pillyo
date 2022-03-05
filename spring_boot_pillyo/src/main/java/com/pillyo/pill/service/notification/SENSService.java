package com.pillyo.pill.service.notification;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class SENSService {
	public static void sendSMS(String[] args) {
		String hostNameUrl = "https://sens.apigw.ntruss.com"; // 호스트 URL
		String requestUrl = "/sms/v2/services/"; // 요청 URL
		String requestUrlType = "/messages"; // 요청 URL
		String accessKey = "2EB4FA620F6AE3E1332B";
		String secretKey = "96A6282CDC95EB01E57883FFBDF77C1005519AFF";
		String serviceId = "ncp:sms:kr:274025242511:pillyo_notification";
		String method = "POST";
		String timestamp = Long.toString(System.currentTimeMillis()); // current timestamp (epoch)
		
		requestUrl += serviceId + requestUrlType;
		String apiUrl = hostNameUrl + requestUrl;
		
		// JSON을 활용한 body data 생성
		JSONObject bodyJson = new JSONObject();
		JSONObject toJson = new JSONObject();
		JSONArray toArr = new JSONArray();
		
		// ------ messages ------
//		toJson.put("subject", ""); // 메시지 제목 (LMS에서만 사용 가능)
		toJson.put("content", "pillyo sms test"); // 실제 전송되는 메시지 내용 (SMS 80 byte 제한)
		toJson.put("to", "01097746608"); // 수신번호 목록 (필수, 최대 50개까지 한번에 전송 가능)
		toArr.add(toJson);
		// ------ messages ------
		
		
		bodyJson.put("type", "SMS"); // 메시지 타입 (sms, lms)
//		bodyJson.put("contentType", ""); // 메시지 내용 타입 (COMM: 일반메시지 | AD: 광고메시지 | (default: COMM))
		bodyJson.put("countryCode", "82"); // 국가 번호 (default: 82))
		bodyJson.put("from", "01054673724"); // 발신번호 (사전에 등록된 발신번호만 사용 가능)
//		bodyJson.put("subject", ""); // 기본 메시지 제목 (lms)
		bodyJson.put("content", "pillyo sms test"); // 기본 메시지 내용 (필수) (SMS 80 byte 제한)
		bodyJson.put("messages", toArr); // 메시지 정보 (필수)
		
		
		String body = bodyJson.toJSONString();
		System.out.println(body);
		
		try {
			URL url = new URL(apiUrl);

            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("content-type", "application/json");
            con.setRequestProperty("x-ncp-apigw-timestamp", timestamp);
            con.setRequestProperty("x-ncp-iam-access-key", accessKey);
            con.setRequestProperty("x-ncp-apigw-signature-v2", makeSignature(requestUrl, timestamp, method, accessKey, secretKey));
            con.setRequestMethod(method);
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            
            wr.write(body.getBytes());
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            BufferedReader br;
            System.out.println("responseCode" +" " + responseCode);
            if(responseCode == 202) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else { // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
	            
            System.out.println(response.toString());
        } catch (Exception e) {
            System.out.println(e);
        }
	}
	
	
	// 시그니처 키
	private static String makeSignature(String url, String timestamp, String method, String accessKey, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
	    String space = " ";                    // one space
	    String newLine = "\n";                 // new line
	    

	    String message = new StringBuilder()
	        .append(method)
	        .append(space)
	        .append(url)
	        .append(newLine)
	        .append(timestamp)
	        .append(newLine)
	        .append(accessKey)
	        .toString();

	    SecretKeySpec signingKey;
	    String encodeBase64String;
		try {
			signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
		    encodeBase64String = Base64.getEncoder().encodeToString(rawHmac);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			encodeBase64String = e.toString();
		}
		
	  return encodeBase64String;
	}	
}