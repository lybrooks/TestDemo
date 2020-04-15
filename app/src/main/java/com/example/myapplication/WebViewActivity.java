package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WebViewActivity extends AppCompatActivity {
   private WebView mWv;
   private Button mBtnJs;
   private TextView mTvData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        mBtnJs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mWv.evaluateJavascript("javascript:AndroidcallJS()", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            mTvData.setText(value);
                        }
                    });
                }
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initData() {

        WebSettings webSettings = mWv.getSettings();
        //js交互
        webSettings.setJavaScriptEnabled(true);
        //设置屏幕自适应
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false);

        mWv.addJavascriptInterface(new AndroidJs(this), "AndroidJs");
        mWv.loadUrl("file:///android_asset/test.html");

//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
//        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
//        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
//        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        mWv.setWebChromeClient(new WebChromeClient());


        // 加载至本页面 不使用自带浏览器打开
        mWv.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    private void initView() {
        mWv = findViewById(R.id.wv);
        mBtnJs = findViewById(R.id.btn_js);
        mTvData =findViewById(R.id.tv_data);
    }



    public class AndroidJs {
        private Context mContext;

        public AndroidJs(Context context) {
            this.mContext = context;
        }

        @JavascriptInterface
        public void showList() {
            new AlertDialog.Builder(mContext)
                    .setTitle("图书列表")
                    .setIcon(R.mipmap.ic_launcher)
                    .setItems(
                            new String[]{"java", "Android",
                                    "java EE"}, null)
                    .setPositiveButton("确定", null).create().show();
        }

        @JavascriptInterface
        public void showToast() {

            Toast.makeText(mContext, "hello", Toast.LENGTH_LONG).show();
        }

    }
}
