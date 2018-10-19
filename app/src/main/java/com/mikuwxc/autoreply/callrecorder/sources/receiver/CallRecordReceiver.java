package com.mikuwxc.autoreply.callrecorder.sources.receiver;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;


import com.mikuwxc.autoreply.callrecorder.CallDateUtils;
import com.mikuwxc.autoreply.callrecorder.RecordUpload;
import com.mikuwxc.autoreply.callrecorder.sources.CallRecord;
import com.mikuwxc.autoreply.callrecorder.sources.helper.PrefsHelper;
import com.mikuwxc.autoreply.common.util.ToastUtil;
import com.mikuwxc.autoreply.utils.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class CallRecordReceiver extends PhoneCallReceiver {


    private static final String TAG = CallRecordReceiver.class.getSimpleName();

    public static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    public static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    public static final String EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER";

    protected CallRecord callRecord;
    private static MediaRecorder recorder;
    private File audiofile;
    private boolean isRecordStarted = false;

    public CallRecordReceiver(CallRecord callRecord) {
        this.callRecord = callRecord;
    }

    @Override
    protected void onIncomingCallReceived(Context context, String number, Date start) {

    }

    @Override
    protected void onIncomingCallAnswered(Context context, String number, Date start) {
        startRecord(context, "incoming", number);
    }

    @Override
    protected void onIncomingCallEnded(Context context, String number, Date start, Date end) {
        stopRecord(context);
    }

    /**
     * 电话呼出开始回调
     * **/
    @Override
    protected void onOutgoingCallStarted(Context context, String number, Date start) {
        startRecord(context, "outgoing", number);
    }

    /**
     * 电话呼出结束回调
     * **/
    @Override
    protected void onOutgoingCallEnded(Context context, String number, Date start, Date end) {
        stopRecord(context);
    }

    @Override
    protected void onMissedCall(Context context, String number, Date start) {

    }

    // Derived classes could override these to respond to specific events of interest
    protected void onRecordingStarted(Context context, CallRecord callRecord, File audioFile) {
    }

    protected void onRecordingFinished(Context context, CallRecord callRecord, File audioFile) {
//        ToastUtil.showLongToast("完成了"+audioFile.getAbsolutePath());
        try {
            //       录音文件地址 /storage/emulated/0/CallRecorderTest/CallRecorderTestFile_outgoing_10086_1539664048629_727984859.amr
            String absolutePath = audioFile.getAbsolutePath();
            int i = absolutePath.lastIndexOf("/")+1;
            String name=absolutePath.substring(i);
            String[] split = name.split("_");

            int amrDuration = CallDateUtils.getAmrDuration(audioFile);//时长 单位s
            //开始时间戳
            String startTime=split[3];
            //结束时间戳
            String endTime=String.valueOf(Long.valueOf(startTime)+amrDuration*1000);
            //电话号码
            String phoneNum=split[2];
            String type = split[1];
            if("outgoing".equals(type)){//呼出
                type="true";
            }else if("incoming".equals(type)){//呼入
                type="false";
            }else{
                type="undifined";
            }

            String imei = SystemUtil.getIMEI(context);


//            Toast.makeText(context, ""+imei+"..."+type+"..."+amrDuration, Toast.LENGTH_LONG).show();
//            RecordUpload.uploadAmr(name,absolutePath,startTime,endTime,imei,type,phoneNum);
            RecordUpload.handleArm2mp3(name,absolutePath,startTime,endTime,imei,type,phoneNum);



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRecord(Context context, String seed, String phoneNumber) {
        try {
            boolean isSaveFile = PrefsHelper.readPrefBool(context, CallRecord.PREF_SAVE_FILE);
            Log.i(TAG, "isSaveFile: " + isSaveFile);

            // dosya kayıt edilsin mi?
            if (!isSaveFile) {
                return;
            }

            if (isRecordStarted) {
                try {
                    recorder.stop();  // stop the recording
                } catch (RuntimeException e) {
                    // RuntimeException is thrown when stop() is called immediately after start().
                    // In this case the output file is not properly constructed ans should be deleted.
                    Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                    //noinspection ResultOfMethodCallIgnored
                    audiofile.delete();
                }
                releaseMediaRecorder();
                isRecordStarted = false;
            } else {
                if (prepareAudioRecorder(context, seed, phoneNumber)) {
                    recorder.start();
                    isRecordStarted = true;
                    onRecordingStarted(context, callRecord, audiofile);
                    Log.i(TAG, "record start");
                } else {
                    releaseMediaRecorder();
                }
                //new MediaPrepareTask().execute(null, null, null);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
        } catch (RuntimeException e) {
            e.printStackTrace();
            releaseMediaRecorder();
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
        }
    }

    private void stopRecord(Context context) {
        try {
            if (recorder != null && isRecordStarted) {
                releaseMediaRecorder();
                isRecordStarted = false;
                onRecordingFinished(context, callRecord, audiofile);
                Log.i(TAG, "record stop");
            }
        } catch (Exception e) {
            releaseMediaRecorder();
            e.printStackTrace();
        }
    }

    /**
     * 拼接SD卡存放路径
     * **/
    private boolean prepareAudioRecorder(Context context, String seed, String phoneNumber) {
        try {
            String file_name = PrefsHelper.readPrefString(context, CallRecord.PREF_FILE_NAME);//CallRecorderTestFile
            String dir_path = PrefsHelper.readPrefString(context, CallRecord.PREF_DIR_PATH);///storage/emulated/0
            String dir_name = PrefsHelper.readPrefString(context, CallRecord.PREF_DIR_NAME);//CallRecorderTest
            boolean show_seed = PrefsHelper.readPrefBool(context, CallRecord.PREF_SHOW_SEED);
            boolean show_phone_number = PrefsHelper.readPrefBool(context, CallRecord.PREF_SHOW_PHONE_NUMBER);
            int output_format = PrefsHelper.readPrefInt(context, CallRecord.PREF_OUTPUT_FORMAT);
            int audio_source = PrefsHelper.readPrefInt(context, CallRecord.PREF_AUDIO_SOURCE);
            int audio_encoder = PrefsHelper.readPrefInt(context, CallRecord.PREF_AUDIO_ENCODER);

            File sampleDir = new File(dir_path + "/" + dir_name);// /storage/emulated/0/CallRecorderTest

            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }

            StringBuilder fileNameBuilder = new StringBuilder();
            fileNameBuilder.append(file_name);
            fileNameBuilder.append("_");

            if (show_seed) {
                fileNameBuilder.append(seed);
                fileNameBuilder.append("_");
            }

            if (show_phone_number) {
                fileNameBuilder.append(phoneNumber);
                fileNameBuilder.append("_");
            }

            fileNameBuilder.append(System.currentTimeMillis());
            fileNameBuilder.append("_");



            file_name = fileNameBuilder.toString();

            String suffix = "";
            switch (output_format) {
                case MediaRecorder.OutputFormat.AMR_NB: {
                    suffix = ".amr";
                    break;
                }
                case MediaRecorder.OutputFormat.AMR_WB: {
                    suffix = ".amr";
                    break;
                }
                case MediaRecorder.OutputFormat.MPEG_4: {
                    suffix = ".mp4";
                    break;
                }
                case MediaRecorder.OutputFormat.THREE_GPP: {
                    suffix = ".3gp";
                    break;
                }
                default: {
                    suffix = ".amr";
                    break;
                }
            }

            audiofile = File.createTempFile(file_name, suffix, sampleDir);

            recorder = new MediaRecorder();
            recorder.setAudioSource(audio_source);
            recorder.setOutputFormat(output_format);
            recorder.setAudioEncoder(audio_encoder);
            recorder.setOutputFile(audiofile.getAbsolutePath());
            recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mediaRecorder, int i, int i1) {

                }
            });

            try {
                recorder.prepare();
            } catch (IllegalStateException e) {
                Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            } catch (IOException e) {
                Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void releaseMediaRecorder() {
        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }

    /*
    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            if (prepareAudioRecorder(, "", "")) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                recorder.start();
                Log.i(TAG, "record start");
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            isRecordStarted = true;
            onRecordingStarted(, callRecord, audiofile);
        }
    }
    */

}
