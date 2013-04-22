package yak.dozen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import yak.etc.DH;
import yak.etc.Hash;
import yak.etc.Yak;
import yak.server.AppServer;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

public class Yak12Activity extends Activity {

	static AppCaller appCaller;
	static AppServer server;
	static Thread serverThread;
	
	Context yakContext = this;
	Handler yakHandler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
    	Log.i("yak12", "###### onCreate.");
		super.onCreate(savedInstanceState);

		// Start embedded App Server, if it is not yet started.
		if (serverThread == null) {
			appCaller = new AppCaller(Yak.fmt("http://localhost:%d/?",
					AppServer.DEFAULT_PORT));
			server = new AppServer(AppServer.DEFAULT_PORT);
			serverThread = new Thread(server);
			serverThread.start();
			Yak.sleepSecs(0.333);
		}

		Intent intent = getIntent();
		Uri uri = intent.getData();
		Bundle extras = intent.getExtras();
		String path = uri == null ? "/" : uri.getPath();
		String query = uri == null ? "" : uri.getQuery();

		handleYak12Intent(path, query, extras, savedInstanceState);
	}


	private void handleYak12Intent(String path, String query, Bundle extras,
			Bundle savedInstanceState) {
		try {
			Log.i("antti", "PATH=" + path);
			String[] words = path.split("/");
			// Log.i("antti", "words.LEN=" + words.length);
			String verb = "";
			if (words.length > 1) {
				verb = words[1];
			}

			Log.i("antti", "=============== VERB =" + verb);
//			if (verb.equals("XXXlist")) {
//				String[] labels = extras.getString("items").split(";");
//				displayList(labels);
//			} else if (verb.equals("XXXrendez")) {
//				displayRendezvous(words[2]);
//			} else 
				if (verb.equals("dhdemo")) {
				handleDHDemo();
//			} else if (verb.equals("web")) {
//				displayWeb((String) extras.get("html"));
			} else if (verb.equals("Channel777")) {
				handleChannel777();
//			} else if (verb.equals("Channel0")) {
//				displayChannel0();
			} else {
				handleDefault();
			}
		} catch (Exception e) {
			e.printStackTrace();
			toast(e.toString());
			displayText("handleYak12Intent CAUGHT EXCEPTION:\n\n"
					+ e.toString());
		}
	}

	public void toast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.yak12, menu);
		return true;
	}
	
	///////////////////////////////////////////////////


	private void handleChannel777() throws ClientProtocolException,
			IOException {
		appCaller.handleChannel777();
	}

	private void handleDefault() {
		displayList(new String[] { "One", "Two", "Three", "Channel",
			"Rendezvous", "dhdemo", "Channel777", "Channel0", });
	}
	
	////////////////////////////////////////////////////
	
	private void displayList(String[] labels) {
		AListView v = new AListView(labels);
		setContentView(v);
	}
	
	private void displayText(String s) {
		setContentView(new ATextView(s));
	}

	private void displayWeb(final String html) {
		// handler.post(new Runnable() {
		// @Override
		// public void run() {
		Log.i("displayWeb", "Running TEXT-WEB POST");
		toast("Displaying TEXT-WEB POST: " + html);

		// AWebView v = new AWebView(mainContext, html);
		ATextView v = new ATextView(html);
		setContentView(v);

		
		// }
		// });
	}

//	private void displayChannel0() throws ClientProtocolException, IOException {
//		access.displayChannel0();
//	}

