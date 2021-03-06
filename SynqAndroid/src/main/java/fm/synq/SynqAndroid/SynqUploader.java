package fm.synq.SynqAndroid;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;

/**
 * Created by Kjartan on 09.11.2016.
 */

public class SynqUploader {

    public SynqUploader() {

    }


    /**
     * Upload a video file to Amazon Web Services
     *
     * @param videoFile     The video file to upload
     * @param jsonObject    Parameters for AWS, as a JsonObject
     * @param context       The context from the calling activity
     * @param callback      A SynqUploadHandler to handle upload progress and upload complete
     */
    public void uploadFile(
            File videoFile,
            JsonObject jsonObject,
            Context context,
            final SynqUploadHandler callback) {

        // Set parameter names as strings
        String awsParamAccessKeyId = context.getResources().getString(R.string.AwsParamAccessKeyId);
        String awsParamContentType = context.getResources().getString(R.string.AwsParamContentType);
        String awsParamPolicy = context.getResources().getString(R.string.AwsParamPolicy);
        String awsParamSignature = context.getResources().getString(R.string.AwsParamSignature);
        String awsParamAcl = context.getResources().getString(R.string.AwsParamAcl);
        String awsParamKey = context.getResources().getString(R.string.AwsParamKey);

        // Check if some of the parameters are missing, to prevent crash
        if (jsonObject.get(awsParamAccessKeyId) == null ||
                jsonObject.get(awsParamContentType) == null ||
                jsonObject.get(awsParamPolicy) == null ||
                jsonObject.get(awsParamSignature) == null ||
                jsonObject.get(awsParamAcl) == null ||
                jsonObject.get(awsParamKey) == null) {
            Log.e("f", "Error, missing AWS parameter");
            return;
        }

        Ion.with(context)
                .load("POST", jsonObject.get("action").getAsString())
                .uploadProgressHandler(new ProgressCallback() {
                    @Override
                    public void onProgress(long transferred, long total) {
                        callback.onProgress(transferred, total);
                    }
                })
                .setMultipartParameter(awsParamAccessKeyId, jsonObject.get(awsParamAccessKeyId).getAsString())
                .setMultipartParameter(awsParamContentType, jsonObject.get(awsParamContentType).getAsString())
                .setMultipartParameter(awsParamPolicy, jsonObject.get(awsParamPolicy).getAsString())
                .setMultipartParameter(awsParamSignature, jsonObject.get(awsParamSignature).getAsString())
                .setMultipartParameter(awsParamAcl, jsonObject.get(awsParamAcl).getAsString())
                .setMultipartParameter(awsParamKey, jsonObject.get(awsParamKey).getAsString())
                .setMultipartFile("file", videoFile)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {

                        if(e != null){
                            Log.e("f", "Exception: " + e);
                            callback.onFailure(e.toString());
                        }
                        else if(result != null && result.length() > 0) {
                            // An error occured
                            callback.onFailure(result);
                        }
                        else {
                            // Upload success
                            callback.onCompleted();
                        }
                    }
                });
    }
}
