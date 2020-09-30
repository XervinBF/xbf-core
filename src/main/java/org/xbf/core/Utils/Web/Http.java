/*
 *
 * MIT License
 *
 * Copyright (c) 2017-2018 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.xbf.core.Utils.Web;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.MapUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by napster on 03.10.17.
 * <p>
 * A Unirest-like wrapper with focus on convenience methods and classes for the
 * OKHttpClient lib
 */
public class Http {

	private static final Logger log = LoggerFactory.getLogger(Http.class);

	private static Proxy proxy = Proxy.NO_PROXY;

	Logger l = LoggerFactory.getLogger(this.getClass());

	// enhance with metrics before using
	public static OkHttpClient buildNewClient() {
		return new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
				.writeTimeout(30, TimeUnit.SECONDS).proxy(proxy).build();
	}

	private OkHttpClient httpClient;

	public Http(OkHttpClient okHttpClient) {
		this.httpClient = okHttpClient;
	}

	// if content type is left null we will assume it is text/plain UTF-8
	public SimpleRequest post(String url, String body, String contentType) {
		shiftProxy();
		MediaType mediaType = contentType != null ? MediaType.parse(contentType) : MediaType.parse("text/plain");
		return new SimpleRequest(new Request.Builder().post(RequestBody.create(mediaType, body)).url(url));
	}

	// post a simple form body made of string string key values
	public SimpleRequest post(String url, Params params) {
		shiftProxy();
		FormBody.Builder body = new FormBody.Builder();
		for (Map.Entry<String, String> param : params.params.entrySet()) {
			body.add(param.getKey(), param.getValue());
		}
		return new SimpleRequest(new Request.Builder().post(body.build()).url(url));
	}

	public SimpleRequest get(String url) {
		shiftProxy();
		if (proxy != null && useProxies)
			l.info("[GET] " + url + " using proxy " + proxy.address().toString());
		return new SimpleRequest(new Request.Builder().get().url(url));
	}

	public SimpleRequest delete(String url) {
		shiftProxy();
		return new SimpleRequest(new Request.Builder().delete().url(url));
	}

	public SimpleRequest get(String url, Params params) {
		shiftProxy();
		return new SimpleRequest(new Request.Builder().get().url(paramUrl(url, params.params).build()));
	}

	private static HttpUrl.Builder paramUrl(String url, Map<String, String> params) {

		// noinspection ConstantConditions
		HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(url).newBuilder();
		for (Map.Entry<String, String> param : params.entrySet()) {
			httpUrlBuilder.addQueryParameter(param.getKey(), param.getValue());
		}
		return httpUrlBuilder;
	}

	public Http withProxies() {
		useProxies = true;
		return this;
	}

	private void withProxy(Proxy proxy2) {
		proxy = proxy2;
		this.httpClient = Http.buildNewClient();
	}

	long last;
	int cprox;

	private void shiftProxy() {
		if (!useProxies)
			return;
		if (!(System.currentTimeMillis() - last < 4000)) {
			last = System.currentTimeMillis();
			proxy = getProxy();
			this.httpClient = Http.buildNewClient();
		}
	}

	public Proxy[] proxies = new Proxy[0];
	boolean useProxies = false;

//    private Proxy[] loadProxies() {
//		if(proxies != null) return proxies;
//		ArrayList<Proxy> pr = new ArrayList<Proxy>();
//		try {
//			String[] res = Jsoup.parse(new URL("https://www.proxy-list.download/api/v1/get?type=http"), 1000).text().split(" ");
//			for (String string : res) {
//				String[] a = string.split(":");
//				pr.add(new Proxy(Type.HTTP, new InetSocketAddress(a[0], Integer.parseInt(a[1]))));
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		Collections.shuffle(pr);
//		proxy = pr.get(0);
//		l.info("Loaded " + pr.size() + " proxies");
//		return pr.toArray(new Proxy[0]);
//	}

	/**
	 * A simplified request.
	 */
	public class SimpleRequest {
		private Request.Builder requestBuilder;
		private OkHttpClient httpClient = Http.this.httpClient;

		public SimpleRequest(Request.Builder requestBuilder) {

			this.requestBuilder = requestBuilder;
		}

		public SimpleRequest url(String url, Params params) {

			requestBuilder.url(paramUrl(url, params.params).build());
			return this;
		}

