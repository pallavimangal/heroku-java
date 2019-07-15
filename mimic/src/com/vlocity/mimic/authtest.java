package com.vlocity.mimic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
 
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.json.JSONException;

public class authtest {
	

	    static final String USERNAME     = "*************************";
	    static final String PASSWORD     = "*******************";
	    static final String LOGINURL     = "https://login.salesforce.com";
	    static final String GRANTSERVICE = "/services/oauth2/token?grant_type=password";
	    static final String CLIENTID  = "***********************************";
	    static final String CLIENTSECRET = "***********************************";
	    private static String baseUri;
	    private static String REST_ENDPOINT = "/services/data" ;
	    private static String API_VERSION = "/v32.0" ;
	    private static Header oauthHeader;
	    private static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");
	    private static String accountId ;
	    private static String accountName;
	    
	    public static void main(String[] args) {

	        DefaultHttpClient httpclient = new DefaultHttpClient();

	        // Assemble the login request URL
	        String loginURL = LOGINURL + 
	                          GRANTSERVICE + 
	                          "&client_id=" + CLIENTID + 
	                          "&client_secret=" + CLIENTSECRET +
	                          "&username=" + USERNAME +
	                          "&password=" + PASSWORD;
	        

	        // Login requests must be POSTs
	        HttpPost httpPost = new HttpPost(loginURL);
	        HttpResponse response = null;

	        try {
	            // Execute the login POST request
	            response = httpclient.execute(httpPost);
	        } catch (ClientProtocolException cpException) {
	            cpException.printStackTrace();
	        } catch (IOException ioException) {
	            ioException.printStackTrace();   
	        } 
	        
	     // verify response is HTTP OK
	        final int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode != HttpStatus.SC_OK) {
	            System.out.println("Error authenticating to Force.com: "+statusCode);
	            // Error is in EntityUtils.toString(response.getEntity()) 
	            return;
	        }
	        else
	        	System.out.println("Success: "+statusCode);
	        
	        String getResult = null;
	        try {
	            getResult = EntityUtils.toString(response.getEntity());
	        } catch (IOException ioException) {
	            ioException.printStackTrace();
	        }
	        
	        JSONObject jsonObject = null;
	        String loginAccessToken = null;
	        String loginInstanceUrl = null;
	        
	        try {
	            jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
	            loginAccessToken = jsonObject.getString("access_token");
	            loginInstanceUrl = jsonObject.getString("instance_url");
	        } catch (JSONException jsonException) {
	            jsonException.printStackTrace();
	        }
	        
	        baseUri = loginInstanceUrl + REST_ENDPOINT + API_VERSION ;
	        oauthHeader = new BasicHeader("Authorization", "OAuth " + loginAccessToken) ;
	        System.out.println("oauthHeader1: " + oauthHeader);
	        System.out.println("\n" + response.getStatusLine());
	        System.out.println("Successful login");
	        System.out.println("instance URL: "+loginInstanceUrl);
	        System.out.println("access token/session ID: "+loginAccessToken);
	        System.out.println("baseUri: "+ baseUri); 
	        
	        System.out.println("\n_______________ Account QUERY _______________");
	        try {
	 
	            //Set up the HTTP objects needed to make the request.
	            HttpClient httpClient = HttpClientBuilder.create().build();
	 
	            String uri = baseUri + "/query?q=Select+Id+,+Name+From+Account+Limit+5";
	            System.out.println("Query URL: " + uri);
	            HttpGet httpGet = new HttpGet(uri);
	            System.out.println("oauthHeader2: " + oauthHeader);
	            httpGet.addHeader(oauthHeader);
	            httpGet.addHeader(prettyPrintHeader);
	 
	            // Make the request.
	            HttpResponse response1 = httpClient.execute(httpGet);
	 
	            // Process the result
	            int statusCode1 = response1.getStatusLine().getStatusCode();
	            if (statusCode1 == 200) {
	                String response_string = EntityUtils.toString(response1.getEntity());
	                try {
	                    JSONObject json = new JSONObject(response_string);
	                    System.out.println("JSON result of Query:\n" + json.toString(1));
	                    JSONArray j = json.getJSONArray("records");
	                    for (int i = 0; i < j.length(); i++){
	                        accountId = json.getJSONArray("records").getJSONObject(i).getString("Id");
	                        accountName = json.getJSONArray("records").getJSONObject(i).getString("Name");
	                        System.out.println("Account record is: " + i + ". " + accountId + " " + accountName );
	                    }
	                } catch (JSONException je) {
	                    je.printStackTrace();
	                }
	            } else {
	                System.out.println("Query was unsuccessful. Status code returned is " + statusCode1);
	                System.out.println("An error has occured. Http status: " + response1.getStatusLine().getStatusCode());
	                
	                System.exit(-1);
	            }
	        } catch (IOException ioe) {
	            ioe.printStackTrace();
	        } catch (NullPointerException npe) {
	            npe.printStackTrace();
	        }
	       
	        System.out.println("----------end Query account--------");
	        
	        System.out.println("\n_______________ Account INSERT _______________");
	        
	        String uri = baseUri + "/sobjects/Lead/";
	        try {
	 
	            //create the JSON object containing the new lead details.
	            JSONObject act = new JSONObject();
	            act.put("Name", "test2Pal03092019");
	            
	            System.out.println("JSON for account record to be inserted:\n" + act.toString(1));
	 
	            //Construct the objects needed for the request
	            HttpClient httpClient = HttpClientBuilder.create().build();
	 
	            HttpPost httpPost1 = new HttpPost(uri);
	            httpPost1.addHeader(oauthHeader);
	            httpPost1.addHeader(prettyPrintHeader);
	            // The message we are going to post
	            StringEntity body = new StringEntity(act.toString(1));
	            body.setContentType("application/json");
	            httpPost.setEntity(body);
	 
	            //Make the request
	            HttpResponse response2 = httpClient.execute(httpPost);
	 
	            //Process the results
	            int statusCode2 = response2.getStatusLine().getStatusCode();
	            if (statusCode2 == 200) {
	                String response_string = EntityUtils.toString(response2.getEntity());
	                JSONObject json = new JSONObject(response_string);
	                // Store the retrieved lead id to use when we update the lead.
	                String actId = json.getString("id");
	                System.out.println("New Account id from response: " + response_string+"\n\n");
	                System.out.println("New Account id from response: " + actId);
	            } else {
	                System.out.println("Insertion unsuccessful. Status code returned is " + statusCode2);
	            }
	        } catch (JSONException e) {
	            System.out.println("Issue creating JSON or processing results");
	            e.printStackTrace();
	        } catch (IOException ioe) {
	            ioe.printStackTrace();
	        } catch (NullPointerException npe) {
	            npe.printStackTrace();
	        }
	        
	        System.out.println("----------end account--------");
	        
	        httpPost.releaseConnection();
	    }
	}