//	public void displayRendezvous(String myId) {
//		AWebView v = new AWebView(mainContext, "");
//		v.loadUrl("file:///android_asset/redez_start.html");
//		setContentView(v);
//	}

	public void handleDHDemo() { // DH DEMO
		DH secA = DH.RandomKey();
		DH secB = DH.RandomKey();
		// Each raises g to the secret key to get the public key.
		DH pubA = secA.publicKey();
		DH pubB = secB.publicKey();
		// A learns pubB; B learns pubA.
		DH mutualA = secA.mutualKey(pubB); // A can compute.
		DH mutualB = secB.mutualKey(pubA); // B can compute.
		// Those mutual keys should be equal.
		BigInteger mutualDiff = mutualA.big.subtract(mutualB.big);

		Hash key = new Hash("mumble");
		String plain = "I wish I were an Oscar Mayer Wiener\000.";
		String encr = key.Encrypt(plain, 31415);
		String recover = key.Decrypt(encr, 31415);

		String html = "<html><body><ul>";
		html += "<li> secA = " + secA;
		html += "<li> secB = " + secB;
		html += "<li> pubA = " + pubA;
		html += "<li> pubB = " + pubB;
		html += "<li> mutualA = " + mutualA;
		html += "<li> mutualB = " + mutualB;
		html += "<li> mutualDiff = " + mutualDiff;
		html += "<li> len(mutual) = " + mutualA.toString().length()
				+ " hex digits";

		html += "<li> plain = " + plain.length() + ": "
				+ Yak.CurlyEncode(plain);
		html += "<li> encr = " + encr.length() + ": " + encr;
		html += "<li> recover = " + recover.length() + ": "
				+ Yak.CurlyEncode(recover);

		AWebView v = new AWebView(html);
		setContentView(v);
	}

	// Provides access to the App Server.
	public class AppCaller extends Yak {

		String baseUrl;

		public AppCaller(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public void handleChannel777() throws ClientProtocolException,
				IOException {
			getUrlAndDisplay(baseUrl + "f=chan&c=777");
		}

		/** Details of getUrl in bg, and fill in view in UI Thread. */
		public void getUrlAndDisplay(final String url)
				throws ClientProtocolException, IOException {
			final VerticalView vert = new VerticalView();
			vert.addView(new ATextView(Yak.CurlyEncode(url)));
			setContentView(vert);

			new Thread() { // A background thread.
				@Override
				public void run() {
					String html = null;
					try {
						Log.i("getUrlAndDisplay", "<<< bg: " + CurlyEncode(url));
						html = getUrl(url);
					} catch (Exception e) {
						e.printStackTrace();
						html = "getUrlAndDisplay ERROR:<br>"
								+ htmlEscape(e.toString());
					}
					final String finalHtml = html;

					Log.i("getUrlAndDisplay", ">>> html: "
							+ CurlyEncode(finalHtml));
					// runOnUiThread(
					yakHandler.post(new Runnable() {
						@Override
						public void run() {
							Log.i("Posting", CurlyEncode(finalHtml));
							//##// displayWeb(finalHtml);
							vert.addView(new ATextView(finalHtml));
						}
					});
				}
			}.start(); // Start background thread.
			//##// displayText("FETCHING " + url);
		}

		/** Details of client HTTP GET; expecting only 200 or error. */
		private String getUrl(String url) throws ClientProtocolException,
				IOException {
			HttpClient httpclient = new DefaultHttpClient();
			Log.i("getUrl", "< " + url);
			HttpResponse response = httpclient.execute(new HttpGet(url));
			StatusLine statusLine = response.getStatusLine();
			Log.i("getUrl", "> " + statusLine.getStatusCode());
			if (statusLine.getStatusCode() == 200) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				String responseString = out.toString();
				Log.i("getUrl", ">> " + CurlyEncode(responseString));
				return responseString;
			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
				Log.i("getUrl",
						">>BAD>> " + CurlyEncode(statusLine.getReasonPhrase()));
				throw new IOException(statusLine.getReasonPhrase());
			}
		}
	}

	public class AListView extends ListView {

		String[] labels;

		public AListView(final String[] labels) {
			super(yakContext);
			this.labels = labels;

			this.setAdapter(new ArrayAdapter<String>(yakContext,
					R.layout.list_item, labels));

			this.setLayoutParams(FILL);
			this.setTextFilterEnabled(true);

			this.setOnItemClickListener(new ClickListener());
		}

		private class ClickListener implements OnItemClickListener {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long arg3) {
				final String label = labels[index];
				if (label == "Channel") {
					startChannel("555");
				} else if (label == "dhdemo") {
					startDHDemo();
				} else if (label == "Channel777") {
					startChannel777();
				} else if (label == "Channel0") {
					startChannel0();
				} else if (label == "Rendezvous") {
					SecureRandom random = null;
					try {
						random = SecureRandom.getInstance("SHA1PRNG");
					} catch (NoSuchAlgorithmException e) {
						Log.i("antti", e.getMessage());
					}
					int mytempid = random.nextInt();
					startRendezvous(String.valueOf(mytempid));
				} else {
					String html = "UNKNOWN LABEL {" + label + "}.";
					startWeb(html);
				}
			}
			
		}

		protected void onClick(int index, String label) {
			if (label == "Channel") {
				startChannel("555");
			} else if (label == "dhdemo") {
				startDHDemo();
			} else if (label == "Channel777") {
				startChannel777();
			} else if (label == "Channel0") {
				startChannel0();
			} else if (label == "Rendezvous") {
				SecureRandom random = null;
				try {
					random = SecureRandom.getInstance("SHA1PRNG");
				} catch (NoSuchAlgorithmException e) {
					Log.i("antti", e.getMessage());
				}
				int mytempid = random.nextInt();
				startRendezvous(String.valueOf(mytempid));
			} else {
				String html = "UNKNOWN LABEL {" + label + "}.";
				startWeb(html);
			}
		}
	}

	public class AWebView extends WebView {

		@TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
		public AWebView(String html) {
			super(yakContext);

			this.loadDataWithBaseURL("terse://terse", html, "text/html",
					"UTF-8", null);

			// this.setWebChromeClient(new WebChromeClient());
			this.getSettings().setBuiltInZoomControls(true);
			// this.getSettings().setJavaScriptEnabled(true);
			this.getSettings().setDefaultFontSize(18);
			this.getSettings().setNeedInitialFocus(true);
			this.getSettings().setSupportZoom(true);
			this.getSettings().setSaveFormData(true);

			this.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					return onClickLink(url);
				}
			});
		}

		protected boolean onClickLink(String url) {
			URI uri = URI.create("" + url);
			String path = uri.getPath();
			String query = uri.getQuery();

			startMain(path, query);

			return true;
		}
	}

	public class ATextView extends TextView {
		public ATextView(String text) {
			super(yakContext);
			this.setText(text);
			this.setBackgroundColor(Color.BLACK);
			this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
			this.setTextColor(Color.YELLOW);
		}
	}
	
	public class VerticalView extends LinearLayout {
		public VerticalView() {
			super(yakContext);
			this.setOrientation(LinearLayout.VERTICAL);
		}
	}
	
	// Activity Starters

	void startList(String[] labels) {
		String z = "";
		for (String s : labels) {
			z = z + labels + ";";
		}
		startMain("/list", null, "items", z);
	}

	void startWeb(String html) {
		startMain("/web", null, "html", html);
	}

	void startChannel(String chanKey) {
		startMain("/channel/" + chanKey, null);
	}

	void startRendezvous(String myId) {
		startMain("/rendez/" + myId, null);
	}

	void startDHDemo() {
		startMain("/dhdemo", null);
	}

	void startChannel777() {
		startMain("/Channel777", null);
	}

	void startChannel0() {
		startMain("/Channel0", null);
	}

	void startMain(String actPath, String actQuery, String... extrasKV) {
		Uri uri = new Uri.Builder().scheme("yak12").path(actPath)
				.encodedQuery(actQuery).build();
		Intent intent = new Intent("android.intent.action.MAIN", uri);
		intent.setClass(getApplicationContext(), Yak12Activity.class);
		for (int i = 0; i < extrasKV.length; i += 2) {
			intent.putExtra((String) extrasKV[i], extrasKV[i + 1]);
		}

		startActivity(intent);
	}

	
	//////////////////////////////////////
	// Other Activity Events.

    protected void onStart() {
    	Log.i("yak12", "###### onStart");
    	super.onStart();
    }
    
    protected void onRestart() {
    	Log.i("yak12", "###### onRestart");
    	super.onRestart();
    }

    protected void onResume() {
    	Log.i("yak12", "###### onResume");
    	super.onResume();
    }

    protected void onPause() {
    	Log.i("yak12", "###### onPause");
    	super.onPause();
    }

    protected void onStop() {
    	Log.i("yak12", "###### onStop");
    	super.onStop();
    }

    protected void onDestroy() {
    	Log.i("yak12", "###### onDestroy");
    	super.onDestroy();
    }
    
    ////////////////////////////////
    // Constants.

	LayoutParams FILL = new LayoutParams(LayoutParams.FILL_PARENT,
			LayoutParams.FILL_PARENT);

}