		// set a custom client to execute this request with

		public SimpleRequest client(OkHttpClient httpClient) {
			this.httpClient = httpClient;
			return this;
		}

		// add a header

		public SimpleRequest header(String name, String value) {
			requestBuilder.header(name, value);
			return this;
		}

		// set an authorization header

		public SimpleRequest auth(String value) {
			requestBuilder.header("Authorization", value);
			return this;
		}

		// set a basic authorization header

		public SimpleRequest basicAuth(String user, String pass) {
			requestBuilder.header("Authorization", Credentials.basic(user, pass));
			return this;
		}

		// remember to close the response

		public Response execute() throws IOException {
			Request req = requestBuilder.build();
//            System.out.println(req.method() + " " + req.url().toString() + " " + (req.body() != null ? req.body() : ""));
			log.debug("{} {} {}", req.method(), req.url().toString(), req.body() != null ? req.body() : "");
			return httpClient.newCall(req).execute();
		}

		/**
		 * Enqueue this request with okhttp. This is a non-blocking alternative to
		 * {@link SimpleRequest#execute}, callbacks will be called from okhttp's
		 * internal thread pool.
		 *
		 * @return A Callback enhanced as CompletionStage, that takes care of closing
		 *         the Response and provides convenience transformations of the response
		 *         body to string and json representations.
		 */

		public CompletableCallback enqueue() {
			return enqueue(new CompletableCallback());
		}

		/**
		 * Enqueue this request with okhttp. This is a non-blocking alternative to
		 * {@link SimpleRequest#execute}, callbacks will be called from okhttp's
		 * internal thread pool.
		 * <p>
		 * Make sure to also have a look at {@link SimpleRequest#enqueue()} that makes
		 * use of our own default Callback implementation.
		 *
		 * @param callback success and failure callback
		 * @return the callback that was passed in, for chaining
		 */
		public <T extends Callback> T enqueue(T callback) {
			Request req = requestBuilder.build();
			log.debug("{} {} {}", req.method(), req.url().toString(), req.body() != null ? req.body() : "");
			httpClient.newCall(req).enqueue(callback);
			return callback;
		}

		// give me the content, don't care about error handling

		public String asString() throws IOException {
			try (Response response = this.execute()) {
				// noinspection ConstantConditions
				return response.body().string();
			}
		}

		// give me the content, I don't care about error handling
		// catching JSONExceptions when parsing the returned object is a good idea

		public JSONObject asJson() throws IOException {
			return new JSONObject(asString());
		}
	}

	/**
	 * Fancy wrapper for a string to string map with a factory method
	 */
	public static class Params {
		private Map<String, String> params = new HashMap<>();

		// pass pairs of strings and you'll be fine

		public static Params of(String... pairs) {
			if (pairs.length % 2 == 1) {
				log.warn("Passed an uneven number of args to the Params wrapper, this is a likely bug.");
			}
			Params result = new Params();
			MapUtils.putAll(result.params, pairs);
			return result;
		}
	}

	public static boolean isImage(Response response) {
		String type = response.header("Content-Type");
		return type != null && (type.equals("image/jpeg") || type.equals("image/png") || type.equals("image/gif")
				|| type.equals("image/webp"));
	}

	/**
	 * Callback + CompletableFuture that also takes care of closing the response
	 * after the callback processes it
	 */
	public static class CompletableCallback extends CompletableFuture<Response> implements Callback {

		@Override
		public void onFailure(Call call, IOException e) {
			completeExceptionally(e);
		}

		@Override
		public void onResponse(Call call, Response response) {
			try {
				complete(response);
			} catch (Exception e) {
				completeExceptionally(
						new RuntimeException("Uncaught exception in CompletableCallback success handler", e));
			}
		}

		/**
		 * @return transform the body of the {@link okhttp3.Response} into it's String
		 *         representation
		 */
		public CompletionStage<String> asString() {
			return this.thenApply(response -> {
				try {
					// noinspection ConstantConditions
					return response.body().string();
				} catch (IOException | NullPointerException e) {
					throw new RuntimeException("Failed to get body of response for request to "
							+ response.request().method() + " " + response.request().url());
				}
			});
		}

