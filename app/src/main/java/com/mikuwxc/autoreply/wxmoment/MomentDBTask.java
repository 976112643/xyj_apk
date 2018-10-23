package com.mikuwxc.autoreply.wxmoment;
import android.os.AsyncTask;
import com.mikuwxc.autoreply.R;
import com.mikuwxc.autoreply.common.MyApp;
import com.mikuwxc.autoreply.common.util.ToastUtil;
import com.mikuwxc.autoreply.wxmoment.common.Share;

public class MomentDBTask {

    static Task  task = null;
    static SnsStat snsStat = null;

    public static void run(){
        if(task==null){
            task = new Task(MyApp.getAppContext());
        }
        task.testRoot();

        new RunningTask().execute();
    }

    static class RunningTask extends AsyncTask<Void, Void, Void> {

        Throwable error = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                task.copySnsDB();
                task.initSnsReader();
                task.snsReader.run();
                snsStat = new SnsStat(task.snsReader.getSnsList());
            } catch (Throwable e) {
                this.error = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voidParam) {
            super.onPostExecute(voidParam);
            if (this.error != null) {
                ToastUtil.showLongToast( R.string.not_rooted);
                return;
            }
            Share.snsData = snsStat;
        }
    }
}
