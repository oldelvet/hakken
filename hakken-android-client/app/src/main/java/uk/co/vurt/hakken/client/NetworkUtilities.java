package uk.co.vurt.hakken.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.vurt.hakken.client.json.JacksonStreamParser;
import uk.co.vurt.hakken.client.json.JobDefinitionHandler;
import uk.co.vurt.hakken.client.json.JsonStreamParser;
import uk.co.vurt.hakken.client.json.TaskDefinitionHandler;
import uk.co.vurt.hakken.domain.JSONUtil;
import uk.co.vurt.hakken.domain.job.Submission;
import uk.co.vurt.hakken.domain.job.SubmissionStatus;
import uk.co.vurt.hakken.security.HashUtils;
import uk.co.vurt.hakken.security.model.LoginResponse;
import uk.co.vurt.hakken.util.StringUtils;
import android.accounts.Account;
import android.content.Context;
import android.net.ParseException;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

final public class NetworkUtilities {

	/** The tag used to log to adb console. **/
	private static final String TAG = "NetworkUtilities";

	/** The Intent extra to store password. **/
	public static final String PARAM_PASSWORD = "password";

	public static final String PARAM_AUTHTOKEN = "authToken";

	/** The Intent extra to store username. **/
	public static final String PARAM_USERNAME = "username";

	public static final String PARAM_UPDATED = "timestamp";

	public static final String USER_AGENT = "TaskHelper/1.0";

	public static final int REQUEST_TIMEOUT_MS = 300 * 1000; // ms

	public static final String AUTH_URI = "/auth/login";

	public static final String FETCH_JOBS_URI = "/jobs/for/[username]/since/[timestamp]?hmac=[hmac]";
	
	//TODO: RP/Kash - DONE - Figure out where this URI needs to go to0
	
	public static final String FETCH_TASK_DEFINITIONS_URI = "/tasks/list";

	/*
	 * The trailing slash after the username is required (due to idosyncrasies
	 * in the Spring MVC implementation of the server)
	 */
	public static final String SUBMIT_JOB_DATA_URI = "/submissions/from/[username]/?hmac=[hmac]";

	private NetworkUtilities() {
	}

	private static String getBaseUrl(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString("sync_server", null);
	}

	private static HttpURLConnection getURLConnection(String urival)
	throws IOException {
		URI uri = URI.create(urival);
		URL url = uri.toURL();
		HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
		urlConnection.setRequestProperty("User-Agent", USER_AGENT);
		urlConnection.setConnectTimeout(REQUEST_TIMEOUT_MS);
		urlConnection.setReadTimeout(REQUEST_TIMEOUT_MS);
		return urlConnection;
	}

	private static void sendPostData(OutputStream os, String stringOutput) throws IOException {
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		try {
			osw.write(stringOutput);
		} finally {
			if (osw != null) {
				osw.close();
			}
			if (os != null) {
				os.close();
			}
		}
	}

	@NonNull
	private static String readStringResponse(HttpURLConnection urlConnection) throws IOException {
		StringBuilder sb = new StringBuilder();
		final int bufferSize = 1024;
		char[] buffer = new char[bufferSize];
		Reader in = new InputStreamReader(urlConnection.getInputStream(), "UTF-8");
		while (true) {
			int rsz = in.read(buffer, 0, buffer.length);
			if (rsz < 0) {
				break;
			}
			sb.append(buffer, 0, rsz);
		}
		return sb.toString();
	}

