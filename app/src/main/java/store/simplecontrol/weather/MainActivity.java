package store.simplecontrol.weather;
import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.view.ViewGroup;
import android.graphics.Color;
import android.webkit.*;
import java.net.*;
import java.io.*;
import org.json.*;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.webkit.WebViewAssetLoader;
public class MainActivity extends Activity {
 WebView web; GeolocationPermissions.Callback pending; String pendingOrigin;
 @Override public void onCreate(Bundle b){super.onCreate(b);
 FrameLayout root=new FrameLayout(this);root.setBackgroundColor(Color.rgb(34,43,37));
 web=new WebView(this);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,-1);root.addView(web,lp);setContentView(root);
 root.setOnApplyWindowInsetsListener((view,insets)->{int bottom=insets.getInsets(WindowInsets.Type.navigationBars()).bottom;int top=insets.getInsets(WindowInsets.Type.statusBars()).top;FrameLayout.LayoutParams params=(FrameLayout.LayoutParams)web.getLayoutParams();params.bottomMargin=bottom+Math.round(12*getResources().getDisplayMetrics().density);params.topMargin=top;web.setLayoutParams(params);return insets;});
 if(Build.VERSION.SDK_INT>=30)root.requestApplyInsets();
 web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);
 web.getSettings().setGeolocationEnabled(true);
 web.addJavascriptInterface(new Object(){
 @JavascriptInterface public void fetchWarnings(String state){
 final String[][] feeds={{"NSW","IDZ00054.warnings_nsw.xml"},{"ACT","IDZ00054.warnings_nsw.xml"},{"VIC","IDZ00059.warnings_vic.xml"},{"QLD","IDZ00056.warnings_qld.xml"},{"WA","IDZ00060.warnings_wa.xml"},{"SA","IDZ00057.warnings_sa.xml"},{"TAS","IDZ00058.warnings_tas.xml"},{"NT","IDZ00055.warnings_nt.xml"}};
 String file=null;for(String[] pair:feeds)if(pair[0].equals(state))file=pair[1];if(file==null)return;
 final String feed=file;
 new Thread(()->{JSONArray out=new JSONArray();String error="";try{HttpURLConnection c=(HttpURLConnection)new URL("https://www.bom.gov.au/fwo/"+feed).openConnection();c.setConnectTimeout(12000);c.setReadTimeout(12000);c.setRequestProperty("User-Agent","HomesteadWeather/1.0");InputStream in=c.getInputStream();org.xmlpull.v1.XmlPullParser parser=android.util.Xml.newPullParser();parser.setInput(in,"UTF-8");JSONObject item=null;String field=null;int depth=0;for(int event=parser.getEventType();event!=org.xmlpull.v1.XmlPullParser.END_DOCUMENT;event=parser.next()){if(event==org.xmlpull.v1.XmlPullParser.START_TAG){String tag=parser.getName();if("item".equals(tag)){item=new JSONObject();depth=parser.getDepth();}else if(item!=null&&parser.getDepth()==depth+1&&("title".equals(tag)||"link".equals(tag)||"pubDate".equals(tag)))field=tag;}else if(event==org.xmlpull.v1.XmlPullParser.TEXT){if(item!=null&&field!=null)item.put(field,parser.getText());}else if(event==org.xmlpull.v1.XmlPullParser.END_TAG){String tag=parser.getName();if("item".equals(tag)&&item!=null){out.put(item);item=null;if(out.length()>=60)break;}else if(tag.equals(field))field=null;}}in.close();c.disconnect();}catch(Exception ex){error=ex.getMessage();}final String json=out.toString(),err=error;runOnUiThread(()->web.evaluateJavascript("window.receiveWarnings("+JSONObject.quote(state)+","+json+","+JSONObject.quote(err)+")",null));}).start();
 }
 },"HomesteadNative");
 final WebViewAssetLoader loader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
 web.setWebViewClient(new WebViewClient(){@Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){return loader.shouldInterceptRequest(request.getUrl());}});
 web.setWebChromeClient(new WebChromeClient(){
 @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback cb){
 if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) cb.invoke(origin,true,false);
 else {pending=cb;pendingOrigin=origin;requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},12);}
 }});
 web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
 }
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==12&&pending!=null){boolean ok=false;for(int x:g)if(x==PackageManager.PERMISSION_GRANTED)ok=true;pending.invoke(pendingOrigin,ok,false);pending=null;}}
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
}
