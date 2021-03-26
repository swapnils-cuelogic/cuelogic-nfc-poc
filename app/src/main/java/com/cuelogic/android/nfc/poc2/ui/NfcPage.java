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

package com.cuelogic.android.nfc.poc2.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;

import com.cuelogic.android.nfc.R;
import com.cuelogic.android.nfc.comman.LogUtils;
import com.cuelogic.android.nfc.poc2.SPEC;
import com.cuelogic.android.nfc.poc2.MyApplication;
import com.cuelogic.android.nfc.poc2.nfc.bean.Card;
import com.cuelogic.android.nfc.poc2.nfc.reader.ReaderListener;

public final class NfcPage implements ReaderListener {
    private static final String TAG = "READCARD_ACTION";
    private static final String RET = "READCARD_RESULT";
    private static final String STA = "READCARD_STATUS";

    private final Activity activity;

    public NfcPage(Activity activity) {
        this.activity = activity;
    }

    public static boolean isSendByMe(Intent intent) {
        return intent != null && TAG.equals(intent.getAction());
    }

    public static boolean isNormalInfo(Intent intent) {
        return intent != null && intent.hasExtra(STA);
    }

    public static CharSequence getContent(Activity activity, Intent intent) {
        LogUtils.printLogs(activity, "NfcPage:: getContent");
        String info = intent.getStringExtra(RET);
        if (info == null || info.length() == 0)
            return null;

        return new SpanFormatter(AboutPage.getActionHandler(activity))
                .toSpanned(info);
    }

    @Override
    public void onReadEvent(SPEC.EVENT event, Object... objs) {
        LogUtils.printLogs(activity, "NfcPage:: onReadEvent");
        if (event == SPEC.EVENT.IDLE) {
            showProgressBar();
        } else if (event == SPEC.EVENT.FINISHED) {
            hideProgressBar();

            final Card card;
            if (objs != null && objs.length > 0)
                card = (Card) objs[0];
            else
                card = null;

            activity.setIntent(buildResult(card));
        }
    }

    private Intent buildResult(Card card) {
        LogUtils.printLogs(activity, "NfcPage:: buildResult");
        final Intent ret = new Intent(TAG);

        if (card != null && !card.hasReadingException()) {
            if (card.isUnknownCard()) {
                ret.putExtra(RET, MyApplication
                        .getStringResource(R.string.info_nfc_unknown));
            } else {
                ret.putExtra(RET, card.toHtml());
                ret.putExtra(STA, 1);
            }
        } else {
            ret.putExtra(RET,
                    MyApplication.getStringResource(R.string.info_nfc_error));
        }

        return ret;
    }

    private void showProgressBar() {
        LogUtils.printLogs(activity, "NfcPage:: showProgressBar");
        Dialog d = progressBar;
        if (d == null) {
            d = new Dialog(activity, R.style.progressBar);
            d.setCancelable(false);
            d.setContentView(R.layout.progress);
            progressBar = d;
        }

        if (!d.isShowing())
            d.show();
    }

    private void hideProgressBar() {
        LogUtils.printLogs(activity, "NfcPage:: hideProgressBar");
        final Dialog d = progressBar;
        if (d != null && d.isShowing())
            d.cancel();
    }

    private Dialog progressBar;
}