		/**
		 * @return transform the body of the {@link okhttp3.Response} into it's
		 *         JSONObject representation
		 */
		public CompletionStage<JSONObject> asJson() {
			return asString().thenApply(JSONObject::new);
		}
	}

	public static Http instance;

	public static Http getInstance() {
		if (instance == null)
			instance = getNewInstance();
		return instance;
	}

	public static Http getNewInstance() {
		return new Http(Http.buildNewClient().newBuilder().build());
	}

	public static Proxy getProxy() {
		System.out.println(dynProxies.size());
		if (dynProxies.size() == 0)
			loadNewProxies();
		try {
			Proxy p = dynProxies.get(0);
			dynProxies.remove(0);
			LoggerFactory.getLogger(Http.class).info("Removing proxy " + p.address().toString());
			return p;
		} catch (IndexOutOfBoundsException e) {
			loadNewProxies();
			Proxy p = dynProxies.get(0);
			dynProxies.remove(0);
			return p;
		}
	}

	private static ArrayList<Proxy> dynProxies = new ArrayList<Proxy>();

	static long lastProxyCall = 0;

	public static void loadNewProxies() {
		try {
			if (System.currentTimeMillis() - lastProxyCall < 750)
				return;
			Http http = Http.getNewInstance();
//			String[] str = http.get("http://pubproxy.com/api/proxy?limit=5&format=txt&type=http").asString().split("\n"); // ip:port 
			String[] str = proxyList.replace("\r\n", "\n").split("\n"); // ip:port

			for (String string : str) {
				String[] a = string.split(" ")[0].split(":");
				System.out.println("Adding " + a[0] + ":" + a[1]);
				dynProxies.add(new Proxy(Type.HTTP, new InetSocketAddress(a[0], Integer.parseInt(a[1]))));
			}
			lastProxyCall = System.currentTimeMillis();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static String proxyList = "211.138.121.38:80\r\n" + "88.159.152.98:80\r\n" + "163.158.216.152:80\r\n"
			+ "58.176.46.248:80\r\n" + "218.191.247.51:8380\r\n" + "122.96.59.102:80\r\n" + "122.96.59.102:81\r\n"
			+ "220.136.198.198:8080\r\n" + "202.171.253.72:80\r\n" + "122.96.59.99:81\r\n" + "113.252.236.96:8080\r\n"
			+ "122.96.59.99:843\r\n" + "122.193.14.106:81\r\n" + "111.7.160.20:80\r\n" + "124.88.67.23:843\r\n"
			+ "168.63.20.19:8134\r\n" + "82.176.161.89:80\r\n" + "122.193.14.102:80\r\n" + "111.1.23.169:80\r\n"
			+ "124.88.67.17:83\r\n" + "122.96.59.107:843\r\n" + "124.88.67.17:80\r\n" + "122.193.14.108:82\r\n"
			+ "124.88.67.54:843\r\n" + "122.96.59.107:83\r\n" + "124.88.67.17:82\r\n" + "118.151.209.114:80\r\n"
			+ "201.241.88.63:80\r\n" + "124.88.67.24:81\r\n" + "122.193.14.108:80\r\n" + "94.209.99.35:80\r\n"
			+ "124.88.67.24:843\r\n" + "137.135.166.225:8132\r\n" + "122.96.59.98:81\r\n" + "168.63.20.19:8119\r\n"
			+ "124.88.67.32:843\r\n" + "178.166.41.225:80\r\n" + "124.88.67.31:843\r\n" + "137.135.166.225:8142\r\n"
			+ "118.141.41.84:80\r\n" + "201.55.46.6:80\r\n" + "80.112.143.63:80\r\n" + "78.21.187.112:80\r\n"
			+ "168.63.24.174:8138\r\n" + "111.1.23.146:80\r\n" + "122.96.59.102:82\r\n" + "168.63.20.19:8132\r\n"
			+ "217.23.90.10:8080\r\n" + "122.96.59.99:83\r\n" + "112.214.73.253:80\r\n" + "122.193.14.106:80\r\n"
			+ "80.112.170.75:80\r\n" + "122.193.14.106:83\r\n" + "46.101.22.124:8118\r\n" + "122.96.59.107:80\r\n"
			+ "137.135.166.225:8134\r\n" + "122.96.59.98:80\r\n" + "122.96.59.107:82\r\n" + "82.139.108.47:80\r\n"
			+ "122.96.59.102:83\r\n" + "84.52.93.202:3128\r\n" + "111.1.23.181:80\r\n" + "122.193.14.106:82\r\n"
			+ "218.205.80.2:81\r\n" + "218.205.80.12:80\r\n" + "122.96.59.106:80\r\n" + "46.101.22.159:8118\r\n"
			+ "181.36.5.138:3128\r\n" + "111.1.23.141:80\r\n" + "168.63.20.19:8127\r\n" + "122.96.59.107:81\r\n"
			+ "124.88.67.23:81\r\n" + "111.1.23.213:8080\r\n" + "185.23.142.89:80\r\n" + "122.193.14.104:83\r\n"
			+ "218.191.247.51:80\r\n" + "210.51.2.203:8089\r\n" + "195.138.173.87:3128\r\n" + "111.1.23.213:80\r\n"
			+ "122.96.59.99:80\r\n" + "137.135.166.225:8144\r\n" + "122.96.59.99:82\r\n" + "124.88.67.20:843\r\n"
			+ "209.249.157.73:8080\r\n" + "218.205.80.13:80\r\n" + "111.1.23.213:8088\r\n" + "137.135.166.225:8139\r\n"
			+ "137.135.166.225:8135\r\n" + "218.205.80.8:80\r\n" + "122.193.14.104:80\r\n" + "46.101.3.43:8118\r\n"
			+ "202.112.50.224:8080\r\n" + "51.254.106.68:80\r\n" + "218.56.132.156:8080\r\n" + "168.63.20.19:8136\r\n"
			+ "122.96.59.106:843\r\n" + "168.63.20.19:8124\r\n" + "124.88.67.31:81\r\n" + "59.72.122.3:1080\r\n"
			+ "115.28.170.44:8099\r\n" + "92.47.195.250:3128\r\n" + "168.63.24.174:8131\r\n" + "188.170.209.50:3128\r\n"
			+ "168.63.20.19:8122\r\n" + "188.113.138.238:3128\r\n" + "92.242.125.177:3128\r\n" + "111.1.23.210:80\r\n"
			+ "46.101.22.147:8118\r\n" + "61.5.207.102:80\r\n" + "193.142.213.18:8080\r\n" + "168.63.24.174:8142\r\n"
			+ "111.7.162.11:8088\r\n" + "59.127.38.117:8080\r\n" + "218.205.80.2:80\r\n" + "178.62.54.134:8118\r\n"
			+ "168.63.24.174:8130\r\n" + "211.143.45.216:3128\r\n" + "124.206.241.221:3128\r\n"
			+ "128.199.92.163:8118\r\n" + "168.63.20.19:8145\r\n" + "115.159.185.186:8088\r\n" + "123.56.74.13:8080\r\n"
			+ "177.101.187.207:8080\r\n" + "187.163.1.36:8080\r\n" + "41.87.164.49:3128\r\n" + "80.1.116.80:80\r\n"
			+ "101.200.143.168:80\r\n" + "14.201.122.140:80\r\n" + "46.101.3.126:8118\r\n" + "199.193.255.160:3128\r\n"
			+ "113.253.13.205:80\r\n" + "58.65.240.196:80\r\n" + "137.135.166.225:8140\r\n" + "137.135.166.225:8147\r\n"
			+ "211.142.195.68:8080\r\n" + "83.174.215.72:3128\r\n" + "178.62.88.50:8118\r\n" + "70.164.255.172:8080\r\n"
			+ "218.56.132.158:8080\r\n" + "123.7.88.171:3128\r\n" + "208.40.165.200:80\r\n" + "101.200.169.110:80\r\n"
			+ "107.170.210.4:9001\r\n" + "212.156.157.82:8080\r\n" + "101.200.141.114:80\r\n" + "178.62.52.60:8118\r\n"
			+ "139.196.140.9:80\r\n" + "92.222.107.183:3128\r\n" + "47.88.195.233:3128\r\n" + "193.150.121.67:4444\r\n"
			+ "45.55.212.127:80\r\n" + "60.194.100.51:80\r\n" + "83.99.192.141:3128\r\n" + "137.74.254.198:3128\r\n"
			+ "111.23.4.138:8080\r\n" + "111.23.4.155:8080\r\n" + "200.26.134.237:8080\r\n" + "178.151.149.227:80\r\n"
			+ "143.202.77.195:8080\r\n" + "178.62.123.240:8118\r\n" + "114.6.130.201:3128\r\n"
			+ "112.140.186.170:808\r\n" + "111.23.4.155:8000\r\n" + "111.23.4.138:80\r\n" + "31.214.144.178:80\r\n"
			+ "111.23.4.155:80\r\n" + "109.86.222.27:3128\r\n" + "115.254.104.205:8080\r\n" + "137.135.166.225:8122\r\n"
			+ "107.189.36.34:3128\r\n" + "137.135.166.225:8131\r\n" + "182.253.193.34:8080\r\n"
			+ "202.53.168.125:8080\r\n" + "92.222.109.70:3128\r\n" + "208.99.185.55:80\r\n" + "92.222.108.109:3128\r\n"
			+ "45.55.40.91:80\r\n" + "46.101.22.223:8118\r\n" + "137.135.166.225:8119\r\n" + "137.135.166.225:8133\r\n"
			+ "111.7.162.30:81\r\n" + "111.23.4.138:8000\r\n" + "119.29.103.13:8888\r\n" + "92.222.107.175:3128\r\n"
			+ "92.222.108.211:3128\r\n" + "81.21.77.189:8083\r\n" + "92.222.109.41:3128\r\n" + "189.85.20.21:8080\r\n"
			+ "177.69.52.192:3128\r\n" + "200.46.86.66:3128\r\n" + "90.152.38.179:1080\r\n" + "84.253.103.183:3128\r\n"
			+ "168.63.20.19:8140\r\n" + "177.73.177.25:8080\r\n" + "114.6.130.203:3128\r\n" + "195.209.107.148:3128\r\n"
			+ "80.87.81.102:3128\r\n" + "88.87.90.155:8080\r\n" + "52.4.122.227:80\r\n" + "222.138.67.50:8089\r\n"
			+ "58.59.68.91:9797\r\n" + "119.254.84.90:80\r\n" + "212.34.54.84:4444\r\n" + "137.135.166.225:8141\r\n"
			+ "180.250.149.73:8080\r\n" + "212.227.159.39:80\r\n" + "90.152.38.178:1080\r\n" + "111.1.23.140:80\r\n"
			+ "139.255.40.130:8080\r\n" + "94.200.231.130:8080\r\n" + "223.25.103.154:3128\r\n"
			+ "92.222.104.218:3128\r\n" + "80.87.81.102:80\r\n" + "200.26.134.235:8080\r\n" + "66.175.83.156:8080\r\n"
			+ "46.101.22.228:8118\r\n" + "91.217.42.4:8080\r\n" + "92.222.107.189:3128\r\n" + "92.222.108.124:3128\r\n"
			+ "46.101.22.109:8118\r\n" + "66.98.34.2:8080\r\n" + "202.56.203.40:80\r\n" + "41.242.90.3:80\r\n"
			+ "92.222.108.108:3128\r\n" + "101.201.70.236:8080\r\n" + "217.29.114.36:3128\r\n"
			+ "122.228.179.178:80\r\n" + "218.248.73.193:808\r\n" + "180.250.182.51:8080\r\n"
			+ "92.222.153.175:3128\r\n" + "178.62.125.124:8118\r\n" + "46.231.88.110:80\r\n" + "186.194.47.186:3128\r\n"
			+ "94.203.96.16:80\r\n" + "184.173.139.10:80\r\n" + "178.238.229.236:80\r\n" + "94.231.116.134:8080\r\n"
			+ "36.81.2.206:31281\r\n" + "149.255.25.167:8080\r\n" + "188.132.226.245:3128\r\n" + "60.250.81.118:80\r\n"
			+ "37.191.41.113:8080\r\n" + "49.231.145.29:8888\r\n" + "109.172.106.3:9999\r\n" + "132.148.26.206:80\r\n"
			+ "97.77.104.22:80\r\n" + "60.199.198.24:8080\r\n" + "200.26.134.234:8080\r\n" + "82.198.197.62:80\r\n"
			+ "204.29.115.149:8080\r\n" + "202.106.16.36:3128\r\n" + "60.97.68.162:8118\r\n" + "217.76.204.197:8080\r\n"
			+ "97.77.104.22:3128\r\n" + "92.222.237.118:8888\r\n" + "92.222.237.98:8888\r\n" + "59.107.26.220:10000\r\n"
			+ "115.112.106.147:8080\r\n" + "162.13.86.16:8080\r\n" + "178.22.148.122:3129\r\n"
			+ "115.112.106.146:8080\r\n" + "27.194.230.14:8888\r\n" + "27.219.26.68:8888\r\n"
			+ "115.112.106.149:8080\r\n" + "93.152.174.107:8118\r\n" + "136.243.22.27:8118\r\n"
			+ "218.29.111.106:9999\r\n" + "187.44.1.54:8080\r\n" + "94.20.21.38:3128\r\n" + "60.21.209.114:8080\r\n"
			+ "104.236.90.3:8118\r\n" + "195.154.7.51:8118\r\n" + "200.85.37.254:80\r\n" + "190.206.212.7:8080\r\n"
			+ "118.97.15.106:8080\r\n" + "5.45.64.97:3128\r\n" + "218.7.156.13:8998\r\n" + "123.30.238.16:3128\r\n"
			+ "190.79.29.181:8080\r\n" + "200.29.191.149:3128\r\n" + "201.248.11.83:8080\r\n"
			+ "190.77.241.102:8080\r\n" + "182.48.113.11:8088\r\n" + "120.90.6.92:8080\r\n" + "196.205.203.100:8080\r\n"
			+ "199.168.100.34:8123\r\n" + "199.116.113.206:8080\r\n" + "5.9.117.85:8118\r\n" + "124.206.133.227:80\r\n"
			+ "115.28.101.22:3128\r\n" + "177.70.23.156:8080\r\n" + "4.31.142.200:8080\r\n" + "119.10.72.34:80\r\n"
			+ "120.25.235.11:8089\r\n" + "82.146.52.210:8118\r\n" + "183.82.56.201:8080\r\n" + "190.206.53.138:8080\r\n"
			+ "216.139.71.163:8118\r\n" + "91.107.18.38:8080\r\n" + "192.25.162.193:80\r\n" + "202.43.190.17:8118\r\n"
			+ "88.183.116.156:8080\r\n" + "115.28.188.178:8888\r\n" + "183.129.178.14:8080\r\n" + "51.254.106.67:80\r\n"
			+ "45.32.23.218:5566\r\n" + "120.52.73.97:87\r\n" + "202.43.190.11:8118\r\n" + "149.56.35.226:80\r\n"
			+ "209.87.244.145:80\r\n" + "211.149.155.136:8118\r\n" + "117.218.101.214:8080\r\n"
			+ "112.17.250.78:8080\r\n" + "120.52.73.97:8081\r\n" + "186.92.134.7:8080\r\n" + "220.225.245.109:8000\r\n"
			+ "5.39.89.204:8118\r\n" + "221.226.82.130:8998\r\n" + "101.69.178.145:7777\r\n" + "149.86.225.105:80\r\n"
			+ "113.7.101.139:8998\r\n" + "120.52.73.97:90\r\n" + "120.76.203.31:80\r\n" + "54.191.23.149:80\r\n"
			+ "120.52.73.97:8086\r\n" + "150.129.179.154:8080\r\n" + "128.199.182.249:443\r\n" + "124.47.6.169:80\r\n"
			+ "36.85.91.95:8080\r\n" + "43.229.84.159:80\r\n" + "178.32.98.2:80\r\n" + "167.114.224.6:80\r\n"
			+ "207.188.73.155:80\r\n" + "178.150.136.59:8080\r\n" + "31.186.25.157:3128\r\n" + "120.52.21.132:8082\r\n"
			+ "128.199.113.157:443\r\n" + "115.236.75.201:80\r\n" + "120.52.73.97:8091\r\n" + "222.169.193.162:8099\r\n"
			+ "120.52.73.97:85\r\n" + "51.254.132.238:80\r\n" + "186.103.193.51:8080\r\n" + "113.10.214.213:3123\r\n"
			+ "181.143.65.117:80\r\n" + "120.76.243.40:80\r\n" + "120.52.73.97:8080\r\n" + "120.52.73.97:8085\r\n"
			+ "89.189.96.24:80\r\n" + "173.68.185.170:80";

}
