package store.simplecontrol.weather;
import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.view.ViewGroup;
import android.graphics.Color;
import android.webkit.*;
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
