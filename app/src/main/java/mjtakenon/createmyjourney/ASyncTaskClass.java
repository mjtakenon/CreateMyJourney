package mjtakenon.createmyjourney;

import android.os.AsyncTask;

public class ASyncTaskClass extends AsyncTask<String, Void, Integer> {
    String apiUrl;
    private CallBackTask callbacktask;

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Integer doInBackground(String... params) {
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... params) {
    }

    @Override
    protected void onPostExecute(Integer timeSec) {
        super.onPostExecute(timeSec);
        callbacktask.CallBack(timeSec);
        return;
    }

    public void setOnCallBack(CallBackTask _cbj) {
        callbacktask = _cbj;
    }

    //https://qiita.com/oxsoft/items/9ae07c5512449b15b923

    /**
     * コールバック用のstaticなclass
     */
    public static class CallBackTask {
        public void CallBack(Integer time) {
        }
    }
}