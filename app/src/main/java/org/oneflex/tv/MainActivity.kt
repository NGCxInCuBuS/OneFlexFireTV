package org.oneflex.tv

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private lateinit var web: WebView
    private var fullscreen: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        root = FrameLayout(this).apply { setBackgroundColor(Color.rgb(16,24,43)) }
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) { injectNavigation() }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (view == null) return
                    fullscreen = view; fullscreenCallback = callback
                    web.visibility = View.GONE
                    root.addView(view, FrameLayout.LayoutParams(-1,-1))
                }
                override fun onHideCustomView() { closeFullscreen() }
            }
        }
        root.addView(web, FrameLayout.LayoutParams(-1,-1))
        setContentView(root)
        web.loadUrl("https://1flex.org/")
    }
    private fun injectNavigation() {
        web.evaluateJavascript("""
        (function(){
          if(window.__tvInstalled)return;
          window.__tvInstalled=true;
          var style=document.createElement('style');
          style.textContent='.__tvfocus{outline:3px solid #5ad2ff!important;outline-offset:3px!important;}';
          document.head.appendChild(style);
          window.__tvCurrent=null;
          window.__tvItems=function(){return Array.from(document.querySelectorAll('a,button,input,select,[role="button"],[tabindex]')).filter(function(e){var r=e.getBoundingClientRect(),s=getComputedStyle(e);return r.width>0&&r.height>0&&s.display!=='none'&&s.visibility!=='hidden';});};
          window.__tvMove=function(dir){
            var a=window.__tvItems();if(!a.length)return;
            var cur=window.__tvCurrent,rect=cur?cur.getBoundingClientRect():null,best=null,score=Infinity;
            a.forEach(function(e){if(e===cur)return;var r=e.getBoundingClientRect(),dx=r.left+r.width/2-(rect?rect.left+rect.width/2:innerWidth/2),dy=r.top+r.height/2-(rect?rect.top+rect.height/2:innerHeight/2);if(dir==='up'&&dy>=0||dir==='down'&&dy<=0||dir==='left'&&dx>=0||dir==='right'&&dx<=0)return;var p=(dir==='up'||dir==='down')?Math.abs(dy):Math.abs(dx),s=(dir==='up'||dir==='down')?Math.abs(dx):Math.abs(dy),v=p+s*2;if(v<score){score=v;best=e;}});
            if(!best)best=a[0];if(cur)cur.classList.remove('__tvfocus');window.__tvCurrent=best;best.classList.add('__tvfocus');best.focus();best.scrollIntoView({block:'nearest',inline:'nearest'});
          };
          window.__tvSelect=function(){if(window.__tvCurrent)window.__tvCurrent.click();};
        })();
        """.trimIndent(),null)
    }
    override fun onKeyDown(keyCode: Int,event: KeyEvent?): Boolean {
        if(fullscreen!=null)return super.onKeyDown(keyCode,event)
        val dir=when(keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> "up"
            KeyEvent.KEYCODE_DPAD_DOWN -> "down"
            KeyEvent.KEYCODE_DPAD_LEFT -> "left"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "right"
            else -> null
        }
        if(dir!=null){web.evaluateJavascript("window.__tvMove&&window.__tvMove('$dir')",null);return true}
        if(keyCode==KeyEvent.KEYCODE_DPAD_CENTER){web.evaluateJavascript("window.__tvSelect&&window.__tvSelect()",null);return true}
        return super.onKeyDown(keyCode,event)
    }
    private fun closeFullscreen() {
        fullscreen?.let { root.removeView(it) };fullscreen=null
        fullscreenCallback?.onCustomViewHidden();fullscreenCallback=null
        web.visibility=View.VISIBLE
    }
    @Deprecated("Android back navigation")
    override fun onBackPressed() {
        when { fullscreen!=null -> closeFullscreen();web.canGoBack() -> web.goBack();else -> super.onBackPressed() }
    }
    override fun onDestroy(){web.destroy();super.onDestroy()}
}