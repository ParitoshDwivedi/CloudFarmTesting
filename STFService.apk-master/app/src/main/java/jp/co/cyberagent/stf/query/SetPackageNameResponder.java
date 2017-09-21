package jp.co.cyberagent.stf.query;

import android.content.Context;
import android.util.Log;

import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.InvalidProtocolBufferException;

import jp.co.cyberagent.stf.proto.Wire;

/**
 * Created by root on 14/9/17.
 */

public class SetPackageNameResponder extends AbstractResponder{
    private static final String TAG  = "STFPackage";

    public SetPackageNameResponder(Context context) {
        super(context);
    }

    @Override
    public GeneratedMessageLite respond(Wire.Envelope envelope) throws InvalidProtocolBufferException {
        Wire.SetPackageNameRequest request =
                Wire.SetPackageNameRequest.parseFrom(envelope.getMessage());
        String pkg = request.getName();

        Log.i(TAG, String.format("Package %s was received", pkg));

        return Wire.Envelope.newBuilder()
                .setId(envelope.getId())
                .setType(Wire.MessageType.SET_MASTER_MUTE)
                .setMessage(Wire.SetMasterMuteResponse.newBuilder()
                        .setSuccess(true)
                        .build()
                        .toByteString())
                .build();
    }

    @Override
    public void cleanup() {

    }
}