	/**
	 * Connects to the server, authenticates the provided username and password.
	 * 
	 * @param username
	 *            The user's username
	 * @param password
	 *            The user's password
	 * @param context
	 *            The context of the calling Activity.
	 * @return boolean The boolean result indicating whether the user was
	 *         successfully authenticated.
	 */
	public static LoginResponse authenticate(Context context, String username,
			String password) {

		LoginResponse response = new LoginResponse();
		HttpURLConnection urlConnection = null;
		try {
			String postData = PARAM_USERNAME + "=" + URLEncoder.encode(username)
					+ "&" + PARAM_PASSWORD + "=" + URLEncoder.encode(password);

			urlConnection = getURLConnection(getBaseUrl(context) + AUTH_URI);
			urlConnection.setRequestProperty("Accept", "application/json");
			urlConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");

			urlConnection.setDoOutput(true);
			OutputStream os = urlConnection.getOutputStream();
			sendPostData(os, postData);

			int sc = urlConnection.getResponseCode();
			if (sc == 200) {
				JSONObject loginJson;
				try {
					loginJson = new JSONObject(readStringResponse(urlConnection));

					response.setSuccess(loginJson.getBoolean("success"));
					if (loginJson.has("reason")) {
						response.setReason(loginJson.getString("reason"));
					}
					if (loginJson.has("token")) {
						response.setToken(loginJson.getString("token"));
					}
				} catch (JSONException e) {
					response.setSuccess(false);
					response.setReason(e.getMessage());
					Log.e(TAG, "Unable to parse login response", e);
				}
			}
			if (Log.isLoggable(TAG, Log.INFO)) {
				Log.i(TAG, "Login Response: " + response);
			}
		} catch (final IOException e) {
			if (Log.isLoggable(TAG, Log.INFO)) {
				Log.i(TAG, "IOException when getting authtoken", e);
			}
			response.setReason(e.getMessage());
		} finally {
			if (Log.isLoggable(TAG, Log.VERBOSE)) {
				Log.v(TAG, "getAuthtoken completing");
			}
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
		return response;
	}

	/**
	 * Submit job data back to the server
	 * 
	 * essentially json encoded version of the dataitems submitted as form data.
	 * 
	 * @param account
	 * @param authToken
	 * @return
	 */
	public static SubmissionStatus submitData(Context context, Account account,
			String authToken, Submission submission) {

		SubmissionStatus status = null;

		HttpURLConnection urlConnection = null;
		try {
			String stringOutput = JSONUtil.getInstance().toJson(submission);

			Map<String, String> parameterMap = new HashMap<String, String>();
			parameterMap.put("username", account.name);

			String hmac = HashUtils.hash(parameterMap);
			parameterMap.put("hmac", URLUtils.encode(hmac));

			Log.d(TAG, "username: " + account.name);
			Log.d(TAG, "hmac: " + hmac);

			urlConnection = getURLConnection(StringUtils.replaceTokens(
					getBaseUrl(context) + SUBMIT_JOB_DATA_URI, parameterMap));
			urlConnection.setRequestProperty("Accept", "application/json");
			urlConnection.setRequestProperty("Content-type", "application/json");

			urlConnection.setDoOutput(true);
			OutputStream os = urlConnection.getOutputStream();
			sendPostData(os, stringOutput);

			int sc = urlConnection.getResponseCode();
			// if (httpResponse.getStatusLine().getStatusCode() ==
			// HttpStatus.SC_CREATED) {
			String response = readStringResponse(urlConnection);

			Log.d(TAG, "Response: " + response);
			// } else {
			// Log.w(TAG, "Data submission failed: "
			// + httpResponse.getStatusLine().getStatusCode());
			// }
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Unable to convert submission to JSON", e);
		} catch (IOException e) {
			Log.e(TAG, "Error submitting json", e);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
		return status;

	}

	public static void fetchJobs(Context context, Account account,
			String authToken, Date lastUpdated, JobDefinitionHandler callback)
			throws JSONException, ParseException, IOException {

		SimpleDateFormat dateFormatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss");

		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("username", account.name);
		parameterMap.put("timestamp", dateFormatter.format(lastUpdated));

		String hmac = HashUtils.hash(parameterMap);
		parameterMap.put("hmac", URLUtils.encode(hmac));

		HttpURLConnection urlConnection = null;
		try {
			urlConnection = getURLConnection(StringUtils.replaceTokens(
					getBaseUrl(context) + FETCH_JOBS_URI, parameterMap));

			int sc = urlConnection.getResponseCode();
			JsonStreamParser streamParser = new JacksonStreamParser();
			streamParser.parseJobDefinitionStream(urlConnection.getInputStream(), callback);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
	}

	public static void fetchTaskDefinitions(Context context, Account account,
			String authToken, Date lastUpdated, TaskDefinitionHandler callback) throws JSONException,
			ParseException, IOException {
			
		SimpleDateFormat dateFormatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss");

		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("username", account.name);
		parameterMap.put("timestamp", dateFormatter.format(lastUpdated));

		String hmac = HashUtils.hash(parameterMap);
		parameterMap.put("hmac", URLUtils.encode(hmac));

		HttpURLConnection urlConnection = null;
		try {
			urlConnection = getURLConnection(StringUtils.replaceTokens(
					getBaseUrl(context) + FETCH_TASK_DEFINITIONS_URI, parameterMap));

			int sc = urlConnection.getResponseCode();
			JsonStreamParser streamParser = new JacksonStreamParser();
			streamParser.parseTaskDefinitionStream(urlConnection.getInputStream(), callback);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
	}
}