/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package com.cuelogic.android.nfc.poc2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cuelogic.android.nfc.R;
import com.cuelogic.android.nfc.comman.LogUtils;
import com.cuelogic.android.nfc.comman.Logger;
import com.cuelogic.android.nfc.poc2.nfc.NfcManager;
import com.cuelogic.android.nfc.poc2.ui.AboutPage;
import com.cuelogic.android.nfc.poc2.ui.MainPage;
import com.cuelogic.android.nfc.poc2.ui.NfcPage;
import com.cuelogic.android.nfc.poc2.ui.Toolbar;
import com.cuelogic.android.nfc.webview.SendInputScreenActivity;

public class Poc2MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poc2_main);
        initViews();
        nfc = new NfcManager(this);
        onNewIntent(getIntent());
    }

    @Override
    public void onBackPressed() {
        if (isCurrentPage(SPEC.PAGE.ABOUT))
            loadDefaultPage();
        else if (safeExit)
            super.onBackPressed();
    }

    @Override
    public void setIntent(Intent intent) {
        if (NfcPage.isSendByMe(intent))
            loadNfcPage(intent);
        else if (AboutPage.isSendByMe(intent))
            loadAboutPage();
        else
            super.setIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfc.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfc.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            if (nfc.updateStatus())
                loadDefaultPage();

            // 有些ROM将关闭系统状态下拉面板的BACK事件发给最顶层窗口
            // 这里加入一个延迟避免意外退出
            board.postDelayed(new Runnable() {
                public void run() {
                    safeExit = true;
                }
            }, 800);
        } else {
            safeExit = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadDefaultPage();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (!nfc.readCard(intent, new NfcPage(this)))
            loadDefaultPage();
    }

    public void onSwitch2DefaultPage(View view) {
        if (!isCurrentPage(SPEC.PAGE.DEFAULT))
            loadDefaultPage();
    }

    public void onSwitch2AboutPage(View view) {
        if (!isCurrentPage(SPEC.PAGE.ABOUT))
            loadAboutPage();
    }

    public void onCopyPageContent(View view) {
        LogUtils.printLogs(Poc2MainActivity.this, "Poc2MainActivity:: onCopyPageContent");
        toolbar.copyPageContent(getFrontPage());
    }

    public void onShareDebugLogs(View view) {
        LogUtils.emailLogs(Poc2MainActivity.this);
    }

    public void onSendInputs(View view) {
        startActivity(new Intent(Poc2MainActivity.this, SendInputScreenActivity.class));
    }

//    public void onSharePageContent(Vie`w view) {
//        LogUtils.printLogs(Poc2MainActivity.this, "Poc2MainActivity:: onSharePageContent");
//        toolbar.sharePageContent(getFrontPage());
//    }

    private void loadDefaultPage() {
        LogUtils.printLogs(Poc2MainActivity.this, "loadDefaultPage:: Default Page");
        toolbar.show(null);

        TextView ta = getBackPage();

        resetTextArea(ta, SPEC.PAGE.DEFAULT, Gravity.CENTER);
        ta.setText(MainPage.getContent(this));

        board.showNext();
    }

    private void loadAboutPage() {
        LogUtils.printLogs(Poc2MainActivity.this, "loadNfcPage:: About Page");
        toolbar.show(R.id.btnBack);

        TextView ta = getBackPage();

        resetTextArea(ta, SPEC.PAGE.ABOUT, Gravity.LEFT);
        ta.setText(AboutPage.getContent(this));

        board.showNext();
    }

    private void loadNfcPage(Intent intent) {
        LogUtils.printLogs(Poc2MainActivity.this, "loadNfcPage:: NFC Page");
        final CharSequence info = NfcPage.getContent(this, intent);

        TextView ta = getBackPage();

        if (NfcPage.isNormalInfo(intent)) {
            toolbar.show(R.id.btnCopy, R.id.btnShare, R.id.btnReset);
            resetTextArea(ta, SPEC.PAGE.INFO, Gravity.LEFT);
        } else {
            toolbar.show(R.id.btnBack);
            resetTextArea(ta, SPEC.PAGE.INFO, Gravity.CENTER);
        }

        LogUtils.printLogs(Poc2MainActivity.this, info.toString());
        ta.setText(info);
        board.showNext();
    }

    private boolean isCurrentPage(SPEC.PAGE which) {
        Object obj = getFrontPage().getTag();

        if (obj == null)
            return which.equals(SPEC.PAGE.DEFAULT);

        return which.equals(obj);
    }

    private void resetTextArea(TextView textArea, SPEC.PAGE type, int gravity) {

        ((View) textArea.getParent()).scrollTo(0, 0);

        textArea.setTag(type);
        textArea.setGravity(gravity);
    }

    private TextView getFrontPage() {
        return (TextView) ((ViewGroup) board.getCurrentView()).getChildAt(0);
    }

    private TextView getBackPage() {
        return (TextView) ((ViewGroup) board.getNextView()).getChildAt(0);
    }

    private void initViews() {
        board = (ViewSwitcher) findViewById(R.id.switcher);

        Typeface tf = MyApplication.getFontResource(R.string.font_oem1);
        TextView tv = (TextView) findViewById(R.id.txtAppName);
        //tv.setTypeface(tf);

        tf = MyApplication.getFontResource(R.string.font_oem2);

        tv = getFrontPage();
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        //tv.setTypeface(tf);

        tv = getBackPage();
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        //tv.setTypeface(tf);

        toolbar = new Toolbar((ViewGroup) findViewById(R.id.toolbar));
    }

    private ViewSwitcher board;
    private Toolbar toolbar;
    private NfcManager nfc;
    private boolean safeExit;
}
