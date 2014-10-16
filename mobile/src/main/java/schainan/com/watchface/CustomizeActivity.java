package schainan.com.watchface;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.NodeApi.GetConnectedNodesResult;
import com.google.android.gms.wearable.Wearable;


import schainan.com.watchface.colorpicker.ColorPickerPalette;
import schainan.com.watchface.colorpicker.ColorPickerSwatch.OnColorSelectedListener;

/**
 * @author Samir Chainani (samir.chainani@gmail.com)
 * @date Oct 13, 2014
 */
public class CustomizeActivity extends Activity implements OnColorSelectedListener {

    private static final String SECOND_HAND_COLOR_KEY = "secondhandcolor";

    GoogleApiClient mGoogleApiClient;
    private ColorPickerPalette mPalette;
    private int[] mColors;
    private int mSelectedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        ActionBar ab = getActionBar();
        ab.setTitle(R.string.app_name);

        setContentView(R.layout.activity_customize);

        ensureUiColorPicker();
    }

    private void ensureUiColorPicker() {
        Resources resources = getResources();
        mColors = new int[] {
                resources.getColor(R.color.secondhand_color_1),
                resources.getColor(R.color.secondhand_color_2),
                resources.getColor(R.color.secondhand_color_3),
                resources.getColor(R.color.secondhand_color_4),
                resources.getColor(R.color.secondhand_color_5),
                resources.getColor(R.color.secondhand_color_6),
                resources.getColor(R.color.secondhand_color_7),
                resources.getColor(R.color.secondhand_color_8)
        };

        mPalette = (ColorPickerPalette) findViewById(R.id.color_picker);
        mPalette.init(8, 4, this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSelectedColor = prefs.getInt(SECOND_HAND_COLOR_KEY, getResources().getColor(R.color.secondhand_color_1));

        refreshPalette();
    }

    private void sendColorUpdate(int color) {
        final byte[] data = colorToByteArray(color);
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/color", data);
                        }
                    }
                });
    }

    private void refreshPalette() {
        if (mPalette != null && mColors != null) {
            mPalette.drawPalette(mColors, mSelectedColor);
        }
    }

    private byte[] colorToByteArray(int color) {
        String colorString = String.valueOf(color);
        return colorString.getBytes();
    }

    @Override
    public void onColorSelected(int color) {
        if (color != mSelectedColor) {
            mSelectedColor = color;
            // Redraw palette to show checkmark on newly selected color
            mPalette.drawPalette(mColors, mSelectedColor);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putInt(SECOND_HAND_COLOR_KEY, mSelectedColor).commit();

            sendColorUpdate(mSelectedColor);
        }
    }
}
