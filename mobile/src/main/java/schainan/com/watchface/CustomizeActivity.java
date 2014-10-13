package schainan.com.watchface;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Samir Chainani (samir.chainani@gmail.com)
 * @date Oct 13, 2014
 */
public class CustomizeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar ab = getActionBar();
        ab.setTitle(R.string.app_name);

        setContentView(R.layout.activity_customize);
    }

}
